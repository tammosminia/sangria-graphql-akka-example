import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import sangria.schema.{Action, Context, Field, ObjectType, StringType, fields}
import sangria.streaming.akkaStreams._

import scala.concurrent.{ExecutionContext, Future}

//https://sangria-graphql.github.io/learn/#stream-based-subscriptions
//https://github.com/hasura/graphqurl
class GraphqlSubscription(implicit ec: ExecutionContext, mat: Materializer) {
  lazy val eventStream: Source[String, NotUsed] =
    Source.unfoldAsync(1)(i =>
      Future {
        Thread.sleep(1000)
        Some((i + 1, s"bla $i"))
      }
    )

  class Ctx()

  val SubscriptionType = ObjectType(
    "Subscription",
    fields[Unit, Unit](
      Field.subs(
        "messageEvents",
        StringType,
        resolve = (c: Context[Unit, Unit]) =>
          eventStream
            .map(event ⇒ Action(event))
      )(akkaStreamIsValidSubscriptionStream)
    )
  )

}
