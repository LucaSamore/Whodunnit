package model.versioning

import model.knowledgegraph.CaseKnowledgeGraph

type KnowledgeGraph = CaseKnowledgeGraph

trait History:
  def addState(kg: KnowledgeGraph): Unit
  def undo(): Option[KnowledgeGraph]
  def redo(): Option[KnowledgeGraph]
  def currentState: Option[KnowledgeGraph]
  def deepCopy(): History
  def states: Seq[KnowledgeGraph]

case class GameHistory(
    private val historySize: Int,
    private val timeline: RingNavigableBuffer[KnowledgeGraph]
) extends History:
  override def addState(kg: KnowledgeGraph): Unit =
    timeline.push(kg)

  override def undo(): Option[KnowledgeGraph] =
    Option.when(timeline.moveBackward())(timeline.currentElement).flatten

  override def redo(): Option[KnowledgeGraph] =
    Option.when(timeline.moveForward())(timeline.currentElement).flatten

  override def currentState: Option[KnowledgeGraph] = timeline.currentElement

  override def deepCopy(): History =
    val newBuffer = RingNavigableBuffer[KnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(kg => newBuffer.push(kg.deepCopy()))
    GameHistory(historySize, newBuffer)

  override def states: Seq[KnowledgeGraph] = timeline.elements

  override def equals(obj: Any): Boolean = obj match
    case that: GameHistory =>
      this.historySize == that.historySize &&
      this.timeline.elements == that.timeline.elements &&
      this.timeline.currentPosition == that.timeline.currentPosition
    case _ => false

object GameHistory:
  def apply(historySize: Int): History =
    GameHistory(historySize, RingNavigableBuffer[KnowledgeGraph](historySize))
