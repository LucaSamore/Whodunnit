package model.`case`

import model.{CaseFile, Solution, Character}

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def files: Set[CaseFile]
  def solution: Solution

final case class Plot(
  title: String,
  content: String
)

object Case:
  def apply(
      plot: Plot,
      files: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ): Case =
    CaseImpl(plot, files, characters, solution)

  private case class CaseImpl(
      plot: Plot,
      files: Set[CaseFile],
      characters: Set[Character],
      solution: Solution
  ) extends Case
