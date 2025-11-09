package model.generation

import model.game
import model.game.{Case, CaseImpl, Hint, HintImpl}
import upickle.default.*

trait ResponseParser[T]:
  def parse(jsonString: String): Either[ProductionError, T]

object ResponseParser:

  given ResponseParser[Case] with
    override def parse(jsonString: String): Either[ProductionError, Case] =
      try
        println(jsonString)
        Right(read[CaseImpl](jsonString))
      catch
        case e: Exception =>
          Left(ProductionError.ParseError(
            s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}"
          ))

  given ResponseParser[Hint] with
    override def parse(jsonString: String): Either[ProductionError, Hint] =
      try
        print(jsonString)
        Right(read[HintImpl](jsonString))
      catch
        case e: Exception =>
          Left(ProductionError.ParseError(
            s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}"
          ))
