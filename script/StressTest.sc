

// sql driver
// import $ivy.`com.datastax.oss:java-driver-core:4.5.1`
// import com.datastax.oss.driver.api.core.config.DriverConfigLoader
// import io.github.gatling.cql.checks.CqlCheck
// import io.github.gatling.cql.response.CqlResponse

import java.util
// gatling artifacts

// import $ivy.`io.github.gatling-cql:gatling-cql:3.3.1-2`
// import $ivy.`org.scala-lang.modules::scala-collection-compat:2.4.4`
// import $ivy.`dev.code-n-roll.gatling::jdbc-gatling:2.3.0`

// import dev.code_n_roll.gatling.jdbc.Predef._
// code imports
import scala.concurrent.duration._

// import com.datastax.oss.driver.api.core.{ConsistencyLevel, CqlSession}
// import io.github.gatling.cql.Predef._

// package test

val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
logger.setLevel(ch.qos.logback.classic.Level.INFO)

/**
 * load data and then run query test
 */
abstract class CassandraSimulation extends Simulation {
  val keyspace = "test"

  val table = "tbl_image"
  val host = "127.0.0.1" //"predictordb.service.tubi"
  val port = 4000
  val config = "application.conf"
  /* val session = CqlSession
    .builder()
    .addContactPoint(InetSocketAddress.createUnresolved(host, port))
    .withLocalDatacenter("us-east-2")
    .withConfigLoader(DriverConfigLoader.fromFile(new File(config)))
    .build()
  */
  val jdbcConfig = jdbc
    .url(s"jdbc:mysql://$host:$port/test")
    .username("root")
    .password("root")
    .driver("com.mysql.cj.jdbc.Driver")

  // val cqlConfig = cql.session(session)
  val userCount = Integer.getInteger("users", 20000).toInt

  //   val feeder = csv("staff_activities.csv").circular.convert {
  //     case (name, v) => v.toLong
  //   }
  val feeder = 1L.to(userCount).map(v => Map("name" -> v.toString)).circular

  /* before {
    val res = session.execute(
      """
        |CREATE TABLE IF NOT EXISTS tbl_image(
        |  target text,                   -- ContentTarget contentID
        |  version text,                  -- if Ranking is in release state, then "RELEASE", else Ranking.name
        |  ranking blob,                  -- Protobuf serialized RankingBundle with a single ranking
        |  PRIMARY KEY(target, version)
        |) WITH gc_grace_seconds = 86400;
        |""".stripMargin)
        .wasApplied()
      logger.info(s"create table: $res")
  } */


  // after(jdbc.close())
}

class QuerySimulation extends CassandraSimulation {
  // val query = s"SELECT * from $keyspace.$table where target = ? and version = 'RELEASE'"

  // val queryStm = session.prepare(query)
  val queryScene = scenario("query").repeat(1) {
    feed(feeder)
      .exec(
        jdbc("query")
          .select("*")
          .from(table)
          .where("target = ${name} and version = 'release'")
          .check(ResultSetCheck())
      )
  }
  val concurrency = 100
  setUp(
    queryScene.inject(constantUsersPerSec(concurrency).during((userCount / concurrency).second))
  ).protocols(jdbcConfig)
}

class UpdateSimulation extends CassandraSimulation {

  /* val updateStm = session.prepare(
    s"UPDATE $keyspace.${table} USING TTL 86400 SET ranking = ? WHERE target = ? and version = ?;"
  ) */

  val blob = new String(Array.fill(10 /* 24 * 1024 */)('a'.toByte))
  val updateFeeder = 1L.to(userCount).map(v => Map("name" -> v.toString, "blob" -> blob)).circular

  val updateScene = scenario("update")
    .feed(updateFeeder)
    .exec(
      jdbc("update")
        .insert()
        .into(s"${table} (target, version, ranking)")
        // .values("${name}, 'release', ${blob}")
        // above is not working since the jdbc-gatling does not support blob type.
        .values("${name}, 'release', 'blob'")
    )
  val concurrency = 100
  setUp(
    updateScene.inject(constantUsersPerSec(concurrency).during((userCount / concurrency).second))
  ).protocols(jdbcConfig)

}

case class ResultSetCheck() extends JdbcCheck[Map[String, Any]] {
  override def check(
                      response: List[Map[String, Any]],
                      session: Session,
                      preparedCache: util.Map[Any, Any]
                    ): Validation[CheckResult] = {
    // return Success(CheckResult(None, None))
    val results = response
    if (results.isEmpty) {
      Failure("query not exist")
    } else {
      val ranking = results.apply(0).get("ranking")
      // logger.info(s"Ranking: $ranking")
      ranking match {
        case None => Failure("No Ranking")
        case Some(blob: Array[Byte]) =>
          val size = blob.length
          if (size > 0 /* == 1024 * 1024 */ ) Success(CheckResult(Some(size), None))
          else Failure("Ranking size < 1MB")
        case Some(value) => Failure(s"Unknown value type: ${value}")
      }
    }
  }
}

@main
def main(name: String = "q", users: Int = 20000): Unit = {
  // run the actual simulation
  val className = name match {
    case _ if name.startsWith("u") => classOf[UpdateSimulation].getName
    case _ => classOf[QuerySimulation].getName
  }
  val props = new GatlingPropertiesBuilder()
    .simulationClass(className)
    .resultsDirectory("/tmp")
    .build

  System.setProperty("users", users.toString)
  logger.info(s"Users count: $users")
  Gatling.fromMap(props)
}
