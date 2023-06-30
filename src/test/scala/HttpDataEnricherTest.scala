package com.afjcjsbx.sparkttp

import models.RequestWrapper

import com.afjcjsbx.sparkttp.http.HttpDataEnricher
import org.apache.spark.sql.SparkSession
import org.http4s.Method
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HttpDataEnricherTest extends AnyFlatSpec with Matchers {

  implicit val spark: SparkSession = SparkSession
    .builder()
    .appName("HttpDataEnricherTest")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  "HttpDataEnricher" should "enrich data using the HTTP endpoint" in {
    val httpDataEnricher = HttpDataEnricher
    val dataset = Seq(
      RequestWrapper("key1", "value1", Map.empty),
      RequestWrapper("key2", "value2", Map.empty)
    ).toDS()

    val getEndpoint: String => String = _ => "http://127.0.0.1:8080/test"
    val method: Method = Method.POST

    val result = httpDataEnricher
      .call(dataset, getEndpoint, method)
      .cache()

    result.show(false)
    result.count() should be > 0L
    // Assert other expectations here...
    spark.stop()
  }
}

