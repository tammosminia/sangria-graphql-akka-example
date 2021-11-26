import io.circe.generic.auto._
import sangria.ast
import sangria.macros.derive.{ObjectTypeName, deriveObjectType}
import sangria.marshalling.circe._
import sangria.schema.{Argument, fields, _}

object QuerySchema {
  case class WordOutput(word: String, reverse: String, letters: List[String])
  object WordOutput {
    implicit val gqlType: ObjectType[Unit, WordOutput] = deriveObjectType[Unit, WordOutput](ObjectTypeName("Word"))
  }

  def QueryType(): ObjectType[Unit, Unit] = ObjectType(
    "Query",
    fields[Unit, Unit](
      Field(
        "words",
        WordOutput.gqlType,
        arguments = List(Argument("word", StringType)),
        resolve = c => {
          val in: String = c.arg[String]("word")
          val lettersAsked: Boolean = c.astFields.head.selections.exists {
            case ast.Field(alias, "letters", arguments, directives, selections, comments, trailingComments, location) =>
              true
            case _ => false
          }
          val letters: List[String] = if (lettersAsked) in.toList.map(_.toString) else List()
          WordOutput(in, in.reverse, letters)
        }
      )
    )
  )
}
