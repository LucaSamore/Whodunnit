package controller

import model.ModelModule
import model.game.GameState

/** Module providing the Controller layer of the application architecture.
  *
  * This module provides base abstractions for controllers as well as factory methods for creating concrete controller
  * instances. Controllers serve as intermediaries between the View and Model layers.
  */
object ControllerModule:

  /** Base interface for all controllers.
    *
    * Provides access to the current game state.
    */
  trait Controller:
    /** Returns the current game state.
      *
      * @return
      *   the current GameState
      */
    def state: GameState

  /** Abstract base implementation for controllers.
    *
    * Provides common functionality for accessing the model's state.
    *
    * @param model
    *   the model instance that manages the game state
    */
  abstract class AbstractController(protected val model: ModelModule.Model) extends Controller:
    override def state: GameState = model.state

  /** Provides access to controller instances. */
  trait Provider:
    /** Provides the controller for the home page.
      *
      * @return
      *   the HomePageController instance
      */
    def homePageController: HomePageController

    /** Provides the controller for game initialization.
      *
      * @return
      *   the GameInitializationController instance
      */
    def gameInitializationController: GameInitializationController

    /** Provides the controller for the game board.
      *
      * @return
      *   the GameBoardController instance
      */
    def gameBoardController: GameBoardController

    /** Provides the controller for clues management.
      *
      * @return
      *   the CluesManagementController instance
      */
    def cluesManagementController: CluesManagementController

  /** Dependencies required by this module.
    *
    * The ControllerModule requires access to both the Model and View modules.
    */
  type Requirements = model.ModelModule.Provider & view.ViewModule.Provider

  /** Component providing controller implementations.
    *
    * This component creates controller instances and wires them with their required dependencies from the context.
    */
  trait Component:
    context: Requirements =>

    /** Creates a new HomePageController instance.
      *
      * @return
      *   a new HomePageController
      */
    protected def createHomePageController(): HomePageController = HomePageController(context.model)

    /** Creates a new GameInitializationController instance.
      *
      * @return
      *   a new GameInitializationController
      */
    protected def createGameInitializationController(): GameInitializationController =
      GameInitializationController(context.model)

    /** Creates a new GameBoardController instance.
      *
      * @return
      *   a new GameBoardController
      */
    protected def createGameBoardController(): GameBoardController = GameBoardController(context.model)

    /** Creates a new CluesManagementController instance.
      *
      * @return
      *   a new CluesManagementController
      */
    protected def createCluesManagementController(): CluesManagementController =
      CluesManagementController(context.model)

  /** Complete module interface combining Provider, Component, and Requirements.
    *
    * This trait provides fully configured controller instances as lazy values, ensuring they are created only once and
    * on-demand.
    */
  trait Interface extends Provider with Component:
    self: Requirements =>

    override lazy val homePageController: HomePageController = createHomePageController()

    override lazy val gameInitializationController: GameInitializationController = createGameInitializationController()

    override lazy val gameBoardController: GameBoardController = createGameBoardController()

    override lazy val cluesManagementController: CluesManagementController = createCluesManagementController()
