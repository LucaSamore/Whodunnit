package model.case_generation

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def files: Set[CaseFile]
  def solution: Solution

final case class Plot(title: String, content: String)

object Case:
  def apply(
      plot: Plot,
      files: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ): Case = CaseImpl(plot, files, characters, solution)

  def generate(constraints: Constraint*)(using
      cg: CaseGenerator
  ): Either[GenerationError, Case] =
    val expandedConstraints = Constraint.expandConstraints(constraints)
    cg.generate(expandedConstraints*)

  private case class CaseImpl(
      plot: Plot,
      files: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ) extends Case
