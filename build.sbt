name := "zio-kafka-example-app"

organization := "com.ziverge"

scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-kafka" % "0.8.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.13.3",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.3",
  "com.lmax" % "disruptor" % "3.4.2"
)

run / fork := true
