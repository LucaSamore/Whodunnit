package view

import controller.ControllerModule
import scalafx.scene.Scene

/** Factory for creating scenes with dependency injection.
  *
  * This factory uses the Cake Pattern to automatically inject dependencies into
  * scenes, ensuring that each scene has access to the required controllers and
  * navigation capabilities.
  */
trait SceneFactory[S]:
  /** Creates a scene based on the requested page type */
  def createScene(page: ScenePage): Scene

  /** Navigates to a specific page by creating and displaying the corresponding
    * scene
    */
  def navigateTo(page: ScenePage): Unit =
    WhodunnitApp.changeScene(createScene(page))

object SceneFactory:

  /** Factory implementation that requires dependencies through the Cake
    * Pattern.
    *
    * This component provides concrete scene implementations with proper
    * dependency injection, ensuring each scene can access the controller and
    * navigation functionality.
    */
  trait Component[S]:
    // Self-type annotation requiring the necessary dependencies
    context: ControllerModule.Provider[S] =>

    class SceneFactoryImpl extends SceneFactory[S]:

      // Scene implementations with access to the injected context
      private class HomepageSceneImpl extends HomepageScene[S]:
        override protected def controller: ControllerModule.Controller[S] =
          context.controller
        override protected def navigateTo(page: ScenePage): Unit =
          SceneFactoryImpl.this.navigateTo(page)

      private class GameConfigurationSceneImpl
          extends GameConfigurationScene[S]:
        override protected def controller: ControllerModule.Controller[S] =
          context.controller
        override protected def navigateTo(page: ScenePage): Unit =
          SceneFactoryImpl.this.navigateTo(page)

      private class GameBoardSceneImpl extends GameBoardScene[S]:
        override protected def controller: ControllerModule.Controller[S] =
          context.controller
        override protected def navigateTo(page: ScenePage): Unit =
          SceneFactoryImpl.this.navigateTo(page)

      override def createScene(page: ScenePage): Scene =
        page match
          case ScenePage.Homepage          => new HomepageSceneImpl()
          case ScenePage.GameConfiguration => new GameConfigurationSceneImpl()
          case ScenePage.GameBoard         => new GameBoardSceneImpl()
          case ScenePage.CluesManagement   =>
            // TODO: implement CluesManagementScene
            throw new NotImplementedError(
              "CluesManagementScene not yet implemented"
            )
          case ScenePage.Accuse =>
            // TODO: implement AccuseScene
            throw new NotImplementedError("AccuseScene not yet implemented")

  trait Provider[S]:
    val sceneFactory: SceneFactory[S]

  trait Interface[S] extends Provider[S] with Component[S]:
    self: ControllerModule.Provider[S] =>
    override val sceneFactory: SceneFactory[S] = new SceneFactoryImpl()
