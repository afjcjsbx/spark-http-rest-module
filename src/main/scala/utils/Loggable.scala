package com.afjcjsbx.sparkttp
package utils

import org.apache.logging.log4j.{LogManager, Logger}

trait Loggable {
  val logger: Logger = LogManager.getLogger(getClass.getName)
}