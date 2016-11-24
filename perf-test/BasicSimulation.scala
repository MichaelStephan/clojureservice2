import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicSimulation extends Simulation {
  val httpConf = http 
    .baseURL("http://localhost:6667") 

  val scn = scenario("BasicSimulation")
    .repeat(1000) {
      exec(http("request_1")
      .get("/hello/test"))}

  setUp(
    scn.inject(atOnceUsers(80))
  ).protocols(httpConf)
}
