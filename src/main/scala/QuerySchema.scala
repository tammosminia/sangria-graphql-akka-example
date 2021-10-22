import io.circe.generic.auto._
import sangria.marshalling.circe._
import sangria.schema.{Argument, Field, fields, _}

import java.time.LocalDate

object QuerySchema {

  def QueryType(): ObjectType[Unit, Unit] = ObjectType(
    "Query",
    fields[Unit, Unit](
      Field(
        "bla",
        StringType,
        arguments = List(),
        resolve = c => {
          "bla"
        }
      ),
    )
  )
}
