package com.afjcjsbx.sparkttp
package http

import exceptions.UnexpectedStatus
import http.HttpHelper.{getErrorMessage, getErrorStatus, getPayloadFromError, parseJsonUnsafe}
import models.{RequestWrapper, ResponseWrapper}

import cats.effect.IO
import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.parser
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.`Content-Type`

import scala.util.Try

/**
 * Helper class used for http calls
 *
 * @param client          hhtp4sclient
 * @param maxAttempts     max attempts before failing
 * @param sleepTimeMillis sleep time after each attempt (at each attempt the sleep time is computed as sleepTimeMillis * 2**attempt)
 * @param ec              ExecutionContext Required by Http4s
 * @param es              ExecutorService Required by Http4s
 */
class HttpHelper(val client: Client[IO]) {

  /**
   * Single http call skipping retry
   */
  def call(api: String, data: String): IO[String] = {
    val req = Request[IO]()
      .withMethod(Method.POST)
      .withUri(Uri.unsafeFromString(api))
      .withEntity(data)
      .withContentType(`Content-Type`.apply(MediaType.application.json))
    client.expect[String](req)
  }

  /**
   * Post Http call with retry mechanism, the body of the request is taken from data.value and it is replaced afterwards
   * the value of status code will be set in this way:
   * 102 -> application error (the semantic of the data is wrong)
   * 110 -> System/Network error
   * 100 -> ok
   * Following the values of the sealed trait [[StatusCode]]
   */
  def callWithRetry(
    api: String,
    data: RequestWrapper,
    method: Method
  ): IO[ResponseWrapper] =
    internalCallWithRetry(api, data, method)



  private def internalCallWithRetry(
    api: String,
    data: RequestWrapper,
    method: Method
  ): IO[ResponseWrapper] = {
    val uri = Uri.unsafeFromString(api)

    //logger.info(s"HTTP Call $uri - $method - id: $counter - retry: $retryCounter")
    val req = method match {
      case Method.POST =>
        Request[IO]()
          .withMethod(Method.POST)
          .withUri(uri)
          .withEntity(data.value)
          .withHeaders(
            if (data.headers.nonEmpty) Headers(data.headers.map(x => Header(x._1, x._2)).toList)
            else Headers.of(Header("Accept", "*/*"))
          )
          .withContentType(`Content-Type`.apply(MediaType.application.json))
      case Method.GET  =>
        Request[IO]()
          .withMethod(Method.GET)
          .withHeaders(Header("Accept", "*/*"))
          .withUri(
            uri.withQueryParams(Try(parseJsonUnsafe(data.value)).getOrElse(Map.empty))
          )
          .withContentType(`Content-Type`.apply(MediaType.application.json))

      case _ => throw new UnsupportedOperationException
    }



    client
      .expectOr[String](req) { res =>
        val body = res.body.through(fs2.text.utf8Decode).compile.string
        body.map(UnexpectedStatus(res.status, _))
      }
      .redeemWith(
        //error case, retry
        ex =>
          getErrorStatus(ex) match {
            case _ =>
              //logger.error(s"HTTP Response ApplicationError - id: $counter - retry: $retryCounter")
              IO(
                ResponseWrapper(
                  key = data.key,
                  value = getPayloadFromError(ex).getOrElse(getErrorMessage(data.key, ex)),
                  statusCode = getErrorStatus(ex)
                )
              )
          },
        //success case, transform the value inside ProcessData.value
        s =>
          IO.pure({
            //val size = s.getBytes.length
            //logger.info(s"HTTP Call $uri - $method - id: $counter - retry: $retryCounter -> Response Size $size B")
            ResponseWrapper(
              key = data.key,
              value = s,
              statusCode = 200
            )
          })
      )


  }

}

object HttpHelper {

  private val om: ObjectMapper = new ObjectMapper()

  def parseJsonUnsafe(json: String): Map[String, String] =
    parser.decode[Map[String, String]](json).getOrElse(throw new Exception("Invalid JSON to parse"))

  def getErrorStatus(ex: Throwable): Int =
    ex match {
      case e: UnexpectedStatus => e.status.code
      case _                   => -1
    }

  def getPayloadFromError(ex: Throwable): Option[String] =
    ex match {
      case e: UnexpectedStatus => Some(e.payload)
      case _                   => None
    }

  def getErrorMessage(key: String, ex: Throwable): String =
    generateErrorJson(key, description = ex.getMessage)

  def generateErrorJson(key: String, description: String): String =
    generateJson(Map("id" -> key, "description" -> description))

  def generateJson(map: Map[String, String]): String              =
    om.writeValueAsString(map)

}
