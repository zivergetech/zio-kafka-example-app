name := "zio-kafka-example-app"

organization := "com.ziverge"

scalaVersion := "2.12.11"

libraryDependencies += "dev.zio" %% "zio-kafka" % "0.8.0"

run / fork := true
