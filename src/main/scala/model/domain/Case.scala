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