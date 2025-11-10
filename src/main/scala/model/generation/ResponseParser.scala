package model.generation

import model.game
import model.game.{Case, CaseImpl, Hint, HintImpl}
import upickle.default.*

import scala.util.Try

trait ResponseParser[T]:
  def parse(jsonString: String): Either[ProductionError, T]

object ResponseParser:

  private def parseString[T: Reader](jsonString: String): Either[ProductionError, T] =
    Try(read[T](jsonString)).toEither.left.map { e =>
      ProductionError.ParseError(s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
    }

  given ResponseParser[Case] with
    override def parse(jsonString: String): Either[ProductionError, Case] = parseString[CaseImpl](jsonString)

  given ResponseParser[Hint] with
    override def parse(jsonString: String): Either[ProductionError, Hint] = parseString[HintImpl](jsonString)
