package com.afjcjsbx.sparkttp
package http

import models.{ Http4sClientBuilderParameters, RequestWrapper, ResponseWrapper }
import utils.BasicIOTimer

import cats.effect.{ ContextShift, IO }
import io.netty.util.concurrent.DefaultEventExecutor
import org.apache.spark.sql.{ Dataset, Encoders }
import org.http4s.Method

import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.ExecutionContext

object HttpDataEnricher {

  /**
   * Process a dataframe using an http endpoint by passing each record through the [[HttpHelper]] class
   *
   * @param ds                      input Datasource
   * @param getEndpoint             function that take s ProcessedData.key and retrieves the correct endpoint for the datapoint
   * @param method                  http method
   * @param inputTransform          transformation to be done to processedData.value before sending it to the endpoint
   * @param outputTransform         transformation to be done to processedData.value after receiving it from the endpoint
   * @param filterFunction                 Exclude some values from the dataset
   */
  def call(
    ds: Dataset[RequestWrapper],
    getEndpoint: String => String,
    method: Method,
    inputTransform: String => String = identity[String],
    outputTransform: String => String = identity[String],
    filterFunction: Option[RequestWrapper => Boolean] = None
  ): Dataset[ResponseWrapper] =
    ds.mapPartitions { it =>
      val clientBuilderParameters: Http4sClientBuilderParameters =
        Http4sClientBuilderParameters()
      implicit val ec: ExecutionContext                          = ExecutionContext.global
      implicit val contextShift: ContextShift[IO]                = IO.contextShift(ec)
      implicit val ses: ScheduledExecutorService                 = new DefaultEventExecutor()
      implicit val t: BasicIOTimer                               = new BasicIOTimer()

      val client = Http4sClientBuilder.buildClient[IO](clientBuilderParameters)
      val res    = client.use { c =>
        val helper     = new HttpHelper(c)
        val filteredIt = filterFunction.map(it.filter).getOrElse(it)

        def doCall(requestWrapper: RequestWrapper): IO[ResponseWrapper] = {
          val responseWrapper = helper.callWithRetry(
            getEndpoint(requestWrapper.value),
            requestWrapper.copy(value = inputTransform(requestWrapper.value)),
            method
          )
          responseWrapper.map(o => o.copy(value = outputTransform(o.value)))
        }
        sequentialTraverse(filteredIt)(doCall)
      }
      res.unsafeRunSync()
    }(Encoders.product[ResponseWrapper])

  private def sequentialTraverse[A, B](seq: Iterator[A])(f: A => IO[B]): IO[Iterator[B]] =
    seq
      .foldLeft(IO(List.empty[B])) { case (accIO, nxt) =>
        for {
          acc <- accIO
          b   <- f(nxt)
        } yield b :: acc
      }
      .map(_.iterator)
}
