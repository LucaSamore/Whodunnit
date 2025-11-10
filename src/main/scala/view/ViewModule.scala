package view

import controller.{
  CaseGenerationController,
  CluesManagementController,
  ControllerModule,
  GameBoardController,
  HomePageController
}
import scalafx.application.Platform
import scalafx.scene.Scene

object ViewModule:

  trait View:
    def showPage(page: ScenePage): Unit
    def showScene(scene: Scene): Unit

  trait Provider:
    def view: View

  type Requirements = controller.ControllerModule.Provider

  trait Component:
    context: Requirements =>

    private trait SceneComposer:
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
        override protected def controller: CaseGenerationController =
          context.caseGenerationController
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

  trait Interface extends Provider with Component:
    self: Requirements =>
    override lazy val view: View = createView()
