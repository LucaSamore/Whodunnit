package model.casegeneration

import upickle.default.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait ResponseParser[T]:
  def parse(jsonString: String): Either[ProductionError, T]

object ResponseParser:

  import CaseDTO.given

  given ResponseParser[Case] with
    def parse(jsonString: String): Either[ProductionError, Case] =
      val cleanedJson = cleanJson(jsonString)
      try
        val caseDTO = read[CaseDTO](cleanedJson)
        convertDTOToCase(caseDTO)
      catch
        case e: Exception =>
          Left(ProductionError.ParseError(
            s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}"
          ))

    private def cleanJson(input: String): String =
      input.replaceAll("```json", "").replaceAll("```", "").trim

    private def convertDTOToCase(dto: CaseDTO): Either[ProductionError, Case] =
      for
        plot <- convertPlot(dto.plot)
        characters <- convertCharacters(dto.characters)
        caseFiles <- convertCaseFiles(dto.caseFiles, characters)
        solution <- convertSolution(dto.solution, characters, caseFiles)
      yield CaseImpl(plot, caseFiles, characters, solution)

    private def convertPlot(dto: PlotDTO): Either[ProductionError, Plot] =
      Right(Plot(dto.title, dto.content))

    private def convertCharacters(dtos: Set[CharacterDTO])
        : Either[ProductionError, Set[Character]] =
      val converted = dtos.map { dto =>
        parseCaseRole(dto.role).map(role => Character(dto.name, role))
      }
      val errors = converted.collect { case Left(error) => error }
      if errors.nonEmpty then
        Left(errors.head)
      else
        Right(converted.collect { case Right(char) => char })

    private def convertCaseFiles(
        dtos: Set[CaseFileDTO],
        characters: Set[Character]
    ): Either[ProductionError, Set[CaseFile]] =
      val characterMap = characters.map(c => c.name -> c).toMap
      val converted = dtos.map { dto =>
        for
          kind <- parseCaseFileType(dto.kind)
          sender = dto.sender.flatMap(characterMap.get)
          receiver = dto.receiver.flatMap(characterMap.get)
          date <- dto.date.map(parseDateTime).getOrElse(Right(None))
        yield CaseFile(dto.title, dto.content, kind, sender, receiver, date)
      }
      val errors = converted.collect { case Left(error) => error }
      if errors.nonEmpty then
        Left(errors.head)
      else
        Right(converted.collect { case Right(file) => file })

    private def convertSolution(
        dto: SolutionDTO,
        characters: Set[Character],
        caseFiles: Set[CaseFile]
    ): Either[ProductionError, Solution] =
      val characterMap = characters.map(c => c.name -> c).toMap
      val caseFileMap = caseFiles.map(f => f.title -> f).toMap
      for
        culprit <- characterMap.get(dto.culprit)
          .toRight(
            ProductionError.ParseError(s"Culprit not found: ${dto.culprit}")
          )
        prerequisites <- convertPrerequisites(
          dto.prerequisite,
          characterMap,
          caseFileMap
        )
      yield CaseSolution(prerequisites, culprit, dto.motive)

    private def convertPrerequisites(
        dtos: Set[PrerequisiteDTO],
        characterMap: Map[String, Character],
        caseFileMap: Map[String, CaseFile]
    ): Either[ProductionError, Set[KGPrerequisite]] =
      val converted = dtos.map { dto =>
        for
          first <- resolveEntity(dto.firstEntity, characterMap, caseFileMap)
          second <- resolveEntity(dto.secondEntity, characterMap, caseFileMap)
        yield KGPrerequisite(first, second, dto.semantic)
      }
      val errors = converted.collect { case Left(error) => error }
      if errors.nonEmpty then
        Left(errors.head)
      else
        Right(converted.collect { case Right(prereq) => prereq })

    private def resolveEntity(
        entityName: String,
        characterMap: Map[String, Character],
        caseFileMap: Map[String, CaseFile]
    ): Either[ProductionError, Character | CaseFile] =
      val entity: Option[Character | CaseFile] =
        characterMap.get(entityName).orElse(caseFileMap.get(entityName))
      entity.toRight(
        ProductionError.ParseError(s"Entity not found: $entityName")
      )

    private def parseCaseRole(role: String): Either[ProductionError, CaseRole] =
      try
        Right(CaseRole.valueOf(role))
      catch
        case _: IllegalArgumentException =>
          Left(ProductionError.ParseError(s"Invalid CaseRole: $role"))

    private def parseCaseFileType(kind: String)
        : Either[ProductionError, CaseFileType] =
      try
        Right(CaseFileType.valueOf(kind))
      catch
        case _: IllegalArgumentException =>
          Left(ProductionError.ParseError(s"Invalid CaseFileType: $kind"))

    private def parseDateTime(
        dateStr: String
    ): Either[ProductionError, Option[LocalDateTime]] =
      try
        Right(Some(LocalDateTime.parse(
          dateStr,
          DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )))
      catch
        case e: Exception =>
          Left(ProductionError.ParseError(
            s"Invalid date format: $dateStr - ${e.getMessage}"
          ))
