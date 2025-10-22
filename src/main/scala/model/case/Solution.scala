package model

sealed trait Solution:
  def culprit: Character
  def motive: String

final case class CaseSolution(
    prerequisite: Set[KGPrerequisite],
    culprit: Character,
    motive: String
) extends Solution

final case class KGPrerequisite(
    firstEntity: Character | CaseFile,
    secondEntity: Character | CaseFile,
    semantic: String
)
