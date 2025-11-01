package model.versioning

trait KnowledgeGraph:
  def deepCopy(): KnowledgeGraph

case class GameHistory(
    private val historySize: Int,
    private val timeline: RingNavigableBuffer[KnowledgeGraph]
):
  def currentState: Option[KnowledgeGraph] = timeline.currentElement

object GameHistory:
  def apply(historySize: Int): GameHistory =
    GameHistory(historySize, RingNavigableBuffer[KnowledgeGraph](historySize))
