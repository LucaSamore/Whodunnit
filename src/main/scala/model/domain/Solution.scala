package model.domain

sealed trait Solution:
  def culprit: Character
  def motive: String

case class CaseSolution(
    prerequisite: Set[KGPrerequisite],
    culprit: Character,
    motive: String
) extends Solution

case class KGPrerequisite(
    firstEntity: Character | CaseFile,
    secondEntity: Character | CaseFile,
    semantic: String
)
