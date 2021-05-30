import cats.effect.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for {
    start <- IO.realTime
    results <- 1.to(1000)
      .toList
      .parUnorderedTraverse(worker)
    end <- IO.realTime
  } yield {
    println(s"Time: ${end - start}")
    println(s"Results: $results")
    ExitCode.Success
  }

  def worker(id: Int): IO[Int] = for {
    _ <- IO.sleep(1.second)
    _ = println(s"worker $id started")
  } yield id * 2
}

