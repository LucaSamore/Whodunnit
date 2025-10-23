ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

coverageExcludedPackages := ".*view.*"

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
      "org.scalafx" %% "scalafx" % "23.0.1-R34",
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "com.lihaoyi" %% "upickle" % "4.3.1"
    ),
    mainClass := Some("Launcher")
  )
