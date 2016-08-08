package com.clevercloud.warp10client
import org.http4s.client.blaze._
import org.http4s.{Request, Response, Method, Uri, Header, Headers}
import java.net.URL

sealed abstract class Warp10Value {
  def warp10Serialize:String
}
case class IntWarp10Value(value: Int) extends Warp10Value{
  def warp10Serialize = {value.toString()}
}
case class LongWarp10Value(value: Long) extends Warp10Value{
  def warp10Serialize = {value.toString()}
}
case class BooleanWarp10Value(value: Boolean) extends Warp10Value{
  def warp10Serialize = {value.toString()}
}
case class StringWarp10Value(value: String) extends Warp10Value{
  def warp10Serialize = {value.toString()}
}

case class Warp10GeoValue(lat:Double, lon:Double, elev:Option[Double]){
  def warp10Serialize = {
    lat.toString() + ":" + lon.toString() + elev.fold("/")("/" + _.toString())
  }
}


case class Warp10Data(dateTime:Long, geo:Option[Warp10GeoValue], name:String, labels:Set[(String, String)], value:Warp10Value){
  def urlEncode(s:String) = java.net.URLEncoder.encode(s, "utf-8")
  def warp10Serialize = {
    dateTime.toString + "/" + geo.fold("/")(g => g.warp10Serialize) + " " + urlEncode(name) + "{" + labels.map(kv => urlEncode(kv._1) + "=" + urlEncode(kv._2)) + "} " + value.warp10Serialize
  }
}

object Warp10Data {
  def apply(dateTime:Long, geo:Option[Warp10GeoValue], name:String, labels:Set[(String, String)], value:Int):Warp10Data = Warp10Data(dateTime, geo, name, labels, IntWarp10Value(value))
  def apply(dateTime:Long, geo:Option[Warp10GeoValue], name:String, labels:Set[(String, String)], value:Long):Warp10Data = Warp10Data(dateTime, geo, name, labels, LongWarp10Value(value))
  def apply(dateTime:Long, geo:Option[Warp10GeoValue], name:String, labels:Set[(String, String)], value:Boolean):Warp10Data = Warp10Data(dateTime, geo, name, labels, BooleanWarp10Value(value))
  def apply(dateTime:Long, geo:Option[Warp10GeoValue], name:String, labels:Set[(String, String)], value:String):Warp10Data = Warp10Data(dateTime, geo, name, labels, StringWarp10Value(value))
}

case class PooledHttp1ClientConfiguration(maxTotalConnections:Option[Int], config: Option[BlazeClientConfig])

class Warp10Client(apiEndPoint:Uri, token:String, pooledHttp1ClientConfiguration:Option[PooledHttp1ClientConfiguration] = None){
val defaultMaxTotalConnections4pooledHttp1ClientConfiguration = 10
 val httpClient = PooledHttp1Client(
   maxTotalConnections = pooledHttp1ClientConfiguration.fold(defaultMaxTotalConnections4pooledHttp1ClientConfiguration)(_.maxTotalConnections.getOrElse(defaultMaxTotalConnections4pooledHttp1ClientConfiguration)),
   config = pooledHttp1ClientConfiguration.fold(BlazeClientConfig.defaultConfig)(_.config.getOrElse(BlazeClientConfig.defaultConfig))
 )
 val requestTemplate = Request(
   method = Method.POST,
   uri = apiEndPoint,
   headers = Headers(
     Header("Host", apiEndPoint.host.fold("localhost")(_.renderString)),
     Header("X-Warp10-Token", token),
     Header("Content-Type", "test/plain")
   )
 )
 def sendData(datas:Set[Warp10Data]) = {
   val r = requestTemplate.withBody(datas.map(_.warp10Serialize).mkString("\n"))
   
 }

}

object Warp10Client {


}
