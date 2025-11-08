package controller

import model.game.GameState

object ControllerModule:

  trait Controller[S]:
    def currentGameState: GameState

  abstract class AbstractController[S](protected val gameState: GameState)
      extends Controller[S]:
    override def currentGameState: GameState = gameState

  trait Provider[S]:
    def homePageController: HomePageController[S]
    def caseGenerationController: CaseGenerationController[S]

  type Requirements[S] =
    model.ModelModule.Provider[S] & view.ViewModule.Provider[S]

  trait Component[S]:
    context: Requirements[S] =>

    protected def createHomePageController(): HomePageController[S] =
      HomePageController[S](context.model.gameState)

    protected def createCaseGenerationController()
        : CaseGenerationController[S] =
      CaseGenerationController[S](context.model.gameState)

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    override lazy val homePageController: HomePageController[S] =
      createHomePageController()
    override lazy val caseGenerationController: CaseGenerationController[S] =
      createCaseGenerationController()
