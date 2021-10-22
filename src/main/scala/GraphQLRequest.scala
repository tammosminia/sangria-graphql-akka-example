import io.circe.Json

case class GraphQLRequest(
    query: String,
    operationName: Option[String],
    variables: Option[Json])
