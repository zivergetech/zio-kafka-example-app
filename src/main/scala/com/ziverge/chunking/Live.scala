package com.ziverge.chunking

import zio._
import zio.kafka.consumer.{ CommittableRecord, OffsetBatch }
import zio.stream._

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }

class Live(filePrefix: String, writtenFiles: Ref[Int]) extends Chunking.Service {
  val batchRecords = ZSink.foldWeighted(List[String]())(
    (rec: CommittableRecord[String, String]) => rec.value.length,
      16384) { (acc, el) =>
        el.record.value :: acc
      }.map(_.reverse.mkString("\n").getBytes(StandardCharsets.UTF_8))

  val batchOffsets = ZSink.foldLeft(OffsetBatch.empty) { 
    (acc, rec: CommittableRecord[String, String]) => 
      acc.merge(rec.offset) 
  }

  def writeRecords[R](
    stream: ZStream[R, Throwable, CommittableRecord[String, String]]): 
    ZStream[R, Throwable, OffsetBatch] = 
    stream
      .aggregate(batchOffsets zipPar batchRecords)
      .mapM { case (offsets, data) =>
        for {
          fileIndex <- writtenFiles.updateAndGet(_ + 1)
          _ <- Task {
                 Files.write(
                   Paths.get(filePrefix, s"chunk-$fileIndex"),
                   data
                 )
               }
        } yield offsets
      }
}
