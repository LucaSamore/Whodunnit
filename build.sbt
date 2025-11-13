ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

coverageExcludedPackages := ".*view.*;.*controller.*"

inThisBuild(
  List(
    scalaVersion := "3.3.6",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:all", "-Wunused:imports")
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "PPS-24-whodunnit",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "24.0.2-R36",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "com.lihaoyi" %% "upickle" % "4.4.0",
      "org.scalamock" %% "scalamock" % "7.5.0" % Test,
      "com.softwaremill.sttp.client3" %% "core" % "3.11.0",
      "com.softwaremill.sttp.client3" %% "circe" % "3.11.0",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",
      "io.github.cdimascio" % "dotenv-java" % "3.2.0"
    ),
    mainClass := Some("view.WhodunnitApp"),
    assembly / assemblyOutputPath := target.value / "scala-3" / "whodunnit.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  ).enablePlugins(AssemblyPlugin)
