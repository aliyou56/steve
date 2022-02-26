package steve

import cats.effect.IO
import sttp.tapir.client.http4s.Http4sClientInterpreter
import org.http4s.client.Client
import cats.implicits.*

class CompatTests extends munit.CatsEffectSuite {

  def testExecutor(
    buildImpl: Map[Build, Either[Throwable, Hash]],
    runImpl: Map[Hash, Either[Throwable, SystemState]],
  ): Executor[IO] =
    new Executor[IO] {
      override def build(build: Build): IO[Hash]    = buildImpl(build).liftTo[IO]
      override def run(hash: Hash): IO[SystemState] = runImpl(hash).liftTo[IO]
    }

  val goodBuild: Build           = Build.empty
  val goodBuildResult: Hash      = Hash(Vector.empty)
  val goodHash: Hash             = Hash(Vector.empty)
  val goodRunResult: SystemState = SystemState(Map.empty)

  val exec: Executor[IO] = testExecutor(
    Map(goodBuild -> goodBuildResult.asRight),
    Map(goodHash  -> goodRunResult.asRight),
  )

  given Http4sClientInterpreter[IO] = Http4sClientInterpreter[IO]()

  val client: Executor[IO] = ClientSideExecutor.instance[IO](
    Client.fromHttpApp(
      Routing.instance[IO](
        exec
      )
    )
  )

  test("Build image successfully") {
    assertIO(
      client.build(Build.empty),
      goodBuildResult,
    )
  }

  test("Run image successfully") {
    assertIO(
      client.run(goodHash),
      goodRunResult,
    )
  }
}
