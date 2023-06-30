package com.afjcjsbx.sparkttp

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.Root
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.dsl.io._

object MockServer extends IOApp {

  val service: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "test" => 
      IO(println(s"Received GET request: ${req}")) *> Ok("GET request processed")

    case req @ POST -> Root / "test" =>
      req.as[String].flatMap { body =>
        IO(println(s"Received POST request: ${req}, body: $body")) *> BadGateway("POST request processed")
      }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(service.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
