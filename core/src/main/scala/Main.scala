import cats.effect.{ExitCode, IO, IOApp}

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  private val task = {
    val workers = 1.to(10000000)
      .toList
      .map(worker)
    for {
      fibers <- IOExtension.startAll(workers)
      results <- IOExtension.awaitAll(fibers)
    } yield results
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    start <- IO.realTime
    results <- task
      .timeoutTo(30000.millis, IO.pure(Seq.empty))
      .timed
    end <- IO.realTime
    (time, nums) = results
    _ <- IO.println(s"Time: ${time.toMillis}") *>
      IO.println(s"Real time: ${end - start}") *>
      IO.println(s"Results count: ${nums.size}")
  } yield {
    ExitCode.Success
  }

  def worker(id: Int): IO[Int] = for {
    _ <- IO.sleep(1.second)
  } yield id * 2
}

