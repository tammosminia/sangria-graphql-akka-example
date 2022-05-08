import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import Theater._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class Theater(actorContext: ActorContext[_]) {
  private val echoActor: ActorRef[Query] = actorContext.spawnAnonymous(echoBehavior)

  private val timeout = new Timeout(5 seconds)
  private val scheduler = actorContext.system.scheduler

  def echo(in: In): Future[Out] =
    echoActor.ask(ref => Query(in, ref))(timeout, scheduler)
}

object Theater {
  type In = String
  type Out = String

  case class Query(input: In, replyTo: ActorRef[Out])

  val echoBehavior: Behavior[Query] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case Query(in, replyTo) =>
        val out = in
        replyTo ! out
        Behaviors.same
    }
  }


}