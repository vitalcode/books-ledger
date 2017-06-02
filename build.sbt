enablePlugins(JavaAppPackaging)

name := "books-ledger"
organization := "uk.vitalcode"
version := "1.0"
scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.16"
  val akkaHttpV   = "10.0.1"
  val scalaTestV  = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",

    "com.rbmhtechnology" %% "eventuate-core" % "0.7.1",
    "com.rbmhtechnology" %% "eventuate-log-leveldb" % "0.7.1",

    "org.webjars" %% "webjars-play" % "2.5.0-2",
    "com.adrianhurt" %% "play-bootstrap" % "1.0-P25-B3",

    "org.json4s" %% "json4s-core" % "3.3.0",
    "org.json4s" %% "json4s-jackson" % "3.3.0",
    "org.json4s" %% "json4s-ext" % "3.3.0"
  )
}

Revolver.settings
