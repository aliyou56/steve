ThisBuild / scalaVersion                        := "3.1.1"
ThisBuild / versionScheme                       := Some("early-semver")
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

Global / onChangedBuildSource := ReloadOnSourceChanges

val Versions =
  new {
    val catsEffect = "3.3.8"
    val tapir      = "0.20.1"
    val http4s     = "0.23.11" // 1.0.0-M31
    val munit      = "1.0.0-M1"
    val munitCats  = "1.0.7"
    val logback    = "1.2.11"
    val monocle    = "3.1.0"
  }

val commonSettings = Seq(
  // scalacOptions -= "-Werror",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,
    // "org.typelevel" %% "cats-mtl" % "1.2.1",
    "org.typelevel" %% "munit-cats-effect-3" % Versions.munitCats % Test,
//    compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.13" cross CrossVersion.full),
  ),
  testFrameworks += new TestFramework("munit.Framework"),
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

def full(p: Project) = p % "test->test;compile->compile"

lazy val root = project
  .in(file("."))
  .settings(
    publish        := {},
    publish / skip := true,
  )
  .aggregate(server, client, shared, e2e)

lazy val e2e = project
  .in(file("e2e"))
  .settings(commonSettings)
  .dependsOn(full(server), full(client))

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
      "dev.optics"                  %% "monocle-core"        % Versions.monocle,
      "org.http4s"                  %% "http4s-circe"        % Versions.http4s % Test,
      "org.http4s"                  %% "http4s-client"       % Versions.http4s % Test,
      "org.scalameta"               %% "munit-scalacheck"    % Versions.munit  % Test,
    ),
  )
  .dependsOn(full(shared))

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
  .dependsOn(full(shared))
