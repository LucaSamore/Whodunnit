package model.game

import model.generation.{Constraint, Producer, ProductionError}

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def caseFiles: Set[CaseFile]
  def solution: Solution

final case class CaseImpl(
    plot: Plot,
    caseFiles: Set[CaseFile],
    characters: Set[Character],
    solution: Solution
) extends Case

object Case:
  def apply(constraints: Constraint*)(using
      producer: Producer[Case]
  ): Either[ProductionError, Case] =
    producer.produce(constraints*)

final case class Plot(title: String, content: String)

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
