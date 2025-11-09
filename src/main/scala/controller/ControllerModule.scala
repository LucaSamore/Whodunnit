package controller

import model.ModelModule
import model.game.GameState

object ControllerModule:

  trait Controller:
    def state: GameState
    def currentGameState: GameState = state

  abstract class AbstractController(
      protected val model: ModelModule.Model
  ) extends Controller:
    override def state: GameState = model.state

  trait Provider:
    def homePageController: HomePageController
    def caseGenerationController: CaseGenerationController
    def gameBoardController: GameBoardController

  type Requirements = model.ModelModule.Provider & view.ViewModule.Provider

  trait Component:
    context: Requirements =>

    protected def createHomePageController(): HomePageController =
      HomePageController(context.model)

    protected def createCaseGenerationController(): CaseGenerationController =
      CaseGenerationController(context.model)

    protected def createGameBoardController(): GameBoardController =
      GameBoardController(context.model)

  trait Interface extends Provider with Component:
    self: Requirements =>

    override lazy val homePageController: HomePageController =
      createHomePageController()
    override lazy val caseGenerationController: CaseGenerationController =
      createCaseGenerationController()
    override lazy val gameBoardController: GameBoardController =
      createGameBoardController()
