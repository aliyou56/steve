ThisBuild / scalaVersion                        := "3.1.1"
ThisBuild / versionScheme                       := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

Global / onChangedBuildSource := ReloadOnSourceChanges

val commonSettings = Seq(
  // scalacOptions -= "-Werror",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.5",
    // "org.typelevel" %% "cats-mtl" % "1.2.1",
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7",
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    publish        := {},
    publish / skip := true,
  )
  .aggregate(server, client, shared)

lazy val shared = project.settings(
  commonSettings,
  libraryDependencies ++= Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core"       % "0.20.0-M9",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "0.20.0-M9",
  ),
)

lazy val server = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "0.20.0-M9",
      "org.http4s"                  %% "http4s-dsl"          % "0.23.9", // 1.0.0-M31
      "org.http4s"                  %% "http4s-ember-server" % "0.23.9",
      "ch.qos.logback"               % "logback-classic"     % "1.2.10",
    ),
  )
  .dependsOn(shared)

lazy val client = project.settings(commonSettings).dependsOn(shared)
