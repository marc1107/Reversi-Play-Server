name := """reversi-play-server"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
  // Add sbt-web and related plugins
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "sbt-plugin" % "2.8.x",
    "com.typesafe.sbt" % "sbt-web" % "1.4.4",
    "com.typesafe.sbt" % "sbt-less" % "1.1.0"
  ),
  // Ensure Node.js is used for asset compilation
  JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
)

includeFilter in (Assets, LessKeys.less) := "main.less" | "rules.less" | "game.less" | "playerturn.less" | "landing.less"

scalaVersion := "3.3.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
