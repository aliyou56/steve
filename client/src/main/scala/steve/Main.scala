package steve

import cats.effect.IO
import cats.effect.IOApp
import org.http4s.ember.client.EmberClientBuilder
import sttp.tapir.client.http4s.Http4sClientInterpreter

object Main extends IOApp.Simple {

  override def run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()

      val exec = ClientSideExecutor.instance[IO](client)

      exec
        .build(Build.empty)
        .flatMap(exec.run)
        .flatMap(IO.println)
    }

}
