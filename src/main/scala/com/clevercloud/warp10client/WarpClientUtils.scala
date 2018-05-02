package com.clevercloud.warp10client

import java.util.UUID

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

import com.clevercloud.warp10client.models.WarpConfiguration
import com.clevercloud.warp10client.WarpClientUtils.PoolClientFlow

object WarpClientUtils {
  type PoolClientFlow = Flow[(HttpRequest, UUID), (Try[HttpResponse], UUID), _]

  def readAllDataBytes(dataBytesSource: Source[ByteString, _])(implicit actorMaterializer: ActorMaterializer): Future[String] = {
    implicit val executionContext = actorMaterializer.system.dispatcher
    dataBytesSource
      .runFold(ByteString.empty) {
        case (seq, item) => seq ++ item
      }
      .map(_.decodeString("UTF-8"))
  }

  def getLines: Flow[ByteString, String, NotUsed] = {
    Flow[ByteString]
      .map(_.decodeString("UTF-8"))
      .intersperse("", "", "\n")
      .scan("") { 
        case (seq, item) => {
          if (seq.endsWith("\n")) {
            item
          } else if (seq.contains("\n")) {
            seq.substring(seq.lastIndexOf("\n") + 1) + item
          } else {
            seq + item
          }
        }
      }
      .filter(_.contains("\n")) // mandatory because of gts serialization style
      .map(segment => segment.substring(0, segment.lastIndexOf("\n")))
      .mapConcat(_.split("\n").to[immutable.Iterable])
      .map(line => if (line.startsWith("\r")) line.drop(1) else line)
      .map(line => if (line.endsWith("\r")) line.dropRight(1) else line)
  }
}

case class WarpClientContext(
  configuration: WarpConfiguration,
  poolClientFlow: WarpClientUtils.PoolClientFlow,
  actorMaterializer: ActorMaterializer
) {
  implicit def implicitActorMaterializer: ActorMaterializer = actorMaterializer
  implicit def implicitActorSystem: ActorSystem = actorMaterializer.system
  implicit def implicitExecutionContext: ExecutionContext = actorMaterializer.system.dispatcher
  implicit def implicitPoolClientFlow: PoolClientFlow = poolClientFlow
  implicit def implicitWarpConfiguration: WarpConfiguration = configuration
}

case class WarpException(statusCode: Long, error: String) extends Exception(error)

object `X-Warp10-Token` {
  def apply(value: String): HttpHeader = {
    HttpHeader.parse("X-Warp10-Token", value) match {
      case ParsingResult.Ok(httpHeader, _) => httpHeader
      case ParsingResult.Error(error) => throw WarpException(-1, s"${error.summary}: ${error.detail}.")
    }
  }
}