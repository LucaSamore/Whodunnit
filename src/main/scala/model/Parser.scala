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
      characters <- extractCharacters(json)
    yield Case(
      plot,
      Set.empty,
      characters,
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

  private def extractCharacters(json: Value): Either[ParseError, Set[Character]] =
    if !json.obj.contains("characters") then
      Left(MissingFieldError("characters"))
    else
      Try {
        val array = json("characters").arr
        if array.isEmpty then
          throw new Exception("Characters list must not be empty")

        array.map { charJson =>
          val name = charJson("name").str
          val roleStr = charJson("role").str
          val role = parseRole(roleStr).getOrElse(
            throw new Exception(s"Unknown role: $roleStr")
          )
          Character(name, role)
        }.toSet
      } match
        case Success(chars) => Right(chars)
        case Failure(e) => Left(InvalidFieldError("characters", e.getMessage))

  private def parseRole(roleStr: String): Option[CaseRole] =
    import CaseRole.*
    roleStr match
      case "Suspect" => Some(Suspect)
      case "Victim" => Some(Victim)
      case "Witness" => Some(Witness)
      case "Investigator" => Some(Investigator)
      case "Accomplice" => Some(Accomplice)
      case "Informant" => Some(Informant)
      case _ => None

sealed trait ParseError:
  def message: String

case class JsonSyntaxError(message: String) extends ParseError
case class MissingFieldError(field: String) extends ParseError:
  def message = s"Missing required field: $field"
case class InvalidFieldError(field: String, reason: String) extends ParseError:
  def message = s"Invalid field '$field': $reason"
