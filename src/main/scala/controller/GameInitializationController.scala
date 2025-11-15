package controller

import model.ModelModule
import model.generation.*
import model.game.*
import model.hint.{HintEngine, Rule}
import model.hint.Rules.{increasingCoverage, stableDensity}
import model.generation.Producers.given
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.{Failure, Success}

/** Controller responsible for initializing new game sessions.
  *
  * This controller handles the complete game initialization workflow, including case generation, game state setup,
  * timer configuration, and hint trigger registration.
  *
  * @see
  *   [[model.game.Case]] for the case structure
  * @see
  *   [[model.generation.Producer]] for case generation mechanisms
  * @see
  *   [[model.game.GameState]] for game state management
  */
trait GameInitializationController extends ControllerModule.Controller:
  /** Initializes a new game session with the specified theme and difficulty.
    *
    * This method triggers case generation, initializes the game state with a knowledge graph, configures the game timer
    * with hint triggers, and invokes the appropriate callback based on the generation outcome.
    *
    * @param theme
    *   the thematic constraint for case generation (e.g., "Murder", "Theft")
    * @param difficulty
    *   the difficulty level affecting case complexity
    * @param onSuccess
    *   callback invoked when initialization completes successfully
    * @param onError
    *   callback invoked with an error message if initialization fails
    */
  def initGame(theme: Theme, difficulty: Difficulty)(onSuccess: () => Unit, onError: String => Unit): Unit

/** Companion object providing factory methods for GameInitializationController. */
object GameInitializationController:
  /** Creates a new GameInitializationController instance.
    *
    * @param model
    *   the model instance to use for state management
    * @return
    *   a new GameInitializationController
    */
  def apply(model: ModelModule.Model): GameInitializationController = new CaseGenerationControllerImpl(model)

  private final class CaseGenerationControllerImpl(model: ModelModule.Model)
      extends ControllerModule.AbstractController(model) with GameInitializationController:

    private given ExecutionContext = ExecutionContext.global

    override def initGame(theme: Theme, difficulty: Difficulty)(onSuccess: () => Unit, onError: String => Unit): Unit =
      summon[Producer[Case]]
        .produceAsync(theme, difficulty)
        .onComplete:
          case Success(Right(generatedCase)) =>
            initializeGameState(generatedCase)
            model.updateState(state => { state.timer.foreach(_.start()); state })
            onSuccess()
          case Success(Left(error)) => onError(error.message)
          case Failure(exception)   => onError(s"Unexpected error: ${exception.getMessage}")

    private def initializeGameState(generatedCase: Case): Unit = model.updateState(_ =>
      GameState.initialize(
        gameCase = generatedCase,
        initialGraph = CaseKnowledgeGraph().withNodes(generatedCase.characters.toSeq*),
        timer = TimerExecutor(
          totalDuration = 30.minutes,
          triggers = List(
            Trigger(20.minutes, () => sendHint(stableDensity)),
            Trigger(10.minutes, () => sendHint(increasingCoverage(generatedCase.solution.prerequisite)))
          )
        )
      )
    )

    private def sendHint(rule: Rule[BaseOrientedGraph]): Unit = for
      history <- model.state.history.map(_.states.toList)
      hintKind <- HintEngine.evaluate(history)(using rule)
      investigativeCase <- model.state.investigativeCase
    do
      summon[Producer[Hint]].produceAsync(
        hintKind,
        Context(s"Case Plot: ${investigativeCase.plot.content}"),
        Context(s"Player Knowledge Graph: ${history.last}")
      ).onComplete:
        case Success(Right(hint)) => println(s"[Controller] ${hint.description}"); model.updateState(_.addHint(hint))
        case Success(Left(error)) => println(s"[Warning] Failed to generate hint: ${error.message}")
        case Failure(exception)   => println(s"[Error] Unexpected error generating hint: ${exception.getMessage}")
