import org.specs2._
import com.clevercloud.warp10client._
import org.http4s._
import java.lang.System
import scalaz._
import scalaz.concurrent._


class Warp10CLientSendSpec extends Specification with matcher.DisjunctionMatchers { def is = s2"""

  This is a specification to check the Warp10 client on sending data

  The Warp10 client should
    Fail with Warp10Error on invalid token                          $e2

    status 200 sending Int data on the warp10 DB                    $e1
    status 200 sending multiple Int data on the warp10 DB           $e3

    status 200 sending Long data on the warp10 DB                   $eLong
    status 200 sending Double data on the warp10 DB                 $eDouble
    status 200 sending Boolean data on the warp10 DB                $eBoolean

    status 200 sending String data on the warp10 DB                 $eString
    status 200 sending special char String data on the warp10 DB    $eStringWithSpecial
    status 200 sending json String data on the warp10 DB            $eStringjson

    status 200 sending special char in labels                       $eLongSpecialChar


    status 200 to test more request                                 $eStringMore1
    status 200 to test more request                                 $eStringMore2
    status 200 to test more request                                 $eStringMore3
    status 200 to test more request                                 $eStringMore4

                                                                  """

  val time = System.currentTimeMillis()

  val failedw10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "invalid_token")

  val failedSend_f = failedw10client.sendData(Set(Warp10Data(time, None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)))
  def e2 = getStatusCode(failedSend_f) must be_-\/(beAnInstanceOf[Warp10Error])


  val w10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "WRITE")

  val validSend_f = w10client.sendData(Warp10Data(time, None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7))
  def e1 = getStatusCode(validSend_f) must be_\/-(200)

  val valid_multiple_Send_f = w10client.sendData(Set(
    Warp10Data(time, None, "org.test.plain2", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7),
    Warp10Data(time, None, "org.test.plain3", Set("label1" -> "dsfF3", "anotherlabel" -> "dsfg"), 7),
    Warp10Data(time, None, "org.test.plain4", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)
  ))
  def e3 = getStatusCode(valid_multiple_Send_f) must be_\/-(200)

  val eLong_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.long", Set("label1" -> "dsfF3", "label2" -> "dsfg"), (56).toLong))
  def eLong = getStatusCode(eLong_f) must be_\/-(200)

  val eDouble_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.double", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 56.8957))
  def eDouble = getStatusCode(eDouble_f) must be_\/-(200)

  val eBoolean_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.boolean", Set("label1" -> "dsfF3", "label2" -> "dsfg"), true))
  def eBoolean = getStatusCode(eBoolean_f) must be_\/-(200)


  val eString_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test"))
  def eString = getStatusCode(eString_f) must be_\/-(200)

  val eStringWithSpecial_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.stringspecial", Set("label1" -> "dsfF3", "label2" -> "dsfg"), """d4T4 ~ $trïng (t€§t)! -_ <> &@#  " ' \ / plop"""))
  def eStringWithSpecial = getStatusCode(eStringWithSpecial_f) must be_\/-(200)

  val eStringjson_f= w10client.sendData(Warp10Data(time, None, "org.test.plain.json", Set("label1" -> "dsfF3", "label2" -> "dsfg"), """{"plop":5,"plop2" : true, "string" : "plopi", andnumber: 4.5}"""))
  def eStringjson = getStatusCode(eStringjson_f) must be_\/-(200)


  val eLongSpecialChar_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.labels", Set("lab$€$*el1" -> "d sf (o)sfF3", "las bel2" -> "dsfg£$€é&#@vds"), (56893890).toLong))
  def eLongSpecialChar = getStatusCode(eLongSpecialChar_f) must be_\/-(200)


  val eStringMore1_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test"))
  def eStringMore1 = getStatusCode(eStringMore1_f) must be_\/-(200)
  val eStringMore2_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test"))
  def eStringMore2 = getStatusCode(eStringMore2_f) must be_\/-(200)
  val eStringMore3_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test"))
  def eStringMore3 = getStatusCode(eStringMore3_f) must be_\/-(200)
  val eStringMore4_f = w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test"))
  def eStringMore4 = getStatusCode(eStringMore4_f) must be_\/-(200)

  println(w10client.sendData(Warp10Data(time, None, "org.test.plain.string", Set("label1" -> "dsfF3", "label2" -> "dsfg"), "data string test")).run + "  <<<<<") 

  def getStatusCode(f:Task[Response]) = {
    f.map(x => x.status.code)
  }.unsafePerformSyncAttempt

}
