package steve

import cats.effect.Async
import cats.effect.IO
import org.http4s.HttpApp
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Routing {

  def instance[F[_]: Async](exec: Executor[F]): HttpApp[F] = {
    val endpoints: List[ServerEndpoint[Any, F]] = List(
      protocol.build.serverLogicSuccess(exec.build),
      protocol.run.serverLogicSuccess(exec.run),
    )

    Http4sServerInterpreter[F]()
      .toRoutes(endpoints)
      .orNotFound
  }

}
