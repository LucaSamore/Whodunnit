package controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import model.ModelModule
import model.generation.*
import model.game.*
import model.generation.Constraint.Context
import model.hint.HintEngine
import model.hint.Rules.stableDensity
import model.generation.Producers.given

import scala.concurrent.duration.*

// TODO: Change name to something like "GamaInitializationController",
//  as this controller is not only responsible for the case generation,
//  but also for the initialization of other game objects
trait CaseGenerationController extends ControllerModule.Controller:

  def onPlayClicked(
      difficulty: String,
      theme: String,
      onSuccess: () => Unit,
      onError: String => Unit
  ): Unit

object CaseGenerationController:
  def apply(model: ModelModule.Model): CaseGenerationController =
    new CaseGenerationControllerImpl(model)

  private class CaseGenerationControllerImpl(
      model: ModelModule.Model
  ) extends ControllerModule.AbstractController(model)
      with CaseGenerationController:

    private val caseProducer: Producer[Case] = summon[Producer[Case]]

    private def generateCase(
        theme: Option[String],
        difficulty: Constraint.Difficulty,
        customConstraints: Seq[Constraint] = Seq.empty
    ): IO[Either[ProductionError, Case]] =
      val baseConstraints = Seq(difficulty) ++ customConstraints
      val allConstraints = theme match
        case Some(t) => baseConstraints :+ Constraint.Theme(t)
        case None    => baseConstraints
      IO(caseProducer.produce(allConstraints*))

    def onPlayClicked(
        difficulty: String,
        theme: String,
        onSuccess: () => Unit,
        onError: String => Unit
    ): Unit =
      println(
        s"[Controller] Play clicked with difficulty: $difficulty and theme: $theme"
      )

      val themeOption = if theme.isEmpty then None else Some(theme)
      val difficultyConstraint = difficulty match
        case "Easy"   => Constraint.Difficulty.Easy
        case "Medium" => Constraint.Difficulty.Medium
        case "Hard"   => Constraint.Difficulty.Hard
        case _        => Constraint.Difficulty.Easy

      generateCase(
        themeOption,
        difficultyConstraint
      )
        .unsafeRunAsync {
          case Right(result) =>
            result match
              case Right(generatedCase) =>
                println(
                  s"[Controller] Case generated successfully: ${generatedCase.plot.title}"
                )
                val graph =
                  new CaseKnowledgeGraph().withNodes(
                    generatedCase.characters.toSeq: _*
                  )
                val timer = Timer(
                  totalDuration = 30.seconds,
                  triggers = List(
                    TriggerEvent(
                      20.seconds,
                      () => {
                        val history = model.state.history.get.states.toList

                        // TODO: may be empty -> check required
                        val hintOpt = HintEngine.evaluate(history)(using stableDensity)

                        // We assume hintOpt is not empty, just for test purposes
                        println(s"Generated Hint of kind: ${hintOpt.get.toString}")

                        val hint = Hint(
                          hintOpt.get,
                          Context(model.state.investigativeCase.get.plot.content)
                        ).toOption.get

                        model.addHint(hint)
                      }
                    ),
                    TriggerEvent(
                      10.seconds,
                      () => {
                        println("Hello! Trigger 2 has been fired")
                      }
                    )
                  )
                )
                model.updateState(_ =>
                  GameState.initialize(
                    generatedCase,
                    timer,
                    graph
                  )
                )
                model.startTimer()
                onSuccess()
              case Left(error) =>
                println(s"[Controller] Error during generation: $error")
                onError(error.message)
          case Left(exception) =>
            println(s"[Controller] Exception: ${exception.getMessage}")
            onError(s"Exception: ${exception.getMessage}")
        }
