package controller

import model.ModelModule
import model.generation.*
import model.game.*
import model.hint.HintEngine
import model.hint.Rules.stableDensity
import model.generation.Producers.given

import scala.concurrent.duration.*

trait GameInitializationController extends ControllerModule.Controller:

  def initGame(theme: Theme, difficulty: Difficulty)(onSuccess: () => Unit, onError: String => Unit): Unit

object GameInitializationController:
  def apply(model: ModelModule.Model): GameInitializationController = new CaseGenerationControllerImpl(model)

  private final class CaseGenerationControllerImpl(model: ModelModule.Model)
      extends ControllerModule.AbstractController(model) with GameInitializationController:

    override def initGame(theme: Theme, difficulty: Difficulty)(onSuccess: () => Unit, onError: String => Unit): Unit =
      println(s"[Controller] Play clicked with difficulty: $difficulty and theme: $theme")
      summon[Producer[Case]]
        .produce(theme, difficulty)
        .fold(
          error => onError(error.message),
          generatedCase =>
            println(s"[Controller] Case generated successfully: ${generatedCase.plot.title}")
            initializeGameState(generatedCase)
            onSuccess()
        )

    private def initializeGameState(generatedCase: Case): Unit = model.updateState(_ =>
      GameState.initialize(
        gameCase = generatedCase,
        initialGraph = CaseKnowledgeGraph().withNodes(generatedCase.characters.toSeq*),
        timer = Timer(
          totalDuration = 30.seconds,
          triggers = List(
            Trigger(20.seconds, Triggers.sendHint),
            Trigger(10.seconds, Triggers.sendHint)
          )
        )
      )
    )
    model.startTimer()

    private object Triggers:

      def sendHint(): Unit = for
        history <- model.state.history.map(_.states.toList)
        hintKind <- HintEngine.evaluate(history)(using stableDensity) // Not really the best, but okay for now...
        investigativeCase <- model.state.investigativeCase
        hint <- Hint(
          hintKind,
          Context(s"Case Plot: ${investigativeCase.plot.content}"),
          Context(s"Player Knowledge Graph: ${history.last}")
        ).toOption
      do model.updateState(_.addHint(hint))
