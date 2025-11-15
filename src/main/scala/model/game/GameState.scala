package model.game

import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History

/** Represents the complete state of an active game session.
  *
  * GameState is an immutable container that holds all the components of an ongoing investigation game, including the
  * case details, history of player actions, time-travel capabilities, hints, timer, and submission status.
  *
  * All update operations return a new GameState instance, following functional programming principles.
  *
  * @param investigativeCase
  *   the case being investigated
  * @param history
  *   the timeline of knowledge graph states
  * @param timeMachine
  *   the snapshot manager for save/restore functionality
  * @param hints
  *   the collection of hints provided to the player
  * @param timer
  *   the game timer tracking elapsed time
  * @param submissionState
  *   the current state of solution submission
  */
case class GameState(
    investigativeCase: Option[Case] = None,
    history: Option[History] = None,
    timeMachine: Option[TimeMachine[History]] = None,
    hints: Option[Seq[Hint]] = None,
    timer: Option[TimerExecutor] = None,
    submissionState: Option[SubmissionState] = None
):

  /** Returns the current knowledge graph from the history.
    *
    * @return
    *   Some(graph) if history exists and has a current state, None otherwise
    */
  def currentGraph: Option[CaseKnowledgeGraph] =
    history.flatMap(_.currentState)

  /** Creates a new GameState with the specified case.
    *
    * @param c
    *   the case to set
    * @return
    *   a new GameState with the updated case
    */
  def withCase(c: Case): GameState =
    copy(investigativeCase = Some(c))

  /** Creates a new GameState with the specified history.
    *
    * @param h
    *   the history to set
    * @return
    *   a new GameState with the updated history
    */
  def withHistory(h: History): GameState =
    copy(history = Some(h))

  /** Creates a new GameState with the specified time machine.
    *
    * @param tm
    *   the time machine to set
    * @return
    *   a new GameState with the updated time machine
    */
  def withTimeMachine(tm: TimeMachine[History]): GameState =
    copy(timeMachine = Some(tm))

  /** Creates a new GameState with the specified hints.
    *
    * @param h
    *   the sequence of hints to set
    * @return
    *   a new GameState with the updated hints
    */
  def withHints(h: Seq[Hint]): GameState =
    copy(hints = Some(h))

  /** Creates a new GameState with the specified timer.
    *
    * @param t
    *   the timer to set
    * @return
    *   a new GameState with the updated timer
    */
  def withTimer(t: TimerExecutor): GameState =
    copy(timer = Some(t))

  /** Creates a new GameState with the specified submission state.
    *
    * @param state
    *   the submission state to set
    * @return
    *   a new GameState with the updated submission state
    */
  def withSubmissionState(state: SubmissionState): GameState =
    copy(submissionState = Some(state))

  /** Adds a hint to the existing hints collection.
    *
    * @param hint
    *   the hint to add
    * @return
    *   a new GameState with the added hint
    */
  def addHint(hint: Hint): GameState =
    copy(hints = Some(hints.getOrElse(Seq.empty) :+ hint))

  /** Clears all hints.
    *
    * @return
    *   a new GameState with an empty hints collection
    */
  def clearHints: GameState =
    copy(hints = Some(Seq.empty))

  /** Applies a transformation function to the history.
    *
    * @param f
    *   the function to transform the history
    * @return
    *   a new GameState with the updated history
    */
  def updateHistory(f: History => History): GameState =
    copy(history = history.map(f))

  /** Applies a transformation function to the time machine.
    *
    * @param f
    *   the function to transform the time machine
    * @return
    *   a new GameState with the updated time machine
    */
  def updateTimeMachine(f: TimeMachine[History] => TimeMachine[History])
      : GameState =
    copy(timeMachine = timeMachine.map(f))

  /** Adds a knowledge graph to the history.
    *
    * If no history exists, creates a new history with default capacity of 5. Otherwise, creates a deep copy of the
    * existing history and adds the graph.
    *
    * @param graph
    *   the knowledge graph to add
    * @return
    *   a new GameState with the updated history
    */
  def addGraphToHistory(graph: CaseKnowledgeGraph): GameState =
    history match
      case Some(h) =>
        val newHistory = h.deepCopy()
        copy(history = Some(newHistory.addState(graph)))
      case None =>
        val newHistory = GameHistory(5)
        copy(history = Some(newHistory.addState(graph)))

/** Companion object providing factory methods for creating GameState instances. */
object GameState:
  /** Creates an empty GameState with all fields set to None.
    *
    * @return
    *   an empty GameState
    */
  def empty(): GameState =
    GameState(None, None, None, None, None, None)

  /** Initializes a new game session with the specified case, timer, and initial graph.
    *
    * Creates a fully configured GameState with:
    *   - The specified case
    *   - A new history (capacity 5) containing the initial graph
    *   - A new time machine
    *   - An empty hints collection
    *   - The specified timer
    *   - Submission state set to NotSubmitted
    *
    * @param gameCase
    *   the case to investigate
    * @param timer
    *   the game timer
    * @param initialGraph
    *   the initial knowledge graph state
    * @return
    *   a fully initialized GameState ready for play
    */
  def initialize(
      gameCase: Case,
      timer: TimerExecutor,
      initialGraph: CaseKnowledgeGraph
  ): GameState =
    GameState(
      Some(gameCase),
      Some(GameHistory(5).addState(initialGraph)),
      Some(GameTimeMachine[History](None)),
      Some(Seq.empty),
      Some(timer),
      Some(SubmissionState.NotSubmitted)
    )
