package steve

import cats.Id
import cats.catsInstancesForId
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import org.scalacheck.Prop.secure
import ResolvedBuild.Command.*

class InterpreterTests extends ScalaCheckSuite {
  val interpreter: Interpreter[Id] = Interpreter.instance[Id]

  given Arbitrary[SystemState] = Arbitrary(Gen.resultOf(SystemState.apply))

  given Arbitrary[ResolvedBuild.Command] = Arbitrary {
    Gen.oneOf(
      Gen.resultOf(Upsert.apply),
      Gen.resultOf(Delete.apply),
    )
  }

  property("any system + upsert => the key in the system has the given value") {
    forAll { (system: SystemState, key: String, value: String) =>
      val build = ResolvedBuild(system, List(ResolvedBuild.Command.Upsert(key, value)))

      assertEquals(
        interpreter.interpret(build).all.get(key),
        Some(value),
      )
    }
  }

  property("any system + delete => the key is missing") {
    forAll { (system: SystemState, key: String, value: String) =>
      val build = ResolvedBuild(system, List(ResolvedBuild.Command.Delete(key)))

      assertEquals(
        interpreter.interpret(build).all.get(key),
        None,
      )
    }
  }

  property("upsert(k, v) + delete(k) => delete(k)") {
    forAll { (system: SystemState, key: String, value: String) =>
      val build  = ResolvedBuild(system, List(Upsert(key, value), Delete(key)))
      val build2 = ResolvedBuild(system, List(Delete(key)))

      assertEquals(
        interpreter.interpret(build),
        interpreter.interpret(build2),
      )
    }
  }

  property("upsert(k, v1) + upsert(k, v2) == upsert(k, v2)") {
    forAll { (system: SystemState, key: String, value1: String, value2: String) =>
      val build  = ResolvedBuild(system, List(Upsert(key, value1), Upsert(key, value2)))
      val build2 = ResolvedBuild(system, List(Upsert(key, value2)))

      assertEquals(
        interpreter.interpret(build),
        interpreter.interpret(build2),
      )
    }
  }

  property("split build results in the same state as the combined build") {
    forAll {
      (
        system: SystemState,
        commands: List[ResolvedBuild.Command],
        moreCommands: List[ResolvedBuild.Command],
      ) =>
        val lhs = {
          val sys1 = interpreter.interpret(ResolvedBuild(system, commands))
          interpreter.interpret(ResolvedBuild(sys1, moreCommands))
        }

        val rhs = interpreter.interpret(ResolvedBuild(system, commands ++ moreCommands))

        assertEquals(lhs, rhs)
    }
  }
}
