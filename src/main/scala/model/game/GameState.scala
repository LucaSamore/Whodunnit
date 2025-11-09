package model.game

import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History

/** Immutable game state representation. All updates return new instances via
  * the copy method or convenience methods.
  */
case class GameState(
    investigativeCase: Option[Case] = None,
    graph: Option[CaseKnowledgeGraph] = None,
    history: Option[History] = None,
    timeMachine: Option[TimeMachine[History]] = None,
    hints: Option[Seq[Hint]] = None,
    timer: Option[Timer] = None
):

  def withCase(c: Case): GameState =
    copy(investigativeCase = Some(c))

  def withGraph(g: CaseKnowledgeGraph): GameState =
    copy(graph = Some(g))

  def withHistory(h: History): GameState =
    copy(history = Some(h))

  def withTimeMachine(tm: TimeMachine[History]): GameState =
    copy(timeMachine = Some(tm))

  def withHints(h: Seq[Hint]): GameState =
    copy(hints = Some(h))

  def withTimer(t: Timer): GameState =
    copy(timer = Some(t))

  def addHint(hint: Hint): GameState =
    copy(hints = Some(hints.getOrElse(Seq.empty) :+ hint))

  def clearHints: GameState =
    copy(hints = Some(Seq.empty))

  def updateGraph(f: CaseKnowledgeGraph => CaseKnowledgeGraph): GameState =
    copy(graph = graph.map(f))

  def updateHistory(f: History => History): GameState =
    copy(history = history.map(f))

  def updateTimeMachine(f: TimeMachine[History] => TimeMachine[History])
      : GameState =
    copy(timeMachine = timeMachine.map(f))

object GameState:
  def empty(): GameState =
    GameState(None, None, None, None, None, None)

  def initialize(
      gameCase: Case,
      timer: Timer,
      graph: CaseKnowledgeGraph
  ): GameState =
    GameState(
      Some(gameCase),
      Some(graph),
      Some(GameHistory(5)),
      Some(GameTimeMachine[History](None)),
      Some(Seq.empty),
      Some(timer)
    )
