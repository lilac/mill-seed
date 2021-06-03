import cats.effect.{ExitCode, FiberIO, IO, IOApp}

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = for {
    start <- IO.realTime
    workers = 1.to(1000)
      .toList
      .map(worker)
    fibers <- startAll(workers)
    results <- awaitAll(fibers)
    end <- IO.realTime
  } yield {
    println(s"Time: ${end - start}")
    println(s"Results: $results")
    ExitCode.Success
  }

  def startAll[T](tasks: Seq[IO[T]]): IO[Seq[FiberIO[T]]] = {
    tasks match {
      case Seq() => IO.pure(Seq.empty)
      case Seq(t) => t.start.map(Seq(_))
      case a :: b :: other =>
        for {
          fa <- a.start
          fb <- b.start
          fo <- startAll(other)
        } yield Seq(fa, fb) ++ fo
    }
  }

  def awaitAll[T](fibers: Seq[FiberIO[T]]): IO[Seq[T]] = {
    fibers match {
      case Seq() => IO.pure(Seq.empty)
      case Seq(f) =>
        for {
          outcome <- f.joinWithNever
        } yield Seq(outcome)
      case fa :: fb :: fs =>
        for {
          ra <- fa.joinWithNever
          rb <- fb.joinWithNever
          ro <- awaitAll(fs)
        } yield Seq(ra, rb) ++ ro
    }
  }

  def worker(id: Int): IO[Int] = for {
    _ <- IO.sleep(1.second)
    _ = println(s"worker $id started")
  } yield id * 2
}

