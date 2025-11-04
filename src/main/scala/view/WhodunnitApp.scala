package view

import scalafx.application.JFXApp3
import scalafx.scene.Scene

object WhodunnitApp extends JFXApp3:

  private val launcher = new WhodunnitLauncher()

  override def start(): Unit =
    stage = new JFXApp3.PrimaryStage:
      title = "Whodunnit"
      resizable = true
      // 16:9 aspect ratio
      minWidth = 1280
      minHeight = 720

    launcher.view.showPage(new HomepageScene())

  def changeScene(newScene: Scene): Unit =
    stage.scene = newScene

  // Expose launcher for access from scenes
  def getController = launcher.controller
