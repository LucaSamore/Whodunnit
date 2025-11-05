package model

import model.casegeneration.Case
import model.knowledgegraph.Graph
import model.versioning.{History, TimeMachine}

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
