package model.game

import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History

case class GameState(
    investigativeCase: Option[Case] = None,
    history: Option[History] = None,
    timeMachine: Option[TimeMachine[History]] = None,
    hints: Option[Seq[Hint]] = None,
    timer: Option[Timer] = None
):

  def withCase(c: Case): GameState =
    copy(investigativeCase = Some(c))

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

  def updateHistory(f: History => History): GameState =
    copy(history = history.map(f))

  def updateTimeMachine(f: TimeMachine[History] => TimeMachine[History])
      : GameState =
    copy(timeMachine = timeMachine.map(f))

  def addGraphToHistory(graph: CaseKnowledgeGraph): GameState =
    history match
      case Some(h) =>
        val newHistory = h.deepCopy()
        copy(history = Some(newHistory.addState(graph)))
      case None =>
        val newHistory = GameHistory(5)
        copy(history = Some(newHistory.addState(graph)))

  def currentGraph: Option[CaseKnowledgeGraph] =
    history.flatMap(_.currentState)

object GameState:
  def empty(): GameState =
    GameState(None, None, None, None, None)

  def initialize(
      gameCase: Case,
      timer: Timer,
      initialGraph: CaseKnowledgeGraph
  ): GameState =
    GameState(
      Some(gameCase),
      Some(GameHistory(5).addState(initialGraph)),
      Some(GameTimeMachine[History](None)),
      Some(Seq.empty),
      Some(timer)
    )
