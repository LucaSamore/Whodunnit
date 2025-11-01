package model.versioning

trait KnowledgeGraph:
  def deepCopy(): KnowledgeGraph

case class GameHistory(
    private val historySize: Int,
    private val timeline: RingNavigableBuffer[KnowledgeGraph]
):
  def currentState: Option[KnowledgeGraph] = timeline.currentElement

  def addState(state: KnowledgeGraph): Unit =
    timeline.push(state)

  def undo(): Option[KnowledgeGraph] =
    Option.when(timeline.moveBackward())(timeline.currentElement).flatten

  def redo(): Option[KnowledgeGraph] =
    Option.when(timeline.moveForward())(timeline.currentElement).flatten

  def deepCopy(): GameHistory =
    val newBuffer = RingNavigableBuffer[KnowledgeGraph](timeline.capacity)
    timeline.elements.foreach(kg => newBuffer.push(kg.deepCopy()))
    GameHistory(historySize, newBuffer)

  override def equals(obj: Any): Boolean = obj match
    case that: GameHistory =>
      this.historySize == that.historySize &&
      this.timeline.elements == that.timeline.elements &&
      this.timeline.currentPosition == that.timeline.currentPosition
    case _ => false

object GameHistory:
  def apply(historySize: Int): GameHistory =
    GameHistory(historySize, RingNavigableBuffer[KnowledgeGraph](historySize))

case class HistoryTimeMachine[S: Snapshottable](
    private var currentSnapshot: Option[Snapshot[S]] = None
):

  def save(state: S): Unit =
    currentSnapshot = Some(Snapshot(state))

  def restore(): Option[S] =
    currentSnapshot.map(Snapshot.restore)

  def clear(): Unit =
    currentSnapshot = None

  def hasSnapshot: Boolean = currentSnapshot.isDefined
