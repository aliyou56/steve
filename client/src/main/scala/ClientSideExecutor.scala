package steve

object ClientSideExecutor {

  def instance[F[_]]: Executor[F] =
    new Executor[F] {
      override def build(build: Build): F[Hash]    = ???
      override def run(hash: Hash): F[SystemState] = ???
    }

}
