import akka.NotUsed
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Server {
  def main(args: Array[String]): Unit = {
    def behavior: Behavior[NotUsed] = Behaviors.setup { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      implicit val ec: ExecutionContext = system.executionContext

      val serverBinding = Http()(system).newServerAt("localhost", 8080).bind(GraphQLRoutes.graphQLroute())

      serverBinding.onComplete {
        case Success(bound) =>
          println(s"server running at ${bound.localAddress.getHostString}:${bound.localAddress.getPort}.")
        case Failure(e) =>
          println(s"cannot start the server. ${e.getMessage}")
          system.terminate()
      }

      Behaviors.ignore
    }

    val system: ActorSystem[Nothing] = ActorSystem(behavior, "test")
    CoordinatedShutdown(system).addJvmShutdownHook {
      println("end")
    }

  }


}
