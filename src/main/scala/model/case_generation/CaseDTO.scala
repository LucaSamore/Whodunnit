package model.case_generation

import upickle.default.*

case class CaseDTO(
                    plot: PlotDTO,
                    characters: List[CharacterDTO],
                    files: List[CaseFileDTO],
                    solution: SolutionDTO
                  )

case class PlotDTO(title: String, content: String)
case class CharacterDTO(name: String, role: String)
case class CaseFileDTO(
                        title: String,
                        kind: String,
                        sender: Option[String] = None,
                        receiver: Option[String] = None,
                        date: Option[String] = None,
                        content: String
                      )
case class SolutionDTO(
                        prerequisite: List[PrerequisiteDTO],
                        culprit: String,
                        motive: String
                      )
case class PrerequisiteDTO(
                            firstEntity: String,
                            secondEntity: String,
                            semantic: String
                          )

object CaseDTO {
  given ReadWriter[PlotDTO] = macroRW
  given ReadWriter[CharacterDTO] = macroRW
  given ReadWriter[CaseFileDTO] = macroRW
  given ReadWriter[PrerequisiteDTO] = macroRW
  given ReadWriter[SolutionDTO] = macroRW
  given ReadWriter[CaseDTO] = macroRW
}