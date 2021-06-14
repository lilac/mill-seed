import cats.effect.{FiberIO, IO}

object IOExtension {
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
          ro <- IOExtension.awaitAll(fs)
        } yield ra :: rb :: ro
    }
  }

  def startAll[T](tasks: List[IO[T]]): IO[List[FiberIO[T]]] = {
    tasks match {
      case Nil => IO.pure(List.empty)
      case t :: Nil => t.start.map(List(_))
      case a :: b :: other =>
        for {
          fa <- a.start
          fb <- b.start
          fo <- IOExtension.startAll(other)
        } yield fa :: fb :: fo
    }
  }
}
