package steve

import com.comcast.ip4s.host
import com.comcast.ip4s.port
import cats.effect.IO
import cats.effect.IOApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp {
        Routing.instance[IO](ServerSideExecutor.module[IO])
      }
      .build
      .useForever

}
