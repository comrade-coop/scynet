
name := "kafka-distribution-lastSeenBalance"

version := "0.1"

scalaVersion := "2.12.7"

val repositories = Seq(
  "confluent" at "http://packages.confluent.io/maven/",
  Resolver.sonatypeRepo("public")
)

resolvers ++= repositories


sourceGenerators in Compile += (avroScalaGenerate in Compile).taskValue

libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.0.1"
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.0.1"

libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.3"
libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar"))
libraryDependencies += "io.confluent" % "kafka-streams-avro-serde" % "5.0.0"
//libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "5.0.0"
