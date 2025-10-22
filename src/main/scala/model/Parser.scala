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
      caseFiles <- extractFiles(json, characters)
    yield Case(
      plot,
      caseFiles,
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

  private def extractFiles(json: Value, characters: Set[Character]): Either[ParseError, Set[CaseFile]] =
    if !json.obj.contains("files") then
      Left(MissingFieldError("files"))
    else
      Try {
        val array = json("files").arr
        array.map { fileJson =>
          val title = fileJson("title").str
          val content = fileJson("content").str
          val kindStr = fileJson("kind").str
          val kind = parseFileType(kindStr).getOrElse(
            throw new Exception(s"Unknown file type: $kindStr")
          )

          val senderName = extractOptionalString(fileJson, "sender")
          val receiverName = extractOptionalString(fileJson, "receiver")

          val sender = senderName.flatMap(n => characters.find(_.name == n))
          val receiver = receiverName.flatMap(n => characters.find(_.name == n))

          val timestamp = extractOptionalString(fileJson, "date").flatMap(parseTimestamp)

          CaseFile(title, content, kind, sender, receiver, timestamp)
        }.toSet
      } match
        case Success(files) => Right(files)
        case Failure(e) => Left(InvalidFieldError("files", e.getMessage))

  private def extractOptionalString(json: Value, field: String): Option[String] =
    json.obj.get(field).flatMap { v =>
      if v.isNull then None else Some(v.str)
    }

  private def parseTimestamp(tsStr: String): Option[java.time.LocalDateTime] =
    Try(java.time.LocalDateTime.parse(tsStr)) match
      case Success(ts) => Some(ts)
      case Failure(_)  => None

  private def parseFileType(typeStr: String): Option[CaseFileType] =
    import CaseFileType.*
    typeStr match
      case "Email" => Some(Email)
      case "Message" => Some(Message)
      case "Interview" => Some(Interview)
      case "Diary" => Some(Diary)
      case "TextDocument" => Some(TextDocument)
      case "Notes" => Some(Notes)
      case _ => None

sealed trait ParseError:
  def message: String

case class JsonSyntaxError(message: String) extends ParseError
case class MissingFieldError(field: String) extends ParseError:
  def message = s"Missing required field: $field"
case class InvalidFieldError(field: String, reason: String) extends ParseError:
  def message = s"Invalid field '$field': $reason"
