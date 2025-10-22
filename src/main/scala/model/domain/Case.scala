package model.domain

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def files: Set[CaseFile]
  def solution: Solution

opaque type Plot = String

object Plot:
  def apply(text: String): Plot = text

  extension (p: Plot)
    def text: String = p


object Case:
  def apply(plot: Plot,
            files: Set[CaseFile],
            characters: Set[Character],
            solution: Solution): Case =
    CaseImpl(plot, files, characters, solution)

  private case class CaseImpl(
                               plot: Plot,
                               files: Set[CaseFile],
                               characters: Set[Character],
                               solution: Solution
                             ) extends Case