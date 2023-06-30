import sbt._

trait TestDependencies {

  private val scalatest  = "org.scalatest"  %% "scalatest"  % Versions.scalatest  % Test
  private val scalactic  = "org.scalactic"  %% "scalactic"  % Versions.scalatest  % Test
  private val scalacheck = "org.scalacheck" %% "scalacheck" % Versions.scalacheck % Test
  private val `cats-effect-testing-scalatest` =
    "com.codecommit" %% "cats-effect-testing-scalatest" % Versions.`cats-effect-testing-scalatest` % Test
  val log4jTest: Vector[sbt.ModuleID] = Vector[ModuleID](
    "org.apache.logging.log4j" % "log4j-api"        % "2.17.1",
    "org.apache.logging.log4j" % "log4j-core"       % "2.17.1",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1"
  )
  val mrPower = "com.github.mrpowers" %% "spark-fast-tests" % "0.23.0" % "test"

  val tests: Vector[sbt.ModuleID] =
    Vector[ModuleID](scalatest, scalactic, scalacheck, `cats-effect-testing-scalatest`, mrPower) ++ log4jTest
}
