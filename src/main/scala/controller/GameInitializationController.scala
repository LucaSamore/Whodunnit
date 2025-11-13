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

trait GameInitializationController extends ControllerModule.Controller:
  def initGame(theme: Theme, difficulty: Difficulty)(onSuccess: () => Unit, onError: String => Unit): Unit

object GameInitializationController:
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
        timer = Timer(
          totalDuration = 5.minutes,
          triggers = List(
            Trigger(3.minutes, () => sendHint(stableDensity)),
            Trigger(2.seconds, () => sendHint(increasingCoverage(generatedCase.solution.prerequisite)))
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
        case Success(Right(hint)) => println(hint.description); model.updateState(_.addHint(hint))
        case Success(Left(error)) => println(s"[Warning] Failed to generate hint: ${error.message}")
        case Failure(exception)   => println(s"[Error] Unexpected error generating hint: ${exception.getMessage}")
