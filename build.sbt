import play.sbt.routes.RoutesCompiler.autoImport._

name := """shortURLs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "mysql" % "mysql-connector-java" % "5.1.36",
  "commons-validator" % "commons-validator" % "1.4.0"
)

routesGenerator := InjectedRoutesGenerator


