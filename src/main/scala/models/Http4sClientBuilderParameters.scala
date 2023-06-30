package com.afjcjsbx.sparkttp
package models

import org.http4s.client.blaze.ParserMode
import org.http4s.client.defaults

import scala.concurrent.duration.{Duration, DurationInt}

case class Http4sClientBuilderParameters(
  responseHeaderTimeout: Duration = Duration.Inf,
  idleTimeout: Duration = 1.minute,
  requestTimeout: Duration = defaults.RequestTimeout,
  connectTimeout: Duration = defaults.ConnectTimeout,
  parserMode: ParserMode = ParserMode.Strict,
  maxRetries: Int = 3, // number of calls made will be 1 + maxRetries
  maxRetryDelay: Duration = 1.minute
)