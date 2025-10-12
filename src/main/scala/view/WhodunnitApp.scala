package view

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import view.WhodunnitApp.stage

object WhodunnitApp extends JFXApp3 {

  override def start(): Unit = {
    stage = new PrimaryStage {
      title = "Whodunnit"
      scene = HomepageScene
      resizable = true
      // 16:9 aspect ratio
      minWidth = 800
      minHeight = 450
    }
    stage.show()
  }

  def changeScene(newScene: Scene): Unit = {
    stage.scene = newScene
  }
}