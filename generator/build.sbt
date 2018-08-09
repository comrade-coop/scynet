name := "TradingBotBreeder"

version := "1.0"

scalaVersion := "2.12.1"

lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion    = "2.5.6"
lazy val gattakkaProject = RootProject(uri("git://github.com/obecto/gattakka.git#6055c81d63275c8fa8263b7eb623ac7fb93ab862"))

lazy val root = (project in file(".")).dependsOn(gattakkaProject)
  .settings(
    inThisBuild(List(
      organization    := "com.obecto",
      scalaVersion    := "2.12.2"
    )),
    name := "trading-bot-breeder",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "io.spray" %%  "spray-json" % "1.3.3",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"         % "3.0.1"         % Test
    )
  )
