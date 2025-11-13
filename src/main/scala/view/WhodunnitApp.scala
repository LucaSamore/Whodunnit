package view

import scalafx.application.JFXApp3
import scalafx.scene.Scene

/** Main JavaFX application for the Whodunnit game.
  *
  * This object extends JFXApp3 and serves as the entry point for the graphical user interface. It initializes the
  * primary stage and manages scene changes throughout the application lifecycle.
  */
object WhodunnitApp extends JFXApp3:

  private val launcher = new WhodunnitLauncher()

  /** Starts the JavaFX application.
    *
    * Initializes the primary stage with window properties and displays the homepage as the initial scene.
    */
  override def start(): Unit =
    stage = new JFXApp3.PrimaryStage:
      title = "Whodunnit"
      resizable = true
      // 16:9 aspect ratio
      minWidth = 1280
      minHeight = 720

    launcher.view.showPage(ScenePage.Homepage)

  /** Changes the current scene displayed in the primary stage.
    *
    * This method should be called on the JavaFX Application Thread.
    *
    * @param newScene
    *   the new scene to display
    */
  def changeScene(newScene: Scene): Unit =
    stage.scene = newScene
