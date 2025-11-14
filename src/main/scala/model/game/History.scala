package model.game

import model.versioning.RingNavigableBuffer

/** Represents a timeline of knowledge graph states with undo/redo capabilities.
  *
  * History maintains a sequence of CaseKnowledgeGraph states, allowing players to navigate back and forth through their
  * investigation progress. This enables undo/redo functionality and provides a complete audit trail of player actions.
  */
trait History:
  /** Adds a new knowledge graph state to the history.
    *
    * If the cursor is not at the most recent position (i.e., the player has undone some actions), adding a new state
    * will discard all "future" states.
    *
    * @param kg
    *   the knowledge graph to add
    * @return
    *   a new History instance with the added state
    */
  def addState(kg: CaseKnowledgeGraph): History

  /** Moves back to the previous state in the history.
    *
    * @return
    *   a tuple containing the new History and the previous state (if available)
    */
  def undo(): (History, Option[CaseKnowledgeGraph])

  /** Moves forward to the next state in the history.
    *
    * This only works if the player has previously undone actions.
    *
    * @return
    *   a tuple containing the new History and the next state (if available)
    */
  def redo(): (History, Option[CaseKnowledgeGraph])

  /** Returns the current knowledge graph state.
    *
    * @return
    *   Some(graph) if history is not empty, None otherwise
    */
  def currentState: Option[CaseKnowledgeGraph]

  /** Creates a deep copy of this history.
    *
    * All knowledge graphs are copied, and the cursor position is preserved.
    *
    * @return
    *   a new History instance that is a deep copy of this one
    */
  def deepCopy(): History

  /** Returns all states in the history.
    *
    * @return
    *   a sequence of all knowledge graphs in chronological order
    */
  def states: Seq[CaseKnowledgeGraph]

  /** Checks if an undo operation is possible.
    *
    * @return
    *   true if there is a previous state to undo to, false otherwise
    */
  def canUndo: Boolean

  /** Checks if a redo operation is possible.
    *
    * @return
    *   true if there is a next state to redo to, false otherwise
    */
  def canRedo: Boolean

/** Implementation of History using a ring navigable buffer.
  *
  * GameHistory stores knowledge graph states in a circular buffer with inverse navigation. The most recent state is at
  * position 0, and older states are at higher positions. When the buffer is full, the oldest state is discarded.
  *
  * @param historySize
  *   the maximum number of states to keep
  * @param timeline
  *   the underlying buffer storing the states
  */
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

  override def redo(): (History, Option[CaseKnowledgeGraph]) =
    val newBuffer = cloneBuffer()
    val moved = newBuffer.moveForward()
    val state = if moved then newBuffer.currentElement else None
    (GameHistory(historySize, newBuffer), state)

  override def currentState: Option[CaseKnowledgeGraph] =
    timeline.currentElement

  override def deepCopy(): History =
    val newBuffer = RingNavigableBuffer[CaseKnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(kg => newBuffer.push(kg.deepCopy()))
    // Restore cursor position
    (0 until timeline.currentPosition).foreach(_ => newBuffer.moveBackward())
    GameHistory(historySize, newBuffer)

  override def states: Seq[CaseKnowledgeGraph] = timeline.elements

  override def canUndo: Boolean =
    timeline.size > 1 && timeline.currentPosition < timeline.size - 1

  override def canRedo: Boolean =
    timeline.currentPosition > 0

  /** Checks equality with another object.
    *
    * Two GameHistory instances are equal if they have the same history size, the same elements in the same order, and
    * the same cursor position.
    *
    * @param obj
    *   the object to compare with
    * @return
    *   true if equal, false otherwise
    */
  override def equals(obj: Any): Boolean = obj match
    case that: GameHistory =>
      this.historySize == that.historySize &&
      this.timeline.elements == that.timeline.elements &&
      this.timeline.currentPosition == that.timeline.currentPosition
    case _ => false

  /** Creates a shallow clone of the underlying buffer.
    *
    * This method copies the buffer structure and preserves the cursor position, but does not perform deep copies of the
    * knowledge graphs themselves.
    *
    * @return
    *   a new buffer with the same elements and cursor position
    */
  private def cloneBuffer(): RingNavigableBuffer[CaseKnowledgeGraph] =
    val newBuffer = RingNavigableBuffer[CaseKnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(newBuffer.push)
    // Restore cursor position
    (0 until timeline.currentPosition).foreach(_ => newBuffer.moveBackward())
    newBuffer

  /** Returns a string representation of this history.
    *
    * @return
    *   a string showing the history size, element count, cursor position, and all elements
    */
  override def toString: String = {
    val elements = timeline.elements.map(_.toString).mkString("\n")
    s"GameHistory(size=$historySize, elementsize=${timeline.elements.size} ,currentPosition=${timeline.currentPosition}, elements=\n${elements}\n)"
  }

/** Companion object providing factory methods for creating GameHistory instances. */
object GameHistory:
  /** Creates a new empty GameHistory with the specified capacity.
    *
    * @param historySize
    *   the maximum number of states to store
    * @return
    *   a new empty GameHistory instance
    */
  def apply(historySize: Int): History =
    GameHistory(
      historySize,
      RingNavigableBuffer[CaseKnowledgeGraph](historySize)
    )
