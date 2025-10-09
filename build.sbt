import scala.collection.immutable.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "PPS-24-whodunnit",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "23.0.1-R34",
      "org.scalatest" %% "scalatest-flatspec" % "3.2.19" % "test"),
    mainClass := Some("Launcher")
  )
