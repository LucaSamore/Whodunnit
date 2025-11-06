package controller

import cats.effect.unsafe.implicits.global

object ControllerModule:

  trait Controller[S]:
    def onPlayNowClicked(): Unit
    def onPlayClicked(difficulty: String, theme: String): Unit

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

      def onPlayClicked(difficulty: String, theme: String): Unit =
        println(
          s"[Controller] Play clicked with difficulty: $difficulty and theme: $theme"
        )

        val themeOption = if theme.isEmpty then None else Some(theme)
        val difficultyConstraint = difficulty match
          case "Easy"   => Constraint.Difficulty.Easy
          case "Medium" => Constraint.Difficulty.Medium
          case "Hard"   => Constraint.Difficulty.Hard
          case _        => Constraint.Difficulty.Easy

        // Ora la generazione è delegata completamente al model
        context.model.generateNewCase(
          themeOption,
          difficultyConstraint
        ).unsafeRunAsync {
          case Right(Right(generatedCase)) =>
            println(
              s"[Controller] Case generato con successo: ${generatedCase.plot.title}"
            )
          case Right(Left(error)) =>
            println(s"[Controller] Errore durante la generazione: $error")
          case Left(exception) =>
            println(s"[Controller] Eccezione: ${exception.getMessage}")
        }

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    def Controller(): Controller[S] = new ControllerImpl()
