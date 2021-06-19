// build.sc
import mill._, scalalib._

object core extends SbtModule {
  def scalaVersion = "3.0.0"
  def akkaVersion = "2.6.11"

  override def mainClass: T[Option[String]] = Some("Main")

  override def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect::3.1.1".withDottyCompat(scalaVersion()),
    ivy"com.typesafe.akka::akka-actor:$akkaVersion".withDottyCompat(scalaVersion()),
    ivy"com.typesafe.akka::akka-persistence:$akkaVersion".withDottyCompat(scalaVersion())
  )

  object test extends Tests{
    override def ivyDeps = Agg(
      ivy"com.lihaoyi::utest::0.7.7".withDottyCompat(scalaVersion()),
      ivy"com.typesafe.akka::akka-testkit:$akkaVersion".withDottyCompat(scalaVersion())
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

