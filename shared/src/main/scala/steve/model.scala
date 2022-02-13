package steve

import io.circe.Codec

enum Command {
  case Build(build: steve.Build)
  case Run(hash: Hash)
}

final case class Build(
  base: Build.Base,
  commands: List[Build.Command],
) derives Codec.AsObject

object Build {

  enum Base derives Codec.AsObject {
    case EmptyImage
    case ImageReference(hash: Hash)
  }

  enum Command derives Codec.AsObject {
    case Upsert(key: String, value: String)
    case Delete(key: String)
  }

  val empty: Build = Build(base = Build.Base.EmptyImage, commands = Nil)
}

final case class Hash(value: Vector[Byte]) derives Codec.AsObject

final case class SystemState(all: Map[String, String]) derives Codec.AsObject
