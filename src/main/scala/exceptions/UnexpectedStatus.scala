package com.afjcjsbx.sparkttp
package exceptions

import org.http4s.Status

import scala.util.control.NoStackTrace

final case class UnexpectedStatus(status: Status, payload: String) extends RuntimeException with NoStackTrace {
  override def getMessage: String = s"unexpected HTTP status: ${status.code}, payload: '$payload'"

  def getPayload: String = payload
}
