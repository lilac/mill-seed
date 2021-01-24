// build.sc
import mill._, scalalib._

object core extends SbtModule {
  def scalaVersion = "2.13.1"
  def akkaVersion = "2.6.11"

  override def mainClass: T[Option[String]] = Some("Main")

  override def ivyDeps = Agg(
    ivy"org.typelevel::cats-effect::2.3.1",
    ivy"com.typesafe.akka::akka-actor:$akkaVersion",
    ivy"com.typesafe.akka::akka-persistence:$akkaVersion"
  )

  object test extends Tests{
    override def ivyDeps = Agg(
      ivy"com.lihaoyi::utest::0.7.7",
      ivy"com.typesafe.akka::akka-testkit:$akkaVersion"
    )
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

