import Dependencies._
import sbt.Keys._

import scala.util.Try

// give the user a nice default project!
lazy val buildNumber = sys.env.get("BUILD_NUMBER").map( bn => s"b$bn")

lazy val root = (project in file("."))
  .configs       (IntegrationTest)
  .settings      (Defaults.itSettings:_*)
  .enablePlugins (PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    name                                  := "recommend-articles",
    PlayKeys.playMonitoredFiles           ++= (sourceDirectories in Compile).value,
    // Deployment
    (stackCustomParams in DeployDev    )  += ("BuildVersion" -> version.value),
    (stackCustomParams in DeployPreProd)  += ("BuildVersion" -> version.value),
    (stackCustomParams in DeployProd   )  += ("BuildVersion" -> version.value),

    // Assembly
    mainClass             in assembly     := Some("play.core.server.ProdServerStart"),
    target                in assembly     := file("target"),
    assemblyJarName       in assembly     := s"${name.value}-${version.value}.jar",
    fullClasspath         in assembly     += Attributed.blank(PlayKeys.playPackageAssets.value),
    assemblyMergeStrategy in assembly     := {
      case "application.conf"             => MergeStrategy.concat
      case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    test                  in assembly     := {},

    ServiceDependencies
  )

lazy val ct = (project in file("component-test"))
  .disablePlugins(PipelinePlugin)
  .settings(
    envVars := {
      def queryDockerInstancePort(serviceName:String, port:Int, default:String = ""):String = {
        Try{
          val ipAndPort = s"docker-compose port $serviceName $port".!!
          ipAndPort.trim.split(':').last
        }.getOrElse(default)
      }

      Map(
        "SERVICE_PORT"     -> queryDockerInstancePort("service",    9000, "9000"),
        "ENGINE_API_PORT"  -> queryDockerInstancePort("engine-api", 8080),
        "STORAGE_PORT"     -> queryDockerInstancePort("storage",    8080)
      )
    },
    fork                      := true,
    parallelExecution in Test := false,
    publishArtifact           := false,
    ComponentTests
  )
