package view

import controller.{CaseGenerationController, CluesManagementController, ControllerModule, GameBoardController, HomePageController}
import scalafx.application.Platform
import scalafx.scene.Scene

object ViewModule:

  trait View[S]:
    def showPage(page: ScenePage): Unit
    def showScene(scene: Scene): Unit

  trait Provider[S]:
    def view: View[S]

  type Requirements[S] = controller.ControllerModule.Provider[S]

  trait Component[S]:
    context: Requirements[S] =>

    private trait SceneComposer:
      def create(page: ScenePage): Scene

    private class SceneComposerImpl(onNavigate: ScenePage => Unit)
        extends SceneComposer:

      private class HomepageSceneImpl extends HomepageScene[S]:
        override protected def controller: HomePageController[S] =
          context.homePageController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class GameConfigurationSceneImpl
          extends GameConfigurationScene[S]:
        override protected def controller: CaseGenerationController[S] =
          context.caseGenerationController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class GameBoardSceneImpl extends GameBoardScene[S]:
        override protected def controller: GameBoardController[S] =
          context.gameBoardController
        override protected def navigateTo(page: ScenePage): Unit =
          onNavigate(page)

      private class CluesManagementSceneImpl extends CluesManagementScene[S]:
        override protected def controller: CluesManagementController[S] =
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

    protected def createView(): View[S] = new ViewImpl

    private class ViewImpl extends View[S]:
      private lazy val sceneComposer: SceneComposer =
        new SceneComposerImpl(showPage)

      override def showPage(page: ScenePage): Unit =
        showScene(sceneComposer.create(page))

      override def showScene(scene: Scene): Unit =
        Platform.runLater {
          WhodunnitApp.changeScene(scene)
        }

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    override lazy val view: View[S] = createView()
