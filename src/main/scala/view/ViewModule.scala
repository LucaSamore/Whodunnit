package view

import scalafx.application.Platform
import scalafx.scene.Scene

object ViewModule:

  trait View[S]:
    def showPage(scene: Scene): Unit

  trait Provider[S]:
    val view: View[S]

  type Requirements[S] = controller.ControllerModule.Provider[S]

  trait Component[S]:
    context: Requirements[S] =>

    class ViewImpl extends View[S]:
      def showPage(scene: Scene): Unit =
        Platform.runLater {
          WhodunnitApp.changeScene(scene)
        }

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    def View(): View[S] = new ViewImpl()
