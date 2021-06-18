import cats.effect.{FiberIO, IO}

object IOExtension {
  def awaitAll[T](fibers: List[FiberIO[T]]): IO[List[T]] = {
    var io: IO[List[T]] = IO.pure(List.empty)
    for (fiber <- fibers) {
      io = for {
        res <- io
        ra <- fiber.joinWithNever
      } yield ra :: res
    }
    io.map(_.reverse)
  }

  def startAll[T](tasks: List[IO[T]]): IO[List[FiberIO[T]]] = {
    var io: IO[List[FiberIO[T]]] = IO.pure(List.empty)
    for (task <- tasks) {
      io = for {
        fibers <- io
        fb <- task.start
      } yield fb :: fibers
    }
    io.map(_.reverse)
  }
}
