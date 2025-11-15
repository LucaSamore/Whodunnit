package view

import controller.{
  CluesManagementController,
  ControllerModule,
  GameBoardController,
  GameInitializationController,
  HomePageController
}
import scalafx.application.Platform
import scalafx.scene.Scene
import view.board.GameBoardScene
import view.clues.CluesManagementScene
import view.configuration.GameConfigurationScene
import view.home.HomepageScene

/** Module providing the View layer of the application architecture.
  *
  * This module follows the Cake Pattern for dependency injection and manages the creation and navigation between
  * different scenes in the application. It requires the ControllerModule to function.
  */
object ViewModule:

  /** Interface for the application's view layer.
    *
    * Manages scene navigation and display in the JavaFX application.
    */
  trait View:
    /** Displays the specified page by creating and showing its scene.
      *
      * @param page
      *   the page to display
      */
    def showPage(page: ScenePage): Unit

    /** Displays the specified JavaFX scene.
      *
      * This operation is executed on the JavaFX Application Thread.
      *
      * @param scene
      *   the scene to display
      */
    def showScene(scene: Scene): Unit

  /** Provides access to the View instance. */
  trait Provider:
    def view: View

  /** Dependencies required by this module.
    *
    * The ViewModule requires access to controllers for scene creation.
    */
  type Requirements = controller.ControllerModule.Provider

  /** Component providing the View implementation.
    *
    * This component creates scenes based on page types and manages navigation. It requires access to controllers
    * through the Requirements context.
    */
  trait Component:
    context: Requirements =>

    /** Internal interface for composing and creating scenes. */
    private trait SceneComposer:
      /** Creates a JavaFX Scene for the specified page.
        *
        * @param page
        *   the page type to create a scene for
        * @return
        *   the created Scene
        */
      def create(page: ScenePage): Scene

    private class SceneComposerImpl(onNavigate: ScenePage => Unit)
        extends SceneComposer:

      private class HomepageSceneImpl extends HomepageScene:
        override protected def controller: HomePageController =
          context.homePageController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class GameConfigurationSceneImpl
          extends GameConfigurationScene:
        override protected def controller: GameInitializationController =
          context.gameInitializationController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class GameBoardSceneImpl extends GameBoardScene:
        override protected def controller: GameBoardController =
          context.gameBoardController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class CluesManagementSceneImpl extends CluesManagementScene:
        override protected def controller: CluesManagementController =
          context.cluesManagementController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      override def create(page: ScenePage): Scene =
        page match
          case ScenePage.Homepage          => new HomepageSceneImpl()
          case ScenePage.GameConfiguration => new GameConfigurationSceneImpl()
          case ScenePage.GameBoard         => new GameBoardSceneImpl()
          case ScenePage.CluesManagement   => new CluesManagementSceneImpl()
          case ScenePage.Accuse            =>
            throw new NotImplementedError("Accuse scene is not implemented yet")

    protected def createView(): View = new ViewImpl

    private class ViewImpl extends View:
      private lazy val sceneComposer: SceneComposer =
        new SceneComposerImpl(showPage)

      override def showPage(page: ScenePage): Unit =
        showScene(sceneComposer.create(page))

      override def showScene(scene: Scene): Unit =
        Platform.runLater {
          WhodunnitApp.changeScene(scene)
        }

  /** Complete module interface combining Provider, Component, and Requirements.
    *
    * This trait provides a fully configured View instance ready to use.
    */
  trait Interface extends Provider with Component:
    self: Requirements =>
    override lazy val view: View = createView()
