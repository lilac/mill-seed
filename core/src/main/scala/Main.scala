import cats.effect.{ExitCode, FiberIO, IO, IOApp}

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  private val task = {
    val workers = 1.to(100000)
      .toList
      .map(worker)
    for {
      fibers <- startAll(workers)
      results <- awaitAll(fibers)
    } yield results
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    start <- IO.realTime
    results <- task
      .timeoutTo(3000.millis, IO.pure(Seq.empty))
      .timed
    end <- IO.realTime
    (time, nums) = results
    _ <- IO.println(s"Time: ${time.toMillis}") *>
      IO.println(s"Real time: ${end - start}") *>
      IO.println(s"Results count: ${nums.size}")
  } yield {
    ExitCode.Success
  }

  def startAll[T](tasks: List[IO[T]]): IO[List[FiberIO[T]]] = {
    tasks match {
      case Nil => IO.pure(List.empty)
      case t :: Nil => t.start.map(List(_))
      case a :: b :: other =>
        for {
          fa <- a.start
          fb <- b.start
          fo <- startAll(other)
        } yield fa :: fb :: fo
    }
  }

  def awaitAll[T](fibers: List[FiberIO[T]]): IO[List[T]] = {
    fibers match {
      case Nil => IO.pure(Nil)
      case f :: Nil =>
        for {
          outcome <- f.joinWithNever
        } yield List(outcome)
      case fa :: fb :: fs =>
        for {
          ra <- fa.joinWithNever
          rb <- fb.joinWithNever
          ro <- awaitAll(fs)
        } yield ra :: rb :: ro
    }
  }

  def worker(id: Int): IO[Int] = for {
    _ <- IO.sleep(1.second)
  } yield id * 2
}

