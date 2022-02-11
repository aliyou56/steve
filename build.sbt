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

lazy val shared = project.settings(commonSettings)

lazy val server = project.settings(commonSettings).dependsOn(shared)

lazy val client = project.settings(commonSettings).dependsOn(shared)
