import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import org.scalatest.Assertion
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt

class TheaterTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  def setupWithContext[T](f: ActorContext[T] => Assertion): Assertion = {
    //a bit of a hack to get ActorContext from testKit
    def extractor(replyTo: ActorRef[Assertion]): Behavior[T] =
      Behaviors.setup { context =>
        replyTo ! f(context)

        Behaviors.ignore
      }
    val probe = testKit.createTestProbe[Assertion]()
    testKit.spawn(extractor(probe.ref))
    probe.receiveMessage(1.minute)
  }

  "Theater" should {
    "echo input back" in setupWithContext[NotUsed] { context =>
      val theater = new Theater(context)

      whenReady(theater.echo("test")) { r =>
        r shouldBe "test"
      }
    }
  }

}