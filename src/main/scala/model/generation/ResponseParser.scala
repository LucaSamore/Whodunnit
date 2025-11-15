package model.generation

import model.game
import model.game.{Case, CaseImpl, Hint, HintImpl}
import upickle.default.*

import scala.util.Try

/** Type class for parsing LLM responses into domain objects.
  *
  * ResponseParser implementations define how to deserialize JSON strings returned by LLM APIs into typed domain
  * entities. Parsing failures are captured as ProductionErrors.
  *
  * @tparam T
  *   the target type to parse into
  */
trait ResponseParser[T]:
  /** Parses a JSON string into a domain object.
    *
    * @param jsonString
    *   the raw JSON response from the LLM
    * @return
    *   Right(parsed object) on success, Left(ParseError) on failure
    */
  def parse(jsonString: String): Either[ProductionError, T]

/** Companion object providing default parser instances. */
object ResponseParser:

  /** Parser for Case generation responses. */
  given ResponseParser[Case] with
    override def parse(jsonString: String): Either[ProductionError, Case] = parseString[CaseImpl](jsonString)

  /** Parser for Hint generation responses. */
  given ResponseParser[Hint] with
    override def parse(jsonString: String): Either[ProductionError, Hint] = parseString[HintImpl](jsonString)

  private def parseString[T: Reader](jsonString: String): Either[ProductionError, T] =
    Try(read[T](jsonString)).toEither.left.map { e =>
      ProductionError.ParseError(s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
    }
