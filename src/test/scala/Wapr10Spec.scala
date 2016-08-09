import org.specs2._
import com.clevercloud.warp10client._
import org.http4s._
import java.lang.System
import scalaz._
import scalaz.concurrent._


class Warp10CLientSendSpec extends Specification with matcher.DisjunctionMatchers { def is = s2"""

  This is a specification to check the Warp10 client on sending data

  The Warp10 client should
    Fail with 500 status on invalid token                         $e2

    status 200 sending Int data on the warp10 DB                  $e1
    status 200 sending multiple Int data on the warp10 DB         $e3


                                                                 """

  val time = System.currentTimeMillis()

  val failedw10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "invalid_token")

  val w10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "fDdY9M_vl8ex14yz7DDVA9bPvfrDbVvUGn_jzPQbJdK0MMuEWArnyNIzwtuRmJbDmT9ogKlK2rs08cD6SguzsfK2dU2Z6ZohXf1JlcwTLlX8hJQDwqQGJwHu8IWvGPmN")

  val validSend_f = w10client.sendData(Warp10Data(time, None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7))
  def e1 = getStatusCode(validSend_f) must be_\/-(200)

  val valid_multiple_Send_f = w10client.sendData(Set(
    Warp10Data(time, None, "org.test.plain2", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7),
    Warp10Data(time, None, "org.test.plain3", Set("label1" -> "dsfF3", "anotherlabel" -> "dsfg"), 7),
    Warp10Data(time, None, "org.test.plain4", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)
  ))
  def e3 = getStatusCode(valid_multiple_Send_f) must be_\/-(200)

  val failedSend_f = failedw10client.sendData(Set(Warp10Data(time, None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)))
  def e2 = getStatusCode(failedSend_f) must be_\/-(500)


  def getStatusCode(f:Future[Throwable \/ Response]) = {
    f.unsafePerformSync match {
      case \/-(r) => \/-(r.status.code)
      case -\/(x) => -\/(x)
    }
  }

}
