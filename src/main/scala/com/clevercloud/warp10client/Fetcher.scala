package com.clevercloud.warp10client

import java.util.UUID

import scala.util.{Failure, Success, Try}

import akka.NotUsed
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

import com.clevercloud.warp10client.models._
import com.clevercloud.warp10client.models.gts_module.GTS

object Fetcher {
  def fetch(readToken: String)(
    implicit warpClientContext: WarpClientContext
  ): Flow[Query[FetchRange], Seq[GTS], NotUsed] = {
    val uuid = UUID.randomUUID
    Flow[Query[FetchRange]]
      .map(query => fetchRequest(readToken, query))
      .map(request => (request -> uuid)) // cf. https://doc.akka.io/docs/akka-http/current/client-side/host-level.html
      .via(warpClientContext.poolClientFlow)
      .filter({ case (_, key) => key == uuid })
      .map({ case (responseTry, _) => responseTry })
      .via(processResponseTry)
      .via(byteStringToGTSSeq)
  }

  def rangedFetch(
    readToken: String,
    query: Query[StartStopRangeMicros],
    batchSize: Int = 1000,
    limit: Int,
    size: Int = 0
  )(
    implicit warpClientContext: WarpClientContext
  ): Flow[Query[StartStopRangeMicros], Seq[GTS], NotUsed] = {
    val uuid = UUID.randomUUID
    Flow[Query[StartStopRangeMicros]]
      .map(query => query.copy(range = RecordsSinceMicros(query.range.newestDate, batchSize)))
      .map(query => fetchRequest(readToken, query))
      .map(request => (request -> uuid)) // cf. https://doc.akka.io/docs/akka-http/current/client-side/host-level.html
      .via(warpClientContext.poolClientFlow)
      .filter({ case (_, key) => key == uuid })
      .map({ case (responseTry, _) => responseTry })
      .via(processResponseTry)
      .via(byteStringToGTSSeq)
      .map { gtsSeq => gtsSeq.map { _.filter(maxDate = query.range.oldestDate) }} // remove exceeded gtsPoint
      .flatMapConcat { gtsSeq: Seq[GTS] =>
        val newSize = size + gtsSeq.map { gts: GTS => gts.points.size }.sum
        if (gtsSeq.isEmpty) {
          Source.single(gtsSeq)
        } else {
          val currentOldestGTSPoint = gtsSeq.maxBy(_.oldestDate)
          val newNow = currentOldestGTSPoint.points.max.ts.get - 1
          if (newNow > query.range.oldestDate && newSize <= limit) {
            val newQuery = query.copy(range = StartStopRangeMicros(newNow, query.range.oldestDate))
            Source
              .single(newQuery)
              .via(rangedFetch(readToken, newQuery, batchSize, limit, newSize))
              .flatMapConcat(newGtsSeq => {
                val globalSeq = gtsSeq ++ newGtsSeq
                Source.single(globalSeq.take(limit))
              })
          } else {
            Source.single(gtsSeq.take(limit))
          }
        }
      }
  }

  def fetchRequest(readToken: String, query: Query[FetchRange])(
    implicit warpClientContext: WarpClientContext
  ) = {
    HttpRequest(
      method = HttpMethods.GET,
      uri = warpClientContext.configuration.fetchUrl + "?" + query.serialize,
      headers = List(`X-Warp10-Token`(readToken))
    )
  }

  def processResponseTry(
    implicit warpClientContext: WarpClientContext
  ): Flow[Try[HttpResponse], ByteString, NotUsed] = {
    import warpClientContext._

    Flow[Try[HttpResponse]]
      .flatMapConcat {
        case Success(httpResponse) => {
          if (httpResponse.status == StatusCodes.OK) {
            httpResponse.entity.dataBytes
          } else {
            Source.fromFuture(
              WarpClientUtils
                .readAllDataBytes(httpResponse.entity.dataBytes)
                .map(content => WarpException(
                  httpResponse.status.intValue,
                  content
                ))
                .map(throw _)
            )
          }
        }
        case Failure(e) => throw e
      }
  }

  def byteStringToGTSSeq: Flow[ByteString, Seq[GTS], NotUsed] = {
    Flow[ByteString]
      .via(WarpClientUtils.decode)
      .map(GTS.parse)
      .map(_.right.get)
  }
}