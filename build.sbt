ThisBuild / scalaVersion                        := "3.1.1"
ThisBuild / versionScheme                       := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions =
  new {
    val catsEffect = "3.3.5"
    val tapir      = "0.20.0-M9"
    val http4s     = "0.23.9" // 1.0.0-M31
    val munit      = "1.0.7"
    val logback    = "1.2.10"
  }

val commonSettings = Seq(
  // scalacOptions -= "-Werror",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,
    // "org.typelevel" %% "cats-mtl" % "1.2.1",
    "org.typelevel" %% "munit-cats-effect-3" % Versions.munit % Test,
  )
)

val nativeImageSettings = Seq(
  Compile / mainClass := Some("steve.Main"),
  nativeImageVersion  := "21.2.0",
  nativeImageOptions ++= Seq(
    s"-H:ReflectionConfigurationFiles=${(Compile / resourceDirectory).value / "reflect-config.json"}",
    "-H:+ReportExceptionStackTraces",
    "--no-fallback",
    "--allow-incomplete-classpath",
  ),
  nativeImageReady := { () => () }, // remove the alert message (macOs only)
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
    "com.softwaremill.sttp.tapir" %% "tapir-core"       % Versions.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % Versions.tapir,
  ),
)

lazy val server = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.tapir,
      "org.http4s"                  %% "http4s-dsl"          % Versions.http4s,
      "org.http4s"                  %% "http4s-ember-server" % Versions.http4s,
      "ch.qos.logback"               % "logback-classic"     % Versions.logback,
    ),
  )
  .dependsOn(shared)

lazy val client = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % Versions.tapir,
      "org.http4s"                  %% "http4s-ember-client" % Versions.http4s,
      "ch.qos.logback"               % "logback-classic"     % Versions.logback,
    ),
    nativeImageSettings,
  )
  .enablePlugins(NativeImagePlugin)
  .dependsOn(shared)
