import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import sttp.client3.{HttpURLConnectionBackend, SttpBackend, UriContext, basicRequest}
import akka.Done
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.util.ByteString
import sttp.client3._
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.ws

import scala.concurrent.duration._
import scala.concurrent.Future

class ServerTest extends org.scalatest.FunSuite {
  def startServer(): Unit = {
    Server.main(Array(""))
    Thread.sleep(1000)
  }

  startServer()

  test("subscription as http") {
    val request = basicRequest.post(uri"http://localhost:8080/graphql")
      .body("""{"query":"subscription Subscription {\n    messageEvents\n}","operationName":"Subscription"}""".stripMargin)
      .contentType("application/json")

    val backend = HttpURLConnectionBackend()
    val response = request.send(backend)

    // response.header(...): Option[String]
    println(response.header("Content-Length"))

    // response.body: by default read into an Either[String, String] to indicate failure or success
    println(response.body)
  }

  test("using websocket") {
    implicit val system = ActorSystem("test")
    implicit val backend = AkkaHttpBackend.usingActorSystem(system)
//    implicit val backend: SttpBackend[Future, Source[ByteString, Any],
//      Flow[Message, Message, *]] =
//      AkkaHttpBackend.usingActorSystem(system)
    import system.dispatcher

    // creating a sink, which prints all incoming messages to the console
    val sink: Sink[Message, Future[Done]] = Sink.foreach[Message] {
      case m: TextMessage =>
        m.toStrict(1.second).foreach(s => println(s"RECEIVED: $s"))
      case _ =>
    }

    // creating a source, which produces a new text message each second
    val source: Source[Message, Cancellable] = Source
      .tick(1.second, 1.second, ())
      .map(_ => TextMessage("Hello!"))

    // combining the sink & source into a flow; the sink and source are
    // disconnected and operate independently
    val flow: Flow[Message, Message, Future[Done]] =
    Flow.fromSinkAndSourceMat(sink, source)(Keep.left)

    // using a test websocket endpoint
    val response = basicRequest.get(uri"wss://echo.websocket.org").response

    println("end")
    // the "response" will be completed once the websocket is established
//    response.foreach { r =>
//      println("Websocket established!")
//      // The Future[Done] comes from the sink, and in this case will be completed
//      // once the server closes the connection.
//      r.result.foreach { _ =>
//        println("Websocket closed!")
//      }
//    }

  }
}
