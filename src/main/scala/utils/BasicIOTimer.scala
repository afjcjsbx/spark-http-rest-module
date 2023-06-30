package com.afjcjsbx.sparkttp
package utils

import cats.effect.{Clock, IO, Timer}

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import scala.concurrent.duration._

final class BasicIOTimer(implicit scheduler: ScheduledExecutorService) extends Timer[IO] {

  private val clockInstance = new Clock[IO] {
    override def realTime(unit: TimeUnit): IO[Long] =
      IO(unit.convert(System.currentTimeMillis(), MILLISECONDS))

    override def monotonic(unit: TimeUnit): IO[Long] =
      IO(unit.convert(System.nanoTime(), NANOSECONDS))
  }

  override val clock: Clock[IO] = clockInstance

  override def sleep(timespan: FiniteDuration): IO[Unit] = IO.cancelable { cb =>
    val task   = new Runnable {
      def run(): Unit = cb(Right(()))
    }
    val future = scheduler.schedule(task, timespan.length, timespan.unit)
    IO(future.cancel(false)).void
  }
}
