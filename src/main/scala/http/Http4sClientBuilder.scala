package com.afjcjsbx.sparkttp
package http

import models.Http4sClientBuilderParameters

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import com.afjcjsbx.sparkttp.utils.Loggable
import org.http4s.{ Method, Request, Response }
import org.http4s.Status.{
  BadGateway,
  GatewayTimeout,
  InternalServerError,
  NotFound,
  RequestTimeout,
  ServiceUnavailable
}
import org.http4s.client.{ Client, WaitQueueTimeoutException }
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{ Logger, Retry, RetryPolicy }

import scala.concurrent.ExecutionContext

object Http4sClientBuilder extends Loggable {

  private val retriableStatuses = Set(
    NotFound,
    RequestTimeout,
    BadGateway,
    InternalServerError,
    ServiceUnavailable,
    GatewayTimeout
  )

  private def isRetriable[F[_]](req: Request[F], result: Either[Throwable, Response[F]]): Boolean =
    (req.method.isIdempotent || req.method == Method.POST) && handleResult(result)

  private def handleResult[F[_]](result: Either[Throwable, Response[F]]): Boolean =
    result match {
      case Right(resp)                     => retriableStatuses(resp.status)
      case Left(WaitQueueTimeoutException) => false
      case _                               => true
    }

  def buildClient[F[_]: ConcurrentEffect](
    params: Http4sClientBuilderParameters
  )(implicit executionContext: ExecutionContext, t: Timer[F]): Resource[F, Client[F]] = {
    val blazeClientResource = buildBlazeClient(params)
    val retryPolicy         = buildRetryPolicy(params)

    blazeClientResource.map { client =>
      val retryClient = Retry[F](retryPolicy)(client)
      Logger(logHeaders = true, logBody = true)(retryClient)
    }
  }

  private def buildBlazeClient[F[_]: ConcurrentEffect](
    params: Http4sClientBuilderParameters
  )(implicit executionContext: ExecutionContext): Resource[F, Client[F]] =
    BlazeClientBuilder(executionContext)
      .withResponseHeaderTimeout(params.responseHeaderTimeout)
      .withIdleTimeout(params.idleTimeout)
      .withRequestTimeout(params.requestTimeout)
      .withConnectTimeout(params.connectTimeout)
      .withParserMode(params.parserMode)
      .resource

  private def buildRetryPolicy[F[_]: ConcurrentEffect](
    params: Http4sClientBuilderParameters
  ): RetryPolicy[F] =
    RetryPolicy[F](
      RetryPolicy
        .exponentialBackoff(params.maxRetryDelay, params.maxRetries),
      isRetriable
    )

}
