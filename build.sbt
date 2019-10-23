import Dependencies._

// Global build settings
ideaBuild in ThisBuild := Version.ideaVersion
onLoad in Global := ((s: State) => { "updateIdea" :: s}) compose (onLoad in Global).value

lazy val commonSettings = Seq(
  version := "1.0.4b",
  scalaVersion := "2.12.9",
  scalacOptions := Seq(
    "-encoding", "utf8",
    "-deprecation",
    "-Xlint"
  ),
  ideaInternalPlugins := Seq(
    "git4idea"
  ),
  mainClass in (Compile, run) := Some("com.intellij.idea.Main"),
  // Add tools.jar, from https://stackoverflow.com/a/12508163/348497
  unmanagedJars in Compile ~= {uj =>
    Attributed.blank(file(System.getProperty("java.home").dropRight(3) + "lib/tools.jar")) +: uj
  },
  fork in run := true,
  javaOptions in run := Seq(
    "-ea", // enable Java assertions
    s"-Didea.home.path=${ideaBaseDirectory.value}",
  )
)

lazy val root = (project in file("."))
  .enablePlugins(SbtIdeaPlugin)
  .settings(commonSettings)
  .settings(
    name := "Topias",
    libraryDependencies ++= topiasDependencies
  )