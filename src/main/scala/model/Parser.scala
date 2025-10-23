package model

import upickle.default.*
import model.`case`.*
import scala.util.{Try, Success, Failure}

trait Parser:
  def parse(input: String): Either[ParseError, Case]

object JsonParser extends Parser:
  import UPickleCodecs.given

  def parse(input: String): Either[ParseError, Case] =
    Try(read[CaseModel](input)) match
      case Success(caseModel) =>
        val result = caseModel.toCase
        result.foreach(c => println(s"Parsing completato con successo: $c"))
        result
      case Failure(e) =>
        e.getMessage match
          case msg if msg.contains("missing") => Left(MissingFieldError(extractFieldName(msg)))
          case msg => Left(JsonSyntaxError(msg))

  private def extractFieldName(errorMsg: String): String =
    errorMsg match
      case s if s.contains("plot") => "plot"
      case s if s.contains("characters") => "characters"
      case s if s.contains("files") => "files"
      case s if s.contains("title") => "title"
      case s if s.contains("content") => "content"
      case s if s.contains("name") => "name"
      case s if s.contains("role") => "role"
      case s if s.contains("kind") => "kind"
      case s if s.contains("solution") => "solution"
      case s if s.contains("culprit") => "culprit"
      case s if s.contains("prerequisite") => "prerequisite"
      case s if s.contains("firstEntity") => "firstEntity"
      case s if s.contains("secondEntity") => "secondEntity"
      case s if s.contains("semantic") => "semantic"
      case s if s.contains("motive") => "motive"
      case _ => "unknown"

  private case class CaseModel(
                                plot: PlotModel,
                                characters: List[CharacterModel],
                                files: List[CaseFileModel],
                                solution: SolutionModel
                              ):
    def toCase: Either[ParseError, Case] =
      for
        plotObj <- plot.toPlot
        characterSet <- characters.traverse(_.toCharacter).flatMap(_.toSet.validateNonEmpty("characters"))
        caseFiles <- files.traverse(_.toCaseFile(characterSet)).flatMap(_.toSet.validateNonEmpty("files"))
        solutionObj <- solution.toSolution(characterSet, caseFiles)
      yield Case(
        plotObj,
        caseFiles,
        characterSet,
        solutionObj
      )

  private case class PlotModel(title: String, content: String):
    def toPlot: Either[ParseError, Plot] =
      if title.isEmpty then Left(InvalidFieldError("plot.title", "must not be empty"))
      else if content.isEmpty then Left(InvalidFieldError("plot.content", "must not be empty"))
      else Right(Plot(title, content))

  private case class CharacterModel(name: String, role: String):
    def toCharacter: Either[ParseError, Character] =
      for
        _ <- name.nonEmpty.toRight(InvalidFieldError("character.name", "must not be empty"))
        roleValue <- parseRole(role).toRight(InvalidFieldError("character.role", s"Unknown role: $role"))
      yield Character(name, roleValue)

  private case class CaseFileModel(
                                    title: String,
                                    kind: String,
                                    sender: Option[String] = None,
                                    receiver: Option[String] = None,
                                    date: Option[String] = None,
                                    content: String
                                  ):
    def toCaseFile(characters: Set[Character]): Either[ParseError, CaseFile] =
      for
        _ <- title.nonEmpty.toRight(InvalidFieldError("file.title", "must not be empty"))
        _ <- content.nonEmpty.toRight(InvalidFieldError("file.content", "must not be empty"))
        fileType <- parseFileType(kind).toRight(InvalidFieldError("file.kind", s"Unknown type: $kind"))
        validatedSender <- validateCharacter(sender, characters, "sender")
        validatedReceiver <- validateCharacter(receiver, characters, "receiver")
        timestamp <- date.traverse(parseTimestampEither)
      yield CaseFile(title, content, fileType, validatedSender, validatedReceiver, timestamp)

    private def validateCharacter(name: Option[String], characters: Set[Character], field: String): Either[ParseError, Option[Character]] =
      name match
        case None => Right(None)
        case Some(n) =>
          characters.find(_.name == n)
            .toRight(InvalidFieldError(s"file.$field", s"Character '$n' not found"))
            .map(Some(_))

  private case class SolutionModel(
                                    prerequisite: List[PrerequisiteModel],
                                    culprit: String,
                                    motive: String
                                  ):
    def toSolution(characters: Set[Character], files: Set[CaseFile]): Either[ParseError, CaseSolution] =
      for
        culpritChar <- characters.find(_.name == culprit)
          .toRight(InvalidFieldError("solution.culprit", s"Character '$culprit' not found"))
        prerequisites <- prerequisite.traverse(_.toKGPrerequisite(characters, files))
      yield CaseSolution(prerequisites.toSet, culpritChar, motive)

  private case class PrerequisiteModel(
                                        firstEntity: String,
                                        secondEntity: String,
                                        semantic: String
                                      ):
    def toKGPrerequisite(characters: Set[Character], files: Set[CaseFile]): Either[ParseError, KGPrerequisite] =
      for
        first <- resolveEntity(firstEntity, characters, files)
        second <- resolveEntity(secondEntity, characters, files)
      yield KGPrerequisite(first, second, semantic)

  private def resolveEntity(name: String, characters: Set[Character], files: Set[CaseFile]): Either[ParseError, Character | CaseFile] =
    characters.find(_.name == name)
      .map(_.asInstanceOf[Character | CaseFile])
      .orElse(files.find(_.title == name).map(_.asInstanceOf[Character | CaseFile]))
      .toRight(InvalidFieldError("solution.prerequisite", s"Entity '$name' not found"))

  private object UPickleCodecs:
    given ReadWriter[PlotModel] = macroRW
    given ReadWriter[CharacterModel] = macroRW
    given ReadWriter[CaseFileModel] = macroRW
    given ReadWriter[PrerequisiteModel] = macroRW
    given ReadWriter[SolutionModel] = macroRW
    given ReadWriter[CaseModel] = macroRW

  extension [A](set: Set[A])
    private def validateNonEmpty(name: String): Either[ParseError, Set[A]] =
      if set.isEmpty then Left(InvalidFieldError(name, "must not be empty"))
      else Right(set)

  extension [A](list: List[A])
    def validateNonEmpty(name: String): Either[ParseError, List[A]] =
      if list.isEmpty then Left(InvalidFieldError(name, "must not be empty"))
      else Right(list)

    private def traverse[B](f: A => Either[ParseError, B]): Either[ParseError, List[B]] =
      list.foldRight(Right(Nil): Either[ParseError, List[B]]) { (a, acc) =>
        for
          b <- f(a)
          bs <- acc
        yield b :: bs
      }

  extension [A](option: Option[A])
    def toRight(error: => ParseError): Either[ParseError, A] =
      option.toRight(error)

    private def traverse[B](f: A => Either[ParseError, B]): Either[ParseError, Option[B]] =
      option match
        case None => Right(None)
        case Some(a) => f(a).map(Some(_))

  extension (bool: Boolean)
    private def toRight(error: => ParseError): Either[ParseError, Unit] =
      if bool then Right(()) else Left(error)

  private def parseRole(roleStr: String): Option[CaseRole] =
    CaseRole.values.find(_.toString.equalsIgnoreCase(roleStr))

  private def parseFileType(typeStr: String): Option[CaseFileType] =
    CaseFileType.values.find(_.toString.equalsIgnoreCase(typeStr))

  private def parseTimestamp(tsStr: String): Option[java.time.LocalDateTime] =
    Try(java.time.LocalDateTime.parse(tsStr)).toOption

  private def parseTimestampEither(tsStr: String): Either[ParseError, java.time.LocalDateTime] =
    parseTimestamp(tsStr).toRight(InvalidFieldError("file.date", s"Invalid timestamp: $tsStr"))

sealed trait ParseError:
  def message: String

case class JsonSyntaxError(message: String) extends ParseError
case class MissingFieldError(field: String) extends ParseError:
  def message = s"Missing required field: $field"
case class InvalidFieldError(field: String, reason: String) extends ParseError:
  def message = s"Invalid field '$field': $reason"