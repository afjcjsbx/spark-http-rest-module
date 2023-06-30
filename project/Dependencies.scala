import sbt._

/*
 * Dependencies definitions.
 *
 * See project/Versions.scala for the versions definitions.
 */
object Dependencies extends TestDependencies {
  private val `cats-effect` = "org.typelevel" %% "cats-effect" % Versions.`cats-effect`

  private val `circe-core`    = "io.circe" %% "circe-core"    % Versions.`circe`
  private val `circe-generic` = "io.circe" %% "circe-generic" % Versions.`circe`
  private val `circe-parser`  = "io.circe" %% "circe-parser"  % Versions.`circe`

  private val `http4s-dsl`          = "org.http4s" %% "http4s-dsl"          % Versions.http4sVersion
  private val `http4s-core`         = "org.http4s" %% "http4s-core"         % Versions.http4sVersion
  //private val `http4s-circe`        = "org.http4s" %% "http4s-circe"        % Versions.http4sVersion
  private val `http4s-client`       = "org.http4s" %% "http4s-client"       % Versions.http4sVersion
  private val `http4s-server`       = "org.http4s" %% "http4s-server"       % Versions.http4sVersion // maybe can be removed
  private val `http4s-blaze-client` = "org.http4s" %% "http4s-blaze-client" % Versions.http4sVersion
  private val `http4s-blaze-server` = "org.http4s" %% "http4s-blaze-server" % Versions.http4sVersion

  private val `spark-core` = "org.apache.spark" %% "spark-core" % "3.4.0" % Provided
  private val `spark-sql`  = "org.apache.spark" %% "spark-sql"  % "3.4.0" % Provided

  val libs: Seq[ModuleID] =
    Vector[ModuleID](
      `circe-core`,
      `circe-generic`,
      `circe-parser`,
      `http4s-dsl`,
      `http4s-core`,
      //`http4s-circe`,
      `http4s-client`,
      `http4s-blaze-client`,
      `http4s-blaze-server`,
      `http4s-server`,
      `spark-core`,
      `spark-sql`,
      `cats-effect`
    )
}
