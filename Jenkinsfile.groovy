def sendNotification( action, token, channel, shellAction ){
  try {
    slackSend message: "${action} Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", token: token, channel: channel, teamDomain: "telegraph", baseUrl: "https://hooks.slack.com/services/", color: "warning"
    sh shellAction  
  } catch (error) {
    slackSend message: "${action} Failed - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", token: token, channel: channel, teamDomain: "telegraph", baseUrl: "https://hooks.slack.com/services/", color: "danger"
    throw error
  }
  slackSend message: "${action} Finished - ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)", token: token, channel: channel, teamDomain: "telegraph", baseUrl: "https://hooks.slack.com/services/", color: "good"
}

def sendMessage(message, channel, color = "good") {
    slackSend message: "$message", token: "${env.SLACK_PLATFORMS_RELEASES}", channel: channel, teamDomain: "telegraph", baseUrl: "https://hooks.slack.com/services/", color: color
}


ansiColor('xterm') {
    lock("${env.PROJECT_NAME}"){
        node ("master"){

            def sbtFolder          = "${tool name: 'sbt-0.13.13', type: 'org.jvnet.hudson.plugins.SbtPluginBuilder$SbtInstallation'}/bin"
            def projectName        = "${env.PROJECT_NAME}"
            def github_token       = "${env.GITHUB_TOKEN}"
            def jenkins_github_id  = "${env.JENKINS_GITHUB_CREDENTIALS_ID}"
            def docker_account     = "${env.AWS_ECR_DOCKER_ACCOUNT}"
            def docker_registry    = "${env.AWS_ECR_DOCKER_REGISTRY}"
            def pipeline_version   = "1.0.0-b${env.BUILD_NUMBER}"
            def github_commit      = ""

            stage("Checkout"){
                echo "git checkout"
                checkout changelog: false, poll: false, scm: [
                    $class: 'GitSCM',
                    branches: [[
                        name: 'master'
                    ]],
                    doGenerateSubmoduleConfigurations: false,
                    extensions: [[
                        $class: 'WipeWorkspace'
                    ], [
                        $class: 'CleanBeforeCheckout'
                    ]],
                    submoduleCfg: [],
                    userRemoteConfigs: [[
                        credentialsId: "${jenkins_github_id}",
                        url: "git@github.com:telegraph/${projectName}.git"
                    ]]
                ]
            }

            stage("Build & Unit Tests"){
              sendNotification("Build", "${env.SLACK_PLATFORMS_CI}", "#platforms_ci",
                """
                  ${sbtFolder}/sbt clean test
                """
              )
            }

            stage("Assembly"){
                sh """
                    ${sbtFolder}/sbt clean playUpdateSecret assembly
                """
                docker.build("${projectName}:${pipeline_version}", "--build-arg APP_NAME=${projectName} --build-arg APP_VERSION=${pipeline_version} .")
            }

            stage("Functional Tests"){
                sh """
                    echo "Running Component Tests"
                    export APP_NAME="${projectName}"
                    export APP_VERSION="${pipeline_version}"
                    export ENVIRONMENT=ct
                    
                    docker-compose up -d
                """
                try {
                    sh """
                        export APP_NAME="${projectName}"
                        export APP_VERSION="${pipeline_version}"
                        ${sbtFolder}/sbt "ct/test-only -- -n ct"
                    """
                    junit "component-test/target/test-reports/**/*.xml"
                }finally {
                    sh """
                        export APP_NAME="${projectName}"
                        export APP_VERSION="${pipeline_version}"

                        docker-compose down
                    """
                }
            }

            stage("Publish"){
                sh """
                    ${sbtFolder}/sbt publish
                """
                docker.withRegistry("${docker_account}", "${docker_registry}") {
                    docker.image("${projectName}:${pipeline_version}").push()
                }
            }
            stage("PreProd Deploy"){
              sendNotification("Deploy PreProd", "${env.SLACK_PLATFORMS_RELEASES}", "#platforms_releases",
                """
                  ${sbtFolder}/sbt preprod:stackSetup
                """
              )
            }

            stage("PreProd Tests"){
               sh """
                  echo "Running It Tests"
                """
            }

            stage("Validate prod deploy") {
                echo "Is code freeze: ${env.CODE_FREEZE}"
                if (env.CODE_FREEZE == 'true') {
                    echo "Freeze!"
                    currentBuild.result = 'ABORTED'
                    error('There is a code freeze')
                } else {
                    echo "NO Freeze - Deploying to prod!"
                }

                timeout(time: 60, unit: 'MINUTES') {

                    current_commit = sh(returnStdout: true, script: 'git show-ref --tags --head --hash| head -n1').trim()
                    previous_release_tag = sh(returnStdout: true, script: 'git show-ref --tags --head | sort -V -k2,2 | tail -n1 | cut -d " " -f1').trim()
                    tickets = sh(returnStdout: true, script: """git log --full-diff $previous_release_tag..$current_commit | grep -o 'PLAT-[0-9]*'| sort -u | uniq |awk '{print "https://jira.aws.telegraph.co.uk/browse/"\$1}'""").trim()
                    authors = sh(returnStdout: true, script: """git log --full-diff $previous_release_tag..$current_commit | grep -o 'Author: .*' | sed -e 's/Author: //g' | sort -u | uniq""").trim()

                    sendMessage(
                            "The *$projectName* pipeline is waiting to be approved for prod deployment:\n${env.BUILD_URL}/input" +
                                    "\nTicket in this release:\n$tickets" +
                                    "\nCoded by:\n$authors",
                            "#platforms_releases"
                    )
                    approver = input(message: 'Approve deployment?', submitterParameter: 'username_approval')
                    date = new Date()
                    sendMessage(
                            "The *$projectName* build: ${env.BUILD_URL} has been approved by https://jenkins-prod.api-platforms.telegraph.co.uk/user/${approver}/ at ${date}",
                            "#platforms_releases"
                    )
                }
            }

            stage("Prod Deploy"){
              sendNotification("Deploy Prod", "${env.SLACK_PLATFORMS_RELEASES}", "#platforms_releases",
                """
                  ${sbtFolder}/sbt prod:stackSetup
                """
              )
            }

            stage("Release Notes"){
                // Possible error if there is a commit different from the trigger commit
                github_commit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

                //Realease on Git
                println("\n[TRACE] **** Releasing to github ${github_token}, ${pipeline_version}, ${github_commit} ****")
                sh """#!/bin/bash
                    GITHUB_COMMIT_MSG=\$(curl -H "Content-Type: application/json" -H "Authorization: token ${github_token}" https://api.github.com/repos/telegraph/${projectName}/commits/\"${github_commit}\" | /usr/local/bin/jq \'.commit.message\')
                    echo "GITHUB_COMMIT_MSG: \${GITHUB_COMMIT_MSG}"
                    echo "GITHUB_COMMIT_DONE: DONE"
                    C_DATA="{\\\"tag_name\\\": \\\"${pipeline_version}\\\",\\\"target_commitish\\\": \\\"master\\\",\\\"name\\\": \\\"${pipeline_version}\\\",\\\"body\\\": \${GITHUB_COMMIT_MSG},\\\"draft\\\": false,\\\"prerelease\\\": false}"
                    echo "C_DATA: \${C_DATA}"
                    curl -H "Content-Type: application/json" -H "Authorization: token ${github_token}" -X POST -d "\${C_DATA}" https://api.github.com/repos/telegraph/${projectName}/releases
                """
            }
        }
    }
}
