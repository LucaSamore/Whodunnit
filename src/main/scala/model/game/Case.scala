package model.game

import model.generation.{Constraint, Producer, ProductionError}
import upickle.default._

trait Case:
  def plot: Plot
  def characters: Set[Character]
  def caseFiles: Set[CaseFile]
  def solution: Solution

object Case:
  def apply(constraints: Constraint*)(using producer: Producer[Case]): Either[ProductionError, Case] =
    producer.produce(constraints*)

private[model] final case class Plot(title: String, content: String) derives ReadWriter

private[model] final case class CaseImpl(
    plot: Plot,
    characters: Set[Character],
    caseFiles: Set[CaseFile],
    solution: Solution
) extends Case derives ReadWriter

private[model] final case class Solution(
    prerequisite: CaseKnowledgeGraph,
    culprit: Character,
    motive: String
) derives ReadWriter
