import cats.effect.implicits._
import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.DurationInt

object BackPressure extends IOApp {
  val maxTasks = 1000

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      queue <- Queue.bounded[IO, Int](100)
      fa <- producer(queue).start
      fb <- consumer(queue).foreverM.start.replicateA(100)
      _ <- /*fa.join race */ IOExtension.awaitAll(fb)
    } yield ExitCode.Success
  }

  private def producer(queue: Queue[IO, Int]) = {
    1.to(maxTasks).to(LazyList).parTraverse_(queue.offer)
  }

  private def consumer(queue: Queue[IO, Int]): IO[Unit] = {
    for {
      e <- queue.take
      _ <- IO.sleep(1.second) >>
        IO.println(s"Got $e")
    } yield ()
  }
}
