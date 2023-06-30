package com.afjcjsbx.sparkttp
package utils

import cats.effect.{Clock, IO, Timer}

import java.util.concurrent.ScheduledExecutorService
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Basic implementatin of the Timer[IO] */
final class BasicTimer(sc: ScheduledExecutorService) extends Timer[IO] {
  override val clock: Clock[IO] =
    new Clock[IO] {
      override def realTime(unit: TimeUnit): IO[Long] =
        IO(unit.convert(System.currentTimeMillis(), MILLISECONDS))

      override def monotonic(unit: TimeUnit): IO[Long] =
        IO(unit.convert(System.nanoTime(), NANOSECONDS))
    }

  override def sleep(timespan: FiniteDuration): IO[Unit] =
    IO.cancelable { cb =>
      val tick = new Runnable {
        def run(): Unit = {
          val ec = ExecutionContext.fromExecutorService(sc)
          ec.execute(() => cb(Right(())))
        }
      }
      val f = sc.schedule(tick, timespan.length, timespan.unit)
      IO(f.cancel(false))
    }
}
