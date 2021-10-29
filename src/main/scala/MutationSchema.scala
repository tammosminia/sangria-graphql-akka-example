import io.circe.{Decoder, HCursor}
import io.circe.generic.auto._
import sangria.macros.derive.{InputObjectTypeName, ObjectTypeName, deriveInputObjectType, deriveObjectType}
import sangria.marshalling.circe._
import sangria.schema.{Field, fields, _}
import sangria.validation.Violation

import scala.util.{Failure, Success, Try}

case class Color(underlying: String) extends AnyVal
object Color {
  implicit val circeDecoder: Decoder[Color] = (c: HCursor) => c.as[String].map(Color.apply)
  implicit val gqlType: ScalarAlias[Color, String] =
    ScalarAlias[Color, String](StringType, _.underlying, c => Right(Color(c)))
}

case class Speed(underlying: Int) extends AnyVal
object Speed {
  def fromInt(i: Int): Speed = {
    require(i > 0, "cats only go forwards!")
    Speed(i)
  }

  implicit val circeDecoder: Decoder[Speed] = (c: HCursor) => c.as[Int].map(fromInt)
  implicit val gqlType: ScalarAlias[Speed, Int] = {
    val renamed = IntType.copy(name = "Speed", description = Some("Speed in m/s"))
    ScalarAlias(renamed, _.underlying, parse)
  }

  private def parse(input: Int): Either[SpeedParseViolation, Speed] =
    Try(fromInt(input)) match {
      case Success(t) => Right(t)
      case Failure(e) => Left(new SpeedParseViolation(s"error parsing speed: ${e.getMessage}"))
    }

  private class SpeedParseViolation(val errorMessage: String) extends Violation
}

case class Cat(name: String, color: Color, speed: Speed)
object Cat {
  implicit val gqlType: ObjectType[Unit, Cat] = deriveObjectType[Unit, Cat](ObjectTypeName("Cat"))
  implicit val gqlInputType: InputObjectType[Cat] = deriveInputObjectType[Cat](InputObjectTypeName("CatInput"))
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
          Argument("color", Color.gqlType),
          Argument("speed", Speed.gqlType)
        ),
        resolve = c => paintCat(c.arg[Cat]("cat"), c.arg[Color]("color"), c.arg[Speed]("speed"))
      )
    )
  )

  def paintCat(cat: Cat, color: Color, speed: Speed): Cat =
    cat.copy(color = color, speed = speed)
}
