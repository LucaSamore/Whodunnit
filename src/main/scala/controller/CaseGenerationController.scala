package controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import model.ModelModule
import model.generation.*
import model.game.*
import model.generation.Producers.given
import scala.concurrent.duration.*

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
                val timer = Timer(30.seconds, List.empty)
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
