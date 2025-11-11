package controller

import model.ModelModule
import model.game.GameState

object ControllerModule:

  trait Controller:
    def state: GameState

    def currentGameState: GameState = state

  abstract class AbstractController(protected val model: ModelModule.Model) extends Controller:
    override def state: GameState = model.state

  trait Provider:
    def homePageController: HomePageController

    def gameInitializationController: GameInitializationController

    def gameBoardController: GameBoardController

    def cluesManagementController: CluesManagementController

  type Requirements = model.ModelModule.Provider & view.ViewModule.Provider

  trait Component:
    context: Requirements =>

    protected def createHomePageController(): HomePageController = HomePageController(context.model)

    protected def createGameInitializationController(): GameInitializationController =
      GameInitializationController(context.model)

    protected def createGameBoardController(): GameBoardController = GameBoardController(context.model)

    protected def createCluesManagementController(): CluesManagementController =
      CluesManagementController(context.model)

  trait Interface extends Provider with Component:
    self: Requirements =>

    override lazy val homePageController: HomePageController = createHomePageController()

    override lazy val gameInitializationController: GameInitializationController = createGameInitializationController()

    override lazy val gameBoardController: GameBoardController = createGameBoardController()

    override lazy val cluesManagementController: CluesManagementController = createCluesManagementController()
