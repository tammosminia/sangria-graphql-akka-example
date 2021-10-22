import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Json, JsonObject}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.schema.Schema

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class GraphQL(
  )(implicit mat: Materializer)
    extends FailFastCirceSupport {
  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def endpoint(request: GraphQLRequest): Route =
      QueryParser.parse(request.query) match {

        case Success(queryAst) =>
          val vars = request.variables match {
            case Some(obj: Json) => obj
            case _               => Json.fromJsonObject(JsonObject.empty)
          }
          complete(executeGraphQLQuery(queryAst, request.operationName, vars))

        case Failure(error) =>
          complete(BadRequest, "error" -> error.getMessage)
      }

  private def executeGraphQLQuery(
      query: Document,
      operation: Option[String],
      variables: Json,
    ) = {

    Executor
      .execute(
        Schema(QuerySchema.QueryType(), Some(MutationSchema.mutationType), subscription = Some(new GraphqlSubscription().SubscriptionType)),
        query,
        operationName = operation,
        variables = variables
      )
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError    => BadRequest -> error.resolveError
        case error: ErrorWithResolver     => InternalServerError -> error.resolveError
      }
  }

}
