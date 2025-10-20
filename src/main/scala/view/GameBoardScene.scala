package view

import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ContentDisplay}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{
  Background,
  BackgroundImage,
  BackgroundPosition,
  BackgroundRepeat,
  BackgroundSize,
  BorderPane,
  HBox,
  StackPane
}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

class GameBoardScene extends Scene(1280, 720):

  import Config._

  private def createIconButton(
      description: String,
      iconImage: Image,
      buttonWidth: Int,
      buttonHeight: Int,
      action: () => Unit
  ): Button =
    new Button:
      text = description
      font = Font.font(
        iconsFont.getFamily,
        FontWeight.Normal,
        iconsFont.getSize
      )
      textFill = Color.web("#FFFFFF")
      graphic = new ImageView:
        image = iconImage
        fitWidth = buttonWidth
        fitHeight = buttonHeight
        preserveRatio = true
      contentDisplay = ContentDisplay.Top
      alignment = Pos.Center
      style =
        """
          -fx-background-color: transparent;
        """
      onAction = action

  private val notificationsButton = createIconButton(
    "Notifications",
    bellIconImage,
    60,
    60,
    () => {
      println("Notifications button clicked")
      // Handle notifications action here
    }
  )
  private val cluesButton = createIconButton(
    "Clues",
    documentsIconImage,
    60,
    60,
    () => {
      println("Clues button clicked")
      // WhodunnitApp.changeScene(new CluesManagementScene)
    }
  )
  private val snapshotButton = createIconButton(
    "Snapshots",
    cameraIconImage,
    80,
    60,
    () => {
      println("Snapshots button clicked")
      // Handle snapshots action here
    }
  )
  private val accuseButton = createIconButton(
    "Accuse",
    handcuffsIconImage,
    60,
    60,
    () => {
      println("Accuse button clicked")
      // WhodunnitApp.changeScene(new AccuseScene())
    }
  )
  private val undoButton = createIconButton(
    "Undo",
    undoIconImage,
    80,
    80,
    () => {
      println("Undo button clicked")
      // Handle undo action here
    }
  )
  private val redoButton = createIconButton(
    "Redo",
    redoIconImage,
    80,
    80,
    () => {
      println("Redo button clicked")
      // Handle redo action here
    }
  )

  private val topBarHeight: Int = sceneHeight / 5
  private val bottomBarHeight: Int = topBarHeight

  root = new BorderPane:
    background = new Background(Array(new BackgroundImage(
      image = backgroundImage,
      repeatX = BackgroundRepeat.NoRepeat,
      repeatY = BackgroundRepeat.NoRepeat,
      position = BackgroundPosition.Default,
      size = new BackgroundSize(
        width = BackgroundSize.Auto,
        height = BackgroundSize.Auto,
        widthAsPercentage = false,
        heightAsPercentage = false,
        contain = true,
        cover = false
      )
    )))

    top = new BorderPane:
      minWidth = sceneWidth
      minHeight = topBarHeight
      private val boxWidth = sceneWidth / 3
      private val topPadding = 65

      left = new HBox:
        minWidth = boxWidth
        minHeight = topBarHeight
        alignment = Pos.CenterLeft
        padding = Insets(topPadding, 10, 0, 70)
        children = Seq(notificationsButton)
      center = new HBox:
        minWidth = boxWidth
        minHeight = topBarHeight
        alignment = Pos.Center
        padding = Insets(topPadding, 0, 0, 0)
        children = Seq( /* TODO: Timer */ )
      right = new HBox:
        minWidth = boxWidth
        minHeight = topBarHeight
        alignment = Pos.CenterRight
        spacing = 50
        padding = Insets(topPadding, 70, 0, 10)
        children = Seq(cluesButton, snapshotButton, accuseButton)

    center = new StackPane {
      // TODO: Game board content
    }

    bottom = new BorderPane:
      minWidth = sceneWidth
      minHeight = bottomBarHeight
      center = new HBox:
        minWidth = sceneWidth
        minHeight = bottomBarHeight
        alignment = Pos.Center
        spacing = 50
        padding = Insets(0, 0, 45, 0)
        children = Seq(undoButton, redoButton)

  private object Config:
    val sceneWidth = 1280
    val sceneHeight = 720
    private val gameboardImagesPath = "/images/gameboard/"
    val backgroundImage: Image = new Image(
      getClass.getResourceAsStream(gameboardImagesPath + "blackboard.png")
    )
    val bellIconImage: Image = new Image(
      getClass.getResourceAsStream(gameboardImagesPath + "icons/bell-icon.png")
    )
    val cameraIconImage: Image = new Image(getClass.getResourceAsStream(
      gameboardImagesPath + "icons/camera-icon.png"
    ))
    val documentsIconImage: Image = new Image(getClass.getResourceAsStream(
      gameboardImagesPath + "icons/documents-icon.png"
    ))
    val handcuffsIconImage: Image = new Image(getClass.getResourceAsStream(
      gameboardImagesPath + "icons/handcuffs-icon.png"
    ))
    val undoIconImage: Image = new Image(
      getClass.getResourceAsStream(gameboardImagesPath + "icons/undo-icon.png")
    )
    val redoIconImage: Image = new Image(
      getClass.getResourceAsStream(gameboardImagesPath + "icons/redo-icon.png")
    )
    val iconsFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/GloriaHallelujah-Regular.ttf"),
      18
    )
