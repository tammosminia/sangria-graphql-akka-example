import sangria.macros.derive.{
  InputObjectTypeName,
  ObjectTypeName,
  deriveInputObjectType,
  deriveObjectType
}
import sangria.marshalling.{
  CoercedScalaResultMarshaller,
  FromInput,
  ResultMarshaller
}
import sangria.schema.{Field, fields, _}
//import io.circe.generic.auto._
import sangria.ast
import sangria.ast.{ObjectValue, StringValue}
//import sangria.marshalling.circe._
import sangria.validation.Violation
import sangria.marshalling.RawResultMarshaller
import java.time.LocalDate
import scala.util.{Failure, Success, Try}

case class Color(c: String) extends AnyVal
object Color {
//  implicit val gqlType: ScalarType[Color] = ScalarType[Color](
//    "Color",
//    description = Some("Color"),
//    coerceOutput = (c, _) => c.c,
////    coerceOutput = (dt, _) => ObjectValue(
////        "c" -> StringValue(dt.c)
////    ),
//    coerceInput = {
//      case StringValue(s, _, _, _, _) => parseColor(s)
//      case _                          => Left(ParseColorViolation)
//    },
//    coerceUserInput = {
//      case s: String => parseColor(s)
//      case _         => Left(ParseColorViolation)
//    }
//  )
  private def parseColor(c: String) =
    Try(Color(c)) match {
      case Success(v) => Right(v)
      case Failure(_) => Left(ParseColorViolation)
    }
  case object ParseColorViolation extends Violation {
    override def errorMessage: String = "Error while parsing Color"
  }

  implicit val colorFromInput: FromInput[Color] = new FromInput[Color] {
    override val marshaller: ResultMarshaller =
      CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): Color = {
      Color(node.asInstanceOf[String])
    }

  }
  implicit val gqlType: ScalarAlias[Color, String] =
    ScalarAlias[Color, String](StringType, _.c, c => Right(Color(c)))
}

case class Cat(name: String, color: Color)
object Cat {
  implicit val gqlType: ObjectType[Unit, Cat] =
    deriveObjectType[Unit, Cat](ObjectTypeName("Cat"))

  implicit val gqlInputType: InputObjectType[Cat] =
    deriveInputObjectType[Cat](InputObjectTypeName("CatInput"))

  implicit val catFromInput: FromInput[Cat] = new FromInput[Cat] {
    override val marshaller: ResultMarshaller =
      CoercedScalaResultMarshaller.default

    override def fromResult(node: marshaller.Node): Cat = {
      val map = node.asInstanceOf[Map[String, Any]]
      Cat(map("name").asInstanceOf[String], map("color").asInstanceOf[Color])
    }

  }

}

object MutationSchema {

  def mutationType: ObjectType[Unit, Unit] = ObjectType(
    "Mutation",
    fields[Unit, Unit](
      Field(
        "paintCat",
        Cat.gqlType,
        arguments = List(
          Argument("cat", Cat.gqlInputType),
          Argument("color", Color.gqlType)
        ),
        resolve = c => changeCat(c.arg[Cat]("cat"), c.arg[Color]("color"))
      )
    )
  )

  def changeCat(cat: Cat, color: Color): Cat = cat.copy(color = color)
}
