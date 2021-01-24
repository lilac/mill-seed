import cats.effect.{ExitCode, IO, IOApp}
import akka.actor.Actor

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    println("hi")
    IO.pure(ExitCode.Success)
  }
}

