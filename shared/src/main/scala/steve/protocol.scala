package steve

object protocol {
  import sttp.tapir._
  import sttp.tapir.json.circe._
  import sttp.tapir.generic.auto._

  private val base: Endpoint[Unit, Unit, Nothing, Unit, Any] = infallibleEndpoint.in("api")

  val build: Endpoint[Unit, Build, Nothing, Hash, Any] = base
    .put
    .in("build")
    .in(jsonBody[Build])
    .out(jsonBody[Hash])

  val run: Endpoint[Unit, Hash, Nothing, SystemState, Any] = base
    .post
    .in("run")
    .in(jsonBody[Hash])
    .out(jsonBody[SystemState])

}
