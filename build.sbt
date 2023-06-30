
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.0"

lazy val root = (project in file("."))
  .settings(
    name := "spark-http-rest-module",
    idePackagePrefix := Some("com.afjcjsbx.sparkttp")
  )
  .settings(Settings.commonSettings: _*)
  .settings(
    libraryDependencies ++= Dependencies.libs
      /* .map(_ % Provided force ()) */ ++: Dependencies.tests
  )
  .enablePlugins()

