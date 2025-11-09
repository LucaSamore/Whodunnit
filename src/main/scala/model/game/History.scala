package model.game

import model.versioning.RingNavigableBuffer

trait History:
  def addState(kg: CaseKnowledgeGraph): History
  def undo(): (History, Option[CaseKnowledgeGraph])
  def redo(): Option[CaseKnowledgeGraph]
  def currentState: Option[CaseKnowledgeGraph]
  def deepCopy(): History
  def states: Seq[CaseKnowledgeGraph]

case class GameHistory(
    private val historySize: Int,
    private val timeline: RingNavigableBuffer[CaseKnowledgeGraph]
) extends History:
  override def addState(kg: CaseKnowledgeGraph): History =
    val newBuffer = cloneBuffer()
    newBuffer.push(kg)
    GameHistory(historySize, newBuffer)

  override def undo(): (History, Option[CaseKnowledgeGraph]) =
    val newBuffer = cloneBuffer()
    val moved = newBuffer.moveBackward()
    val state = if moved then newBuffer.currentElement else None
    (GameHistory(historySize, newBuffer), state)

  override def redo(): Option[CaseKnowledgeGraph] =
    Option.when(timeline.moveForward())(timeline.currentElement).flatten

  override def currentState: Option[CaseKnowledgeGraph] =
    timeline.currentElement

  override def deepCopy(): History =
    val newBuffer = RingNavigableBuffer[CaseKnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(kg => newBuffer.push(kg.deepCopy()))
    GameHistory(historySize, newBuffer)

  override def states: Seq[CaseKnowledgeGraph] = timeline.elements

  override def equals(obj: Any): Boolean = obj match
    case that: GameHistory =>
      this.historySize == that.historySize &&
      this.timeline.elements == that.timeline.elements &&
      this.timeline.currentPosition == that.timeline.currentPosition
    case _ => false

  private def cloneBuffer(): RingNavigableBuffer[CaseKnowledgeGraph] =
    val newBuffer = RingNavigableBuffer[CaseKnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(newBuffer.push)
    // Restore cursor position
    (0 until timeline.currentPosition).foreach(_ => newBuffer.moveBackward())
    newBuffer

object GameHistory:
  def apply(historySize: Int): History =
    GameHistory(
      historySize,
      RingNavigableBuffer[CaseKnowledgeGraph](historySize)
    )
