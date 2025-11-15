package model.game

import model.generation.{Constraint, Producer, ProductionError}
import upickle.default._

/** Represents an investigative case that the player must solve.
  *
  * A Case is an immutable aggregate containing all narrative elements: the plot setup, involved characters, available
  * evidence (case files), and the hidden solution. It serves as the ground truth of the mystery and is generated based
  * on configurable constraints such as difficulty and theme.
  */
trait Case:
  /** The narrative setup of the case.
    *
    * @return
    *   the plot containing title and story description
    */
  def plot: Plot

  /** All characters involved in the case.
    *
    * @return
    *   a set of characters
    */
  def characters: Set[Character]

  /** Documents and evidence pieces available for investigation.
    *
    * @return
    *   a set of case files containing clues and information
    */
  def caseFiles: Set[CaseFile]

  /** The hidden truth of the case.
    *
    * @return
    *   the solution containing the culprit, prerequisite knowledge graph, and motive
    */
  def solution: Solution

/** Companion object providing factory methods for creating Case instances. */
object Case:
  /** Generates a new case based on the provided constraints.
    *
    * The generation delegates to a Producer in scope, which creates the case according to the specified parameters
    * (e.g., difficulty, theme)
    *
    * @param constraints
    *   configuration parameters for case generation
    * @param producer
    *   the generation strategy (implicit)
    * @return
    *   Right(case) on success, Left(error) on failure
    */
  def apply(constraints: Constraint*)(using producer: Producer[Case]): Either[ProductionError, Case] =
    producer.produce(constraints*)

/** The narrative introduction to a case.
  *
  * @param title
  *   the case name
  * @param content
  *   the story description presented to the player at the start
  */
final case class Plot(title: String, content: String) derives ReadWriter

/** The correct solution to a case.
  *
  * @param prerequisite
  *   the minimal knowledge graph structure that must be reconstructed by the player to correctly solve the mystery
  * @param culprit
  *   the character responsible for the crime
  * @param motive
  *   the reason behind the crime
  */
final case class Solution(prerequisite: CaseKnowledgeGraph, culprit: Character, motive: String) derives ReadWriter

private[model] final case class CaseImpl(
    plot: Plot,
    characters: Set[Character],
    caseFiles: Set[CaseFile],
    solution: Solution
) extends Case derives ReadWriter
