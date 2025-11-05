package model

import model.casegeneration.Case
import model.knowledgegraph.{CaseKnowledgeGraph, Graph}
import model.versioning.{GameHistory, GameTimeMachine, History, TimeMachine}
import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History

// TODO: Define Timer and Hint properly
type Timer = Int
type Hint = Set[String]

case class GameState(
    var investigativeCase: Option[Case],
    var graph: Option[Graph],
    var history: Option[History],
    var timeMachine: Option[TimeMachine[History]],
    var hints: Option[Seq[Hint]],
    var timer: Option[Timer]
)

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
