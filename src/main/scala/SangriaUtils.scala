import io.circe.{Decoder, HCursor}
import sangria.schema.{ScalarAlias, ScalarType}
import sangria.validation.Violation

import scala.reflect.{classTag, ClassTag}
import scala.util.{Failure, Success, Try}

object SangriaUtils {

  /** Needed at runtime to correctly decode value classes.
    * @tparam T The internal value class
    * @tparam U The external class that we use for in/output
    * @param decode function to decode input U into internal type T
    * @return The Circe decoder that we should add as an implicit
    */
  def circeValueClassDecoder[T, U: Decoder](decode: U => T): Decoder[T] = (c: HCursor) => c.as[U].map(decode)

  /** Create a gql type for value class `T`, that will be an alias for an existing gqlType `U`.
    * It will still be represented as `U` by graphQl.
    */
  def gqlAliasType[T: ClassTag, U](decode: U => T, encode: T => U)(implicit
      d: Decoder[T],
      uType: ScalarType[U]
  ): ScalarAlias[T, U] =
    ScalarAlias[T, U](uType, encode, gqlParse(decode))

  /** Parse input of gqlType `U` into a value class `T`.
    * It will be represented as `className[T]` by graphQl.
    */
  def gqlValueClassType[T: ClassTag, U](
      decode: U => T,
      encode: T => U,
      description: String
  )(implicit d: Decoder[T], uType: ScalarType[U]): ScalarAlias[T, U] = {
    val renamed = uType.copy(name = className[T], description = Some(description))
    ScalarAlias[T, U](renamed, encode, gqlParse(decode))
  }

  private def gqlParse[T: ClassTag, U](decode: U => T)(input: U): Either[GqlParseViolation[T], T] =
    Try(decode(input)) match {
      case Success(t) => Right(t)
      case Failure(_) => Left(new GqlParseViolation[T])
    }

  private class GqlParseViolation[T: ClassTag] extends Violation {
    override def errorMessage: String = s"Error while parsing ${className[T]}"
  }

  //Include the package to make sure names are unique
  private def className[T: ClassTag]: String = classTag[T].runtimeClass.getName.replace(".", "_").replace("$", "_")
}
