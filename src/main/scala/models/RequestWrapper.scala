package com.afjcjsbx.sparkttp
package models

import org.apache.spark.sql.types.{ MapType, StringType, StructField, StructType }

import java.util.UUID

object RequestWrapper {
  val KEY     = "key"
  val VALUE   = "value"
  val HEADERS = "headers"

  val schema: StructType = StructType(
    Seq(
      StructField(KEY, StringType, nullable = false),
      StructField(VALUE, StringType, nullable = true),
      StructField(HEADERS, MapType(StringType, StringType), nullable = true)
    )
  )
}

/**
 * Standard container class used to pass and encode single json objects
 *
 * @param key         request id
 * @param value       Json Body
 * @param headers headers
 */
case class RequestWrapper(
  key: String = UUID.randomUUID().toString,
  value: String,
  headers: Map[String, String]
)
