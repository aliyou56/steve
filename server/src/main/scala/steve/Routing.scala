package steve

import cats.effect.Async
import cats.effect.IO
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import org.http4s.HttpApp
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Routing {

  def instance[F[_]: Async](exec: Executor[F]): HttpApp[F] = {
    val endpoints: List[ServerEndpoint[Any, F]] = List(
      protocol.build.serverLogicRecoverErrors(exec.build),
      protocol.run.serverLogicSuccess(exec.run),
    )

    Http4sServerInterpreter[F](
      Http4sServerOptions
        .customInterceptors[F, F]
        .exceptionHandler { _ =>
          Some(
            ValuedEndpointOutput(
              jsonBody[GenericServerError].and(statusCode(StatusCode.InternalServerError)),
              GenericServerError("server error"),
            )
          )
        }
        .options
    )
      .toRoutes(endpoints)
      .orNotFound
  }

}
