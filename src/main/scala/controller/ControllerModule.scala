package controller

import model.casegeneration.Constraint

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
      def onPlayNowClicked(): Unit =
        println("[Controller] Play Now button clicked!")

      def onPlayClicked(difficulty: String, theme: String): Unit =
        println(
          s"[Controller] Play clicked with difficulty: $difficulty and theme: $theme"
        )

        val themeConstraint = Constraint.Theme(theme)
        val difficultyConstraint = difficulty match
          case "Easy" => Constraint.Difficulty.Easy
          case "Medium" => Constraint.Difficulty.Medium
          case "Hard" => Constraint.Difficulty.Hard
          case _ => Constraint.Difficulty.Easy

        val constraints = Seq(themeConstraint, difficultyConstraint)

        import cats.effect.unsafe.implicits.global
        context.model.generateCase(constraints).unsafeRunAsync {
          case Right(Right(generatedCase)) =>
            println(s"[Controller] Case generated: ${generatedCase.plot.title}")
          case Right(Left(error)) =>
            println(s"[Controller] Error: $error")
          case Left(exception) =>
            println(s"[Controller] Exception: ${exception.getMessage}")
        }

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    def Controller(): Controller[S] = new ControllerImpl()
