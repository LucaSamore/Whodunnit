package controller

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import model.generation.*
import model.game.*
import model.generation.Producers.given

import scala.concurrent.duration.DurationInt

trait CaseGenerationController[S]
    extends ControllerModule.Controller[S]:

  def onPlayClicked(
      difficulty: String,
      theme: String,
      onSuccess: () => Unit,
      onError: String => Unit
  ): Unit

object CaseGenerationController:
  def apply[S](gameState: GameState): CaseGenerationController[S] =
    new CaseGenerationControllerImpl[S](gameState)

  private class CaseGenerationControllerImpl[S](gameState: GameState)
      extends ControllerModule.AbstractController[S](gameState)
      with CaseGenerationController[S]:

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
      ).unsafeRunAsync {
        case Right(result) =>
          result match
            case Right(generatedCase) =>
              println(
                s"[Controller] Case generated successfully: ${generatedCase.plot.title}"
              )
              //TODO Can we use model.gameState.initialize(...) -> add 'model' parameter?
              gameState.timer = Some(Timer(30.seconds, List.empty))
              gameState.investigativeCase = Some(generatedCase)
              gameState.graph = Some(
                new CaseKnowledgeGraph().withNodes(
                  generatedCase.characters.toSeq: _*
                )
              )
              gameState.timer.foreach(_.start())
              onSuccess()
            case Left(error) =>
              println(s"[Controller] Error during generation: $error")
              onError(error.message)
        case Left(exception) =>
          println(s"[Controller] Exception: ${exception.getMessage}")
          onError(s"Exception: ${exception.getMessage}")
      }
