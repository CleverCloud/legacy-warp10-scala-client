package com.clevercloud.warp10client

import java.util.UUID

import scala.util.{Failure, Success, Try}

import akka.NotUsed
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Source}

import io.circe._
import io.circe.parser._

import com.clevercloud.warp10client.models.gts_module.Coordinates
import com.clevercloud.warp10client.models.gts_module.GTS
import com.clevercloud.warp10client.models.gts_module.GTSValue

object Executioner {
  type WarpScript = String

  def exec()(
    implicit warpClientContext: WarpClientContext
  ): Flow[WarpScript, Seq[GTS], NotUsed] = {
    val uuid = UUID.randomUUID
    Flow[WarpScript]
      .map(script => execRequest(script))
      .map(request => (request -> uuid)) // cf. https://doc.akka.io/docs/akka-http/current/client-side/host-level.html
      .via(warpClientContext.poolClientFlow)
      .filter({ case (_, key) => key == uuid })
      .map({ case (responseTry, _) => responseTry })
      .via(processResponseTry)
      .via(jsonToGTSSeq)
  }

  def execRequest(script: WarpScript)(
    implicit warpClientContext: WarpClientContext
  ) = {
    HttpRequest(
      method = HttpMethods.POST,
      uri = warpClientContext.configuration.execUrl,
      entity = HttpEntity(script)
    )
  }

  def processResponseTry(
    implicit warpClientContext: WarpClientContext
  ): Flow[Try[HttpResponse], String, NotUsed] = {
    import warpClientContext._

    Flow[Try[HttpResponse]]
      .flatMapConcat {
        case Success(httpResponse) => {
          if (httpResponse.status == StatusCodes.OK) {
            Source.fromFuture(
              WarpClientUtils
                .readAllDataBytes(httpResponse.entity.dataBytes)
            )
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

  private def parseJson(): Flow[String, Json, NotUsed] = {
    Flow[String]
      .map { s =>
        parse(s) match {
          case Right(json) => json
          case Left(e) => throw WarpException(500, e.toString)
        }
      }
  }

  def jsonToGTSSeq(): Flow[String, Seq[GTS], NotUsed] = {
    Flow[String]
      .via(parseJson)
      .map { json => json.hcursor.downArray } // warp response contains [[]] so we drop an array level
      .map { array => { // global array with all matching script
        array.values.getOrElse(throw WarpException(500, "No data to process.")).map { series => { // http://www.warp10.io/apis/gts-output-format/
          val `class` = series.hcursor.getOrElse[String]("c")("").right.getOrElse(throw WarpException(500, "Can't parse `class` value."))
          val labels = series.hcursor.getOrElse[Map[String, String]]("l")(Map.empty[String, String]).right.getOrElse(throw WarpException(500, "Cant parse `labels` value."))

          (series \\ "v").map { seriesContentArrays => // [[point_1], [point_2], ...]
            seriesContentArrays.asArray.get.map { point => { // [point_i]
              point.asArray.get match { // [timestamp, lat, lon, elev, value] is point's content

                case Vector(timestamp: Json, value: Json) => {
                  GTS(
                    timestamp.asNumber.get.toLong,
                    None,
                    None,
                    `class`,
                    labels,
                    GTSValue.parseJson(value.asString.getOrElse(""))
                  )
                }
                case Vector(timestamp: Json, elevation: Json, value: Json) => {
                  GTS(
                    timestamp.asNumber.get.toLong,
                    None,
                    elevation.asNumber.get.toLong,
                    `class`,
                    labels,
                    GTSValue.parseJson(value.asString.getOrElse(""))
                  )
                }
                case Vector(timestamp: Json, latitude: Json, longitude: Json, value: Json) => {
                  GTS(
                    timestamp.asNumber.get.toLong,
                    Some(Coordinates(latitude.asNumber.get.toDouble, longitude.asNumber.get.toDouble)),
                    None,
                    `class`,
                    labels,
                    GTSValue.parseJson(value.asString.getOrElse(""))
                  )
                }
                case Vector(timestamp: Json, latitude: Json, longitude: Json, elevation: Json, value: Json) => {
                  GTS(
                    timestamp.asNumber.get.toLong,
                    Some(Coordinates(latitude.asNumber.get.toDouble, longitude.asNumber.get.toDouble)),
                    elevation.asNumber.get.toLong,
                    `class`,
                    labels,
                    GTSValue.parseJson(value.asString.getOrElse(""))
                  )
                }
              }
            }
          }
        }
      }}}}
      .map(_.flatten.flatten.toSeq)
  }
}
