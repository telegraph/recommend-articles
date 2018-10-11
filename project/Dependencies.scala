import sbt.Keys._
import sbt._

object Dependencies {
  val TmgUtils         = "1.0.0-b61"
  val TmgHttpClient    = "1.1.0-b86"
  val TmgPlayServerExt = "1.1.0-b77"
  val Json4sVersion    = "3.5.3"
  val PlayVersion      = "2.6.0"
  val AwsSdkVersion    = "1.11.271"
  val ScalaTestVersion = "3.0.4"
  val AkkaVersion      = "2.5.3"

  val ServiceDependencies: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      // Telegraph Utils
      "uk.co.telegraph"              %%  "play-server-ext"              % TmgPlayServerExt,
      "uk.co.telegraph"              %%  "generic-client"               % TmgUtils,
      "uk.co.telegraph"              %%  "http-client"                  % TmgHttpClient,

      "uk.co.telegraph"              %%  "ucm-library"                  % "1.1.0-b44",

      "com.typesafe.play"            %%  "play"                         % PlayVersion,
      "com.typesafe.play"            %%  "play-guice"                   % PlayVersion,
      "org.yaml"                     %   "snakeyaml"                    % "1.20",
      "com.amazonaws"                %   "aws-java-sdk-kms"             % AwsSdkVersion  excludeAll ExclusionRule("commons-logging", "commons-logging"),

      // Test dependencies
      "org.scalatest"                %%  "scalatest"                    % ScalaTestVersion % Test,
      "org.scalamock"                %%  "scalamock-scalatest-support"  % "3.6.0"          % Test,
      "uk.co.telegraph"              %%  "http-client-testkit"          % TmgHttpClient    % Test,
      "com.typesafe.akka"            %% "akka-stream-testkit"           % AkkaVersion      % Test
    )
  )

  val ComponentTests: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest"                 %%  "scalatest"                             % ScalaTestVersion,
      "io.rest-assured"               %   "scala-support"                         % "3.0.6",
      "com.atlassian.oai"             %   "swagger-request-validator-restassured" % "1.3.10",
      "com.github.tomakehurst"        %   "wiremock"                              % "2.14.0",
      "org.json4s"                    %%  "json4s-native"                         % Json4sVersion,
      "com.typesafe"                  %   "config"                                % "1.3.1"
    )
  )
}
