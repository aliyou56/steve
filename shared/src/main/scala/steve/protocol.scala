package steve

import sttp.model.StatusCode

object protocol {
  import sttp.tapir._
  import sttp.tapir.json.circe._
  import sttp.tapir.generic.auto._

  private val base: PublicEndpoint[Unit, Nothing, Unit, Any] = infallibleEndpoint.in("api")

  val build: PublicEndpoint[Build, Build.Error, Hash, Any] = base
    .put
    .in("build")
    .in(jsonBody[Build])
    .out(jsonBody[Hash])
    .errorOut(statusCode(StatusCode.UnprocessableEntity).and(jsonBody[Build.Error]))

  val run: PublicEndpoint[Hash, Nothing, SystemState, Any] = base
    .post
    .in("run")
    .in(jsonBody[Hash])
    .out(jsonBody[SystemState])

}
