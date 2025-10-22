package model

import model.`case`.*
import ujson.*

import scala.util.{Failure, Success, Try}

trait Parser:
  def parse(input: String): Either[ParseError, Case]

object JsonParser extends Parser:
  def parse(input: String): Either[ParseError, Case] =
    for
      json <- parseJson(input.trim)
      plot <- extractPlot(json)
    yield Case(
      plot,
      Set.empty,
      Set.empty,
      CaseSolution(Set.empty, Character("", CaseRole.Suspect), "")
    )

  private def parseJson(input: String): Either[ParseError, Value] =
    Try(ujson.read(input)) match
      case Success(json) => Right(json)
      case Failure(e)    => Left(JsonSyntaxError(e.getMessage))

  private def extractPlot(json: Value): Either[ParseError, Plot] =
    if !json.obj.contains("plot") then
      Left(MissingFieldError("plot"))
    else
      val plotJson = json("plot")
      (for
        title <- Try(plotJson("title").str)
        content <- Try(plotJson("content").str)
      yield Plot(title, content)) match
        case Success(p) => Right(p)
        case Failure(_) => Left(MissingFieldError("plot.title or plot.content"))

sealed trait ParseError:
  def message: String

case class JsonSyntaxError(message: String) extends ParseError
case class MissingFieldError(field: String) extends ParseError:
  def message = s"Missing required field: $field"
case class InvalidFieldError(field: String, reason: String) extends ParseError:
  def message = s"Invalid field '$field': $reason"
