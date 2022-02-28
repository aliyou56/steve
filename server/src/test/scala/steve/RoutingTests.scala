package steve

import cats.effect.IO
import cats.implicits.*
import io.circe.Json
import munit.CatsEffectSuite
import org.http4s.Method.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.Client
import org.http4s.client.dsl.io.*
import org.http4s.implicits.*

class RoutingTests extends CatsEffectSuite {

  val exec: Client[IO] = Client.fromHttpApp(
    Routing.instance(
      TestExecutor.instance(
        Map.empty,
        Map(Hash(Vector(10, 40)) -> SystemState(all = Map("K" -> "V")).asRight),
      )
    )
  )

  def toJson(str: String): Json = io.circe.parser.parse(str).toOption.get

  test("POST /api/run") {
    val input = toJson("""
        {
          "value" : [10, 40]
        }
        """)

    val output = toJson("""
        {
          "all": {
            "K": "V"
          }
        }
        """)

    assertIO(
      exec.expect[Json](
        POST.apply(input, uri"/api/run")
      ),
      output,
    )
  }

}
