package steve

import cats.Applicative
import cats.implicits.*
import cats.data.State
import monocle.syntax.all.*

trait Interpreter[F[_]] {
  def interpret(build: ResolvedBuild): F[SystemState]
}

object Interpreter {
  def apply[F[_]](using F: Interpreter[F]): Interpreter[F] = F

  def instance[F[_]: Applicative]: Interpreter[F] =
    new Interpreter[F] {

      // State[S, A] = S => (S, A)
      private val transition: ResolvedBuild.Command => State[SystemState, Unit] = {
        case ResolvedBuild.Command.Upsert(k, v) => State.modify(_.focus(_.all).modify(_ + (k -> v)))
        case ResolvedBuild.Command.Delete(k)    => State.modify(_.focus(_.all).modify(_ - k))
      }

      def interpret(
        build: ResolvedBuild
      ): F[SystemState] = build.commands.traverse(transition).runS(build.base).value.pure[F]

    }

}
