package controller

import cats.effect.unsafe.implicits.global
import model.GameState
import model.knowledgegraph.CaseKnowledgeGraph

object ControllerModule:

  trait Controller[S]:
    def onPlayNowClicked(): Unit
    def onPlayClicked(
        difficulty: String,
        theme: String,
        onSuccess: () => Unit,
        onError: String => Unit
    ): Unit

    def currentGameState: GameState

  trait Provider[S]:
    val controller: Controller[S]

  type Requirements[S] =
    view.ViewModule.Provider[S] with model.ModelModule.Provider[S]

  trait Component[S]:
    context: Requirements[S] =>

    class ControllerImpl extends Controller[S]:

      import _root_.model.casegeneration.Constraint

      def onPlayNowClicked(): Unit =
        println("[Controller] Play Now button clicked!")

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

        context.model.generateNewCase(
          themeOption,
          difficultyConstraint
        ).unsafeRunAsync {
          case Right(result) =>
            result match
              case Right(generatedCase) =>
                println(
                  s"[Controller] Case generated successfully: ${generatedCase.plot.title}"
                )
                // garbage
                context.model.gameState.investigativeCase = Some(generatedCase)
                context.model.gameState.graph = Some(
                  new CaseKnowledgeGraph().withNodes(
                    generatedCase.characters.toSeq: _*
                  )
                )
                onSuccess()
              case Left(error) =>
                println(s"[Controller] Error during generation: $error")
                onError(error.message)
          case Left(exception) =>
            println(s"[Controller] Exception: ${exception.getMessage}")
            onError(s"Exception: ${exception.getMessage}")
        }

      override def currentGameState: GameState = context.model.gameState

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    def Controller(): Controller[S] = new ControllerImpl()
