package model.casegeneration

import upickle.default.*

import scala.util.{Failure, Success, Try}
import cats.syntax.all.*

import java.time.LocalDateTime
import scala.annotation.tailrec

trait Parser:
  def parse(input: String): Either[ParseError, Case]

object JsonParser extends Parser:
  import CaseDTO.given

  def parse(input: String): Either[ParseError, Case] =
    parseJson(input).flatMap(_.toCase)

  private def parseJson(input: String): Either[ParseError, CaseDTO] =
    Try(read[CaseDTO](input)) match
      case Success(dto) => Right(dto)
      case Failure(e)   => Left(handleJsonError(e))

  private def handleJsonError(e: Throwable): ParseError =
    val rootCause = getRootCause(e)
    val errorMessage = rootCause.getMessage
    errorMessage match
      case msg if msg != null && msg.contains("missing keys in dictionary") =>
        MissingFieldError(extractFieldNameFromMissingKeys(msg))
      case msg if msg != null && msg.contains("missing") =>
        MissingFieldError(extractFieldName(msg))
      case _ =>
        JsonSyntaxError(Option(errorMessage).getOrElse("Invalid JSON"))

  @tailrec
  private def getRootCause(e: Throwable): Throwable =
    if e.getCause != null then getRootCause(e.getCause) else e

  private def extractFieldNameFromMissingKeys(errorMsg: String): String =
    """missing keys in dictionary:\s*([a-zA-Z,\s]+)""".r
      .findFirstMatchIn(errorMsg)
      .flatMap(m => m.group(1).split(",").map(_.trim).headOption)
      .getOrElse("unknown")

  private def extractFieldName(errorMsg: String): String =
    val fieldPatterns = Map(
      "plot" -> "plot",
      "characters" -> "characters",
      "files" -> "files",
      "title" -> "title",
      "content" -> "content",
      "name" -> "name",
      "role" -> "role",
      "kind" -> "kind",
      "solution" -> "solution",
      "culprit" -> "culprit",
      "prerequisite" -> "prerequisite",
      "firstEntity" -> "firstEntity",
      "secondEntity" -> "secondEntity",
      "semantic" -> "semantic",
      "motive" -> "motive"
    )
    fieldPatterns.find((_, pattern) => errorMsg.contains(pattern))
      .map(_._1)
      .getOrElse("unknown")

  private def parseRole(roleStr: String): Option[CaseRole] =
    CaseRole.values.find(_.toString.equalsIgnoreCase(roleStr))

  private def parseFileType(typeStr: String): Option[CaseFileType] =
    CaseFileType.values.find(_.toString.equalsIgnoreCase(typeStr))

  private def parseTimestamp(tsStr: String): Option[LocalDateTime] =
    Try(LocalDateTime.parse(tsStr)).toOption

  private def parseTimestampEither(tsStr: String)
      : Either[ParseError, LocalDateTime] =
    parseTimestamp(tsStr).toRight(InvalidFieldError(
      "file.date",
      s"Invalid timestamp: $tsStr"
    ))

  private def validateNonEmpty[A](
      set: Set[A],
      name: String
  ): Either[ParseError, Set[A]] =
    if set.isEmpty then Left(InvalidFieldError(name, "must not be empty"))
    else Right(set)

  private given CaseDTOOps: AnyRef with
    extension (dto: CaseDTO)
      def toCase: Either[ParseError, Case] =
        for {
          plotObj <- dto.plot.toPlot
          characters <- dto.characters.traverse(_.toCharacter)
          characterSet = characters.toSet
          _ <- validateNonEmpty(characterSet, "characters")
          caseFiles <- dto.files.traverse(_.toCaseFile(characterSet))
          caseFileSet = caseFiles.toSet
          _ <- validateNonEmpty(caseFileSet, "files")
          solutionObj <- dto.solution.toSolution(characterSet, caseFileSet)
        } yield Case(plotObj, caseFileSet, characterSet, solutionObj)

  private given PlotDTOOps: AnyRef with
    extension (dto: PlotDTO)
      def toPlot: Either[ParseError, Plot] =
        for {
          _ <- Either.cond(
            dto.title.nonEmpty,
            (),
            InvalidFieldError("plot.title", "must not be empty")
          )
          _ <- Either.cond(
            dto.content.nonEmpty,
            (),
            InvalidFieldError("plot.content", "must not be empty")
          )
        } yield Plot(dto.title, dto.content)

  private given CharacterDTOOps: AnyRef with
    extension (dto: CharacterDTO)
      def toCharacter: Either[ParseError, Character] =
        for {
          _ <- Either.cond(
            dto.name.nonEmpty,
            (),
            InvalidFieldError("character.name", "must not be empty")
          )
          roleValue <- parseRole(dto.role).toRight(InvalidFieldError(
            "character.role",
            s"Unknown role: ${dto.role}"
          ))
        } yield Character(dto.name, roleValue)

  private given CaseFileDTOOps: AnyRef with
    extension (dto: CaseFileDTO)
      def toCaseFile(characters: Set[Character]): Either[ParseError, CaseFile] =
        for {
          _ <- Either.cond(
            dto.title.nonEmpty,
            (),
            InvalidFieldError("file.title", "must not be empty")
          )
          _ <- Either.cond(
            dto.content.nonEmpty,
            (),
            InvalidFieldError("file.content", "must not be empty")
          )
          fileType <- parseFileType(dto.kind).toRight(InvalidFieldError(
            "file.kind",
            s"Unknown type: ${dto.kind}"
          ))
          validatedSender <- validateCharacter(dto.sender, characters, "sender")
          validatedReceiver <-
            validateCharacter(dto.receiver, characters, "receiver")
          timestamp <- dto.date.traverse(parseTimestampEither)
        } yield CaseFile(
          dto.title,
          dto.content,
          fileType,
          validatedSender,
          validatedReceiver,
          timestamp
        )

      private def validateCharacter(
          name: Option[String],
          characters: Set[Character],
          field: String
      ): Either[ParseError, Option[Character]] =
        name.traverse { n =>
          characters.find(_.name == n).toRight(InvalidFieldError(
            s"file.$field",
            s"Character '$n' not found"
          ))
        }

  private given SolutionDTOOps: AnyRef with
    extension (dto: SolutionDTO)
      def toSolution(
          characters: Set[Character],
          files: Set[CaseFile]
      ): Either[ParseError, CaseSolution] =
        for {
          culpritChar <- characters.find(_.name == dto.culprit)
            .toRight(InvalidFieldError(
              "solution.culprit",
              s"Character '${dto.culprit}' not found"
            ))
          prerequisites <-
            dto.prerequisite.traverse(_.toKGPrerequisite(characters, files))
        } yield CaseSolution(prerequisites.toSet, culpritChar, dto.motive)

  private given PrerequisiteDTOOps: AnyRef with
    extension (dto: PrerequisiteDTO)
      def toKGPrerequisite(
          characters: Set[Character],
          files: Set[CaseFile]
      ): Either[ParseError, KGPrerequisite] =
        for {
          first <- resolveEntity(dto.firstEntity, characters, files)
          second <- resolveEntity(dto.secondEntity, characters, files)
        } yield KGPrerequisite(first, second, dto.semantic)

      private def resolveEntity(
          name: String,
          characters: Set[Character],
          files: Set[CaseFile]
      ): Either[ParseError, Character | CaseFile] =
        characters.find(_.name == name)
          .orElse(files.find(_.title == name))
          .toRight(InvalidFieldError(
            "solution.prerequisite",
            s"Entity '$name' not found"
          ))
          .asInstanceOf[Either[ParseError, Character | CaseFile]]

sealed trait ParseError:
  def message: String

case class JsonSyntaxError(message: String) extends ParseError
case class MissingFieldError(field: String) extends ParseError:
  def message = s"Missing required field: $field"
case class InvalidFieldError(field: String, reason: String) extends ParseError:
  def message = s"Invalid field '$field': $reason"
