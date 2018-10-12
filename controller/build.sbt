name := "controller"

version := "0.1"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies += "net.cakesolutions" %% "scala-kafka-client-akka" % "2.0.0"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.16",
//  "com.typesafe.akka" %% "akka-testkit" % "2.5.16" % Test
  "io.skuber" %% "skuber" % "2.0.11"
)

scalaVersion := "2.12.6"
