import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._

object GraphQLRoutes extends FailFastCirceSupport {

  def graphQLroute()(implicit mat: Materializer
    ): Route =
    (post & path("graphql")) {
      entity(as[GraphQLRequest]) { query =>
        new GraphQL().endpoint(query)
      }
    }
}
