package com.afjcjsbx.sparkttp
package models

import org.apache.spark.sql.types.{ IntegerType, MapType, StringType, StructField, StructType }

object ResponseWrapper {
  val KEY         = "key"
  val VALUE       = "value"
  val HEADERS     = "headers"
  val STATUS_CODE = "statusCode"

  val schema: StructType = StructType(
    Seq(
      StructField(KEY, StringType, nullable = false),
      StructField(VALUE, StringType, nullable = true),
      StructField(HEADERS, MapType(StringType, StringType), nullable = true),
      StructField(STATUS_CODE, IntegerType, nullable = true)
    )
  )
}

/**
 * Standard container class used to pass and encode single json objects
 *
 * @param key     request id
 * @param value   Json Body
 * @param headers headers
 */
case class ResponseWrapper(
  key: String,
  value: String,
  headers: Map[String, String] = Map.empty,
  statusCode: Int
)
