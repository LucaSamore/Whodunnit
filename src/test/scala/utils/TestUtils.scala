package utils

import model.game.{Case, CaseFile, CaseFileType, CaseKnowledgeGraph, CaseRole, Character, Link, Plot, Solution}

object TestUtils:

  val mockPlot: Plot = Plot("A mysterious case", "Detailed description of the case.")
  val mockCharacterJD: Character = Character("John Doe", CaseRole.Suspect)
  val mockCharacterJS: Character = Character("Jane Smith", CaseRole.Victim)
  val mockCaseFiles: Set[CaseFile] = Set(CaseFile(
    "title",
    "content",
    CaseFileType.Message,
    Some(mockCharacterJD),
    Some(mockCharacterJS),
    Some("now, bitch")
  ))
  val mockCharacters: Set[Character] = Set(
    mockCharacterJD,
    mockCharacterJS
  )
  val mockSolution: Solution = Solution(
    new CaseKnowledgeGraph().withNodes(
      mockCharacterJD,
      mockCharacterJS
    ).withEdge(mockCharacterJD, Link("hates"), mockCharacterJS),
    mockCharacterJD,
    "John Doe wanted revenge."
  )
  val mockCase: Case = new Case:
    def plot: Plot = mockPlot
    def caseFiles: Set[CaseFile] = mockCaseFiles
    def characters: Set[Character] = mockCharacters
    def solution: Solution = mockSolution
