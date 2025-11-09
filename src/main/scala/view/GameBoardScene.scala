package view

import model.game.CaseKnowledgeGraph
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ContentDisplay, Label, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, TextAlignment}
import scalafx.stage.{Modality, Stage}
import scalafx.scene.shape.Circle
import controller.GameBoardController

abstract class GameBoardScene extends Scene(1280, 720):

  protected def controller: GameBoardController
  protected def navigateTo(page: ScenePage): Unit

  import Config.*

  private val graphView = KnowledgeGraphView(
    controller.currentGameState.graph.getOrElse(new CaseKnowledgeGraph()),
    viewDimensions = (sceneWidth, sceneHeight)
  )

  private val timerLabel = new Label("--:--"):
    font = Font.font(
      iconsFont.getFamily,
      FontWeight.Bold,
      28
    )
    textFill = Color.web("#FFFFFF")
    alignment = Pos.Center

  controller.currentGameState.timer.foreach { timer =>
    timer.onTimeUpdate = timeString =>
      Platform.runLater {
        timerLabel.text = timeString
      }

    timer.onTimeExpired = () =>
      Platform.runLater {
        showGameEndPopup(hasWon = false)
      }
  }

  private case class PopupConfig(
      title: String,
      content: Seq[PopupContent],
      width: Double = 600,
      height: Double = 350,
      backgroundColor: String = "rgba(240, 235, 220, 0.95)"
  )

  private sealed trait PopupContent
  private case class TextContent(
      text: String,
      fontSize: Double = 14,
      color: Color = Color.web("#1E1E1E"),
      isBold: Boolean = false
  ) extends PopupContent

  private def showInfoPopup(config: PopupConfig): Unit =
    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = config.title
      resizable = false

    val contentNodes = config.content.map {
      case TextContent(text, fontSize, color, isBold) =>
        new Label(text):
          font = Font.font(
            iconsFont.getFamily,
            if isBold then FontWeight.Bold else FontWeight.Normal,
            fontSize
          )
          textFill = color
          wrapText = true
          textAlignment = TextAlignment.Center
          maxWidth = config.width - 40
          alignment = Pos.Center
    }

    val closeButton = new Button("Close"):
      font = iconsFont
      prefWidth = 100
      onAction = _ => popup.close()

    val popupContent = new VBox(15):
      padding = Insets(20)
      alignment = Pos.Center
      children = contentNodes :+ closeButton

    val scrollPane = new ScrollPane:
      content = popupContent
      fitToWidth = true

    val popupLayout = new BorderPane():
      center = scrollPane
      background = Background(Array(BackgroundFill(
        Color.web(config.backgroundColor),
        CornerRadii.Empty,
        Insets.Empty
      )))

    popup.scene = new Scene(config.width, config.height):
      root = popupLayout

    // To ensure the scroll is at the top when shown (after rendering)
    Platform.runLater {
      scrollPane.vvalue = 0.0
    }

    popup.showAndWait()

  private def showPlotPopup(): Unit =
    val (plotTitle, plotContent) =
      controller.currentGameState.investigativeCase match
        case Some(currentCase) =>
          (currentCase.plot.title, currentCase.plot.content)
        case None =>
          ("No Case Available", "No plot information available.")

    showInfoPopup(
      PopupConfig(
        title = "Plot",
        content = Seq(
          TextContent(plotTitle, fontSize = 18, isBold = true),
          TextContent(plotContent)
        )
      )
    )

  private def showGameEndPopup(hasWon: Boolean): Unit =
    controller.currentGameState.investigativeCase match
      case Some(currentCase) =>
        val mainMessage = if hasWon then "YOU WON!" else "YOU LOSE!"
        val messageColor = if hasWon then Color.Green else Color.Red

        showInfoPopup(
          PopupConfig(
            title = "Solution",
            content = Seq(
              TextContent(
                mainMessage,
                fontSize = 48,
                color = messageColor,
                isBold = true
              ),
              TextContent(
                s"Culprit: ${currentCase.solution.culprit.name}",
                fontSize = 18,
                isBold = true
              ),
              TextContent(s"Motive: ${currentCase.solution.motive}")
            )
          )
        )

      case None =>
        println(
          "Warning: No investigative case available to show the solution."
        )

  private val notificationsPanel = NotificationsPanel(iconsFont)

  private val notificationBadge = new StackPane:
    prefWidth = 22
    prefHeight = 22
    translateX = 25
    translateY = -30
    mouseTransparent = true
    visible = true
    private val circle = new Circle:
      radius = 8
      fill = Color.Red
      stroke = Color.White
      strokeWidth = 2

    children = Seq(circle)

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
      notificationsPanel.toggleVisibility()
      notificationBadge.visible = false
    }
  )

  private val notificationsButtonContainer = new StackPane:
    alignment = Pos.TopCenter
    children = Seq(notificationsButton, notificationBadge)

  private val plotButton = createIconButton(
    "Plot",
    parchmentImage,
    60,
    60,
    () => {
      println("Plot button clicked")
      showPlotPopup()
    }
  )
  private val cluesButton = createIconButton(
    "Clues",
    documentsIconImage,
    60,
    60,
    () => {
      println("Clues button clicked")
      navigateTo(ScenePage.CluesManagement)
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
      showGameEndPopup(hasWon = true)
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
        children = Seq(notificationsButtonContainer)
      center = new HBox:
        minWidth = boxWidth
        minHeight = topBarHeight
        alignment = Pos.Center
        padding = Insets(topPadding, 0, 0, 0)
        children = Seq(timerLabel)
      right = new HBox:
        minWidth = boxWidth
        minHeight = topBarHeight
        alignment = Pos.CenterRight
        spacing = 50
        padding = Insets(topPadding, 70, 0, 10)
        children = Seq(plotButton, cluesButton, snapshotButton, accuseButton)

    center = new StackPane:
      children = Seq(
        new StackPane:
          prefWidth = sceneWidth
          prefHeight = sceneHeight
          children = Seq(graphView)
        ,
        new StackPane:
          alignment = Pos.TopLeft
          padding = Insets(topBarHeight - 190, 0, 0, 70)
          pickOnBounds = false
          children = Seq(notificationsPanel)
      )

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
    val parchmentImage: Image = new Image(
      getClass.getResourceAsStream(
        gameboardImagesPath + "icons/parchment-icon.png"
      )
    )
    val iconsFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/GloriaHallelujah-Regular.ttf"),
      18
    )
