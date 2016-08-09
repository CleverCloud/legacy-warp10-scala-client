import org.scalatest._
import com.clevercloud.warp10client._
import org.http4s._
import java.lang.System

class Warp10CLientSendSpec extends FlatSpec with Matchers {

  "Warp10Client" should "fail at building a client with an invalid URI" in {


    val failedw10client = new Warp10Client(Uri.uri("http:/localhost:8080/"), "fDdY9M_vl8ex14yz7DDVA9bPvfrDbVvUGn_jzPQbJdK0MMuEWArnyNIzwtuRmJbDmT9ogKlK2rs08cD6SguzsfK2dU2Z6ZohXf1JlcwTLlX8hJQDwqQGJwHu8IWvGPmN")
    println(failedw10client)

    failedw10client.sendData(Set(Warp10Data(System.currentTimeMillis(), None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)))

    true should === (true)
  }



  "Warp10Client" should "Send some Int data" in {


    val w10client = new Warp10Client(Uri.uri("http://localhost:8080/"), "fDdY9M_vl8ex14yz7DDVA9bPvfrDbVvUGn_jzPQbJdK0MMuEWArnyNIzwtuRmJbDmT9ogKlK2rs08cD6SguzsfK2dU2Z6ZohXf1JlcwTLlX8hJQDwqQGJwHu8IWvGPmN")


    w10client.sendData(Set(Warp10Data(System.currentTimeMillis(), None, "org.test.plain", Set("label1" -> "dsfF3", "label2" -> "dsfg"), 7)))

    true should === (true)
  }
}
