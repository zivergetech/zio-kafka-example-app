package com.ziverge

import zio._
import zio.kafka.consumer.{ CommittableRecord, OffsetBatch }
import zio.stream._

package object chunking {
  type Chunking = Has[Chunking.Service]

  object Chunking {
    trait Service {
      def writeRecords[R](
        stream: ZStream[R, Throwable, CommittableRecord[String, String]]):
        ZStream[R, Throwable, OffsetBatch]
    }

    def writeRecords[R](
      stream: ZStream[R, Throwable, CommittableRecord[String, String]]):
      ZStream[R with Chunking, Throwable, OffsetBatch] =
      ZStream.accessStream(_.get[Service].writeRecords(stream))

    def live(filePrefix: String): ZLayer[Any, Nothing, Chunking] =
      ZLayer.fromEffect(Ref.make(0).map(new Live(filePrefix, _)))
  }
}
