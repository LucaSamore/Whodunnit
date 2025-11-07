package utils

import model.casegeneration.*

import java.time.LocalDateTime

object TestUtils:

  val mockPlot = Plot("A mysterious case", "Detailed description of the case.")
  val mockCharacterJD = Character("John Doe", CaseRole.Suspect)
  val mockCharacterJS = Character("Jane Smith", CaseRole.Victim)
  val mockCaseFiles = Set(CaseFile(
    "title",
    "content",
    CaseFileType.Message,
    Some(mockCharacterJD),
    Some(mockCharacterJS),
    Some(LocalDateTime.now())
  ))
  val mockCharacters = Set(
    mockCharacterJD,
    mockCharacterJS
  )
  val mockSolution = CaseSolution(
    Set(KGPrerequisite(mockCharacterJD, mockCharacterJS, "hates")),
    mockCharacterJD,
    "John Doe wanted revenge."
  )
  val mockCase: Case = new Case:
    def plot: Plot = mockPlot
    def caseFiles: Set[CaseFile] = mockCaseFiles
    def characters: Set[Character] = mockCharacters
    def solution: Solution = mockSolution
