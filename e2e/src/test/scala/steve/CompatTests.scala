package steve

import cats.effect.IO
import cats.implicits.*
import org.http4s.client.Client
import sttp.tapir.client.http4s.Http4sClientInterpreter

class CompatTests extends munit.CatsEffectSuite {

  val goodBuild: Build              = Build.empty
  val goodBuildResult: Hash         = Hash(Vector.empty)
  val unexpectedFailingBuild: Build = Build(Build.Base.EmptyImage, List(Build.Command.Delete("a")))

  val unknownHash: Hash = Hash(Vector(1))

  val unknownBaseBuild: Build = Build(
    Build.Base.ImageReference(unknownHash),
    Nil,
  )

  val unknownBaseError: Throwable = Build.Error.UnknownBase(unknownHash)

  val goodHash: Hash              = Hash(Vector.empty)
  val goodRunResult: SystemState  = SystemState(Map.empty)
  val unexpectedFailingHash: Hash = Hash(Vector(3))

  val exec: Executor[IO] = TestExecutor.instance(
    Map(
      goodBuild              -> goodBuildResult.asRight,
      unknownBaseBuild       -> unknownBaseError.asLeft,
      unexpectedFailingBuild -> new Throwable("build internal error").asLeft,
    ),
    Map(
      goodHash              -> goodRunResult.asRight,
      unexpectedFailingHash -> new Throwable("hash internal error").asLeft,
    ),
  )

  given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()

  val client: Executor[IO] = ClientSideExecutor.instance[IO](
    Client.fromHttpApp(
      Routing.instance[IO](
        exec
      )
    )
  )

  test("Build image - success") {
    assertIO(
      client.build(goodBuild),
      goodBuildResult,
    )
  }

  test("Build image - unknown base") {
    assertIO(
      client.build(unknownBaseBuild).attempt,
      unknownBaseError.asLeft,
    )
  }

  test("Build image - unexpected error") {
    assertIO(
      client.build(unexpectedFailingBuild).attempt,
      GenericServerError("server error").asLeft,
    )
  }

  test("Run image - success") {
    assertIO(
      client.run(goodHash),
      goodRunResult,
    )
  }

  test("Run image - unexpected error") {
    assertIO(
      client.run(unexpectedFailingHash).attempt,
      GenericServerError("server error").asLeft,
    )
  }

}
