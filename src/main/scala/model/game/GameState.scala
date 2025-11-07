package model.game

import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History

trait State:
  def getState: GameState
  def reset: GameState

case class GameState(
    var investigativeCase: Option[Case],
    var graph: Option[CaseKnowledgeGraph],
    var history: Option[History],
    var timeMachine: Option[TimeMachine[History]],
    var hints: Option[Seq[Hint]],
    var timer: Option[Timer]
) extends State:
  override def getState: GameState = this

  override def reset: GameState =
    GameState.empty()

  override def equals(obj: Any): Boolean = obj match
    case that: GameState =>
      this.investigativeCase == that.investigativeCase &&
      this.graph.isDefined == that.graph.isDefined &&
      this.history.isDefined == that.history.isDefined &&
      this.timeMachine.isDefined == that.timeMachine.isDefined &&
      this.hints == that.hints &&
      this.timer == that.timer
    case _ => false

object GameState:
  def empty(): GameState =
    GameState(None, None, None, None, None, None)

  def initialize(
      gameCase: Case,
      timer: Timer
  ): GameState =
    GameState(
      Some(gameCase),
      Some(CaseKnowledgeGraph()),
      Some(GameHistory(5)),
      Some(GameTimeMachine[History](None)),
      Some(Seq.empty),
      Some(timer)
    )
