package steve

import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import sttp.tapir.client.http4s.Http4sClientInterpreter
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  val logger = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = EmberClientBuilder
    .default[IO]
    .build
    .use { client =>
      given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()

      val exec = ClientSideExecutor.instance[IO](client)

      logger.info("Building base image") *>
        exec
          .build(Build.empty)
          .flatTap(hash => logger.info("Build image with hash: " + hash))
          .flatMap(exec.run)
          .flatMap(result => logger.info("Run image with hash: " + result))
    }
    .orElse(logger.error("Unhandled error"))

}
