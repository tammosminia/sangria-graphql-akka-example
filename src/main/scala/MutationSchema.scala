import io.circe.Decoder
import io.circe.generic.auto._
import sangria.macros.derive.{InputObjectTypeName, ObjectTypeName, deriveInputObjectType, deriveObjectType}
import sangria.marshalling.circe._
import sangria.schema.{Field, fields, _}

case class Color(underlying: String) extends AnyVal
object Color {
  implicit val circeDecoder: Decoder[Color] = SangriaUtils.circeValueClassDecoder(apply)
  implicit val gqlType: ScalarAlias[Color, String] = SangriaUtils.gqlAliasType(apply, _.underlying)
}

case class Speed(underlying: Int) extends AnyVal
object Speed {
  def fromInt(i: Int): Speed = {
    require(i > 0, "cats only go forwards!")
    Speed(i)
  }

  implicit val circeDecoder: Decoder[Speed] = SangriaUtils.circeValueClassDecoder(fromInt)
  implicit val gqlType: ScalarAlias[Speed, Int] =
    SangriaUtils.gqlValueClassType[Speed, Int](fromInt, _.underlying, "Speed in m/s")
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
        resolve = c => {
          c.astFields
          paintCat(c.arg[Cat]("cat"), c.arg[Color]("color"), c.arg[Speed]("speed"))
        }
      )
    )
  )

  def paintCat(cat: Cat, color: Color, speed: Speed): Cat =
    cat.copy(color = color, speed = speed)
}
