name := "kafka-producer-blockchain"

version := "0.1"

scalaVersion := "2.12.6"


resolvers += "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala"

//libraryDependencies += "com.micronautics" %% "web3j-scala" % "0.3.1" withSources()

libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.0.0"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "5.0.0"
libraryDependencies += "com.sksamuel.avro4s" %% "avro4s-core" % "1.8.3"

libraryDependencies += "javax.ws.rs" % "javax.ws.rs-api" % "2.1" artifacts( Artifact("javax.ws.rs-api", "jar", "jar"))

libraryDependencies += "org.web3j" % "core" % "3.5.0"
libraryDependencies += "org.web3j" % "parity" % "3.5.0"

//val workaround = {
//  sys.props += "packaging.type" -> "jar"
//  ()
//}