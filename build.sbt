val ScalatraVersion = "2.6.2"

organization := "com.github.at1as"

name := "Remind Me"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

resolvers += Classpaths.typesafeReleases
//resolvers +=
//  "Tatami Releases" at "https://raw.github.com/cchantep/tatami/master/releases/"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "container",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "com.twilio.sdk" % "twilio" % "7.15.5",
  "com.typesafe" % "config" % "1.3.2"
  //"foorgol" %% "scala" % "1.0.5"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)

mainClass in (Compile, run) := Some("ScalatraBootstrap")
