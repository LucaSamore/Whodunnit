package model.casegeneration

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def caseFiles: Set[CaseFile]
  def solution: Solution

final case class Plot(title: String, content: String)

object Case:
  def apply(
      plot: Plot,
      caseFiles: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ): Case = CaseImpl(plot, caseFiles, characters, solution)

  def generate(constraints: Constraint*)(using cg: CaseGenerator): Either[GenerationError, Case] =
    val expandedConstraints = Constraint.expandConstraints(constraints)
    cg.generate(expandedConstraints*)

  private case class CaseImpl(
      plot: Plot,
      caseFiles: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ) extends Case

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
