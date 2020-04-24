package com.ziverge

import com.ziverge.chunking._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer._, zio.kafka.serde._

object ExampleApp extends App {
  def createRecordChunkingStream(topic: String) =
    Consumer
      .subscribeAnd(Subscription.topics(topic))
      .plainStream(Serde.string, Serde.string)
      .flattenChunks
      .via(Chunking.writeRecords(_))
      .mapM(_.commit)
      .provideSomeLayer[Clock with Blocking with Chunking](
        Consumer.make(ConsumerSettings(List("localhost:9092")).withGroupId(s"${topic}-group"))
      )
      .foreachManaged(_ => ZIO.unit)
      .fork

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      first <- createRecordChunkingStream("first")
      second <- createRecordChunkingStream("second")
    } yield ZIO.raceAll(first.join, List(second.join)))
      .use(identity)
      .provideCustomLayer(Chunking.live("/tmp/data"))
      .foldM(err => UIO(println(err)).as(1), _ => UIO.succeed(0))
}
