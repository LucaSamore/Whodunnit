ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "PPS-24-whodunnit",
    libraryDependencies += "org.scalatest" %% "scalatest-flatspec" % "3.2.19" % "test"
  )
