package view

import model.game.CaseKnowledgeGraph
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, ContentDisplay, Label, ScrollPane}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, TextAlignment}
import scalafx.stage.{Modality, Stage}
import scalafx.scene.shape.Circle
import controller.GameBoardController
import model.game.{Character, ValidationResult}
import scalafx.util.StringConverter
import scalafx.scene.Node;

abstract class GameBoardScene extends Scene(1280, 720):

  protected def controller: GameBoardController
  protected def navigateTo(page: ScenePage): Unit

  import Config.*

  private val graphView = KnowledgeGraphView(
    controller.currentGameState.currentGraph.getOrElse(
      new CaseKnowledgeGraph()
    ),
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
    padding = Insets(7, 15, 7, 15)
    minWidth = 125
    prefWidth = 125
    maxWidth = 125
    border = new Border(
      new BorderStroke(
        Color.White,
        BorderStrokeStyle.Solid,
        new CornerRadii(5),
        new BorderWidths(2)
      )
    )
    background = new Background(
      Array(new BackgroundFill(
        Color.Transparent,
        new CornerRadii(5),
        Insets.Empty
      ))
    )

  controller.currentGameState.timer.foreach { timer =>
    timer.onTimeUpdate = timeString =>
      Platform.runLater {
        timerLabel.text = timeString
        updateAccuseButton() // Update accuse button on time change
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

  private def showInfoPopup(popupConfig: PopupConfig): Unit =
    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = popupConfig.title
      resizable = false

    val contentNodes = popupConfig.content.map {
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
          maxWidth = popupConfig.width - 40
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

    val popupLayout = createPopupLayout(scrollPane)

    popup.scene = new Scene(popupConfig.width, popupConfig.height):
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

  private def showAccusationPopup(): Unit =
    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = "Make Your Accusation"
      resizable = false

    val suspects = controller.getAvailableSuspects.toSeq.sortBy(_.name)

    val characterComboBox = new ComboBox[Character](suspects):
      promptText = "Select a suspect..."
      prefWidth = 300
      converter = StringConverter.toStringConverter[Character](_.name)

    val submitButton = new Button("Submit"):
      font = iconsFont
      prefWidth = 150
      disable = true
      onAction = _ =>
        characterComboBox.value.value match
          case character: Character =>
            popup.close()
            val result = controller.submitAccusation(character)
            result match
              case ValidationResult.CorrectSolution(culprit, motive) =>
                showGameEndPopup(hasWon = true)
              case ValidationResult.IncorrectSolution(accused, actualCulprit, motive) =>
                showGameEndPopup(hasWon = false)
              case _ =>
                println("Unexpected validation result")
          case null =>
            println("No character selected")

    characterComboBox.onAction = _ =>
      submitButton.disable = characterComboBox.value.value == null

    val popupContent = new VBox(20):
      padding = Insets(30)
      alignment = Pos.Center
      children = Seq(
        new Label("Who do you think is the culprit?"):
          font = Font.font(
            iconsFont.getFamily,
            FontWeight.Bold,
            16
          )
          textFill = Color.web("#1E1E1E")
        ,
        characterComboBox,
        submitButton
      )

    val popupLayout = createPopupLayout(popupContent)

    popup.scene = new Scene(400, 250):
      root = popupLayout

    popup.showAndWait()

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

  private def handleUndo(): Unit =
    // More protection for race conditions, the undo button should be disabled if undo is not possible
    if !controller.canUndo then
      println("Undo not available - already at the oldest state")
      return

    controller.undo() match
      case Some(previousGraph) =>
        graphView.updateGraph(previousGraph)
        println(controller.state.history.toString)
        updateUndoRedoButtons()
        updateAccuseButton()
        println(s"Undo executed - restored previous graph state")
      case None =>
        println("Undo failed unexpectedly")

  private def handleRedo(): Unit =
    // More protection for race conditions, the redo button should be disabled if undo is not possible
    if !controller.canRedo then
      println("Redo not available - no states to redo")
      return

    controller.redo() match
      case Some(nextGraph) =>
        graphView.updateGraph(nextGraph)
        updateUndoRedoButtons()
        updateAccuseButton()
        println(s"Redo executed - restored next graph state")
      case None =>
        println("Redo failed unexpectedly")

  private def updateUndoRedoButtons(): Unit =
    undoButton.disable = !controller.canUndo
    redoButton.disable = !controller.canRedo
    undoButton.opacity = if controller.canUndo then 1.0 else 0.5
    redoButton.opacity = if controller.canRedo then 1.0 else 0.5

  private val notificationsPanel = NotificationsPanel(iconsFont, controller.currentGameState.hints)

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

  private val snapshotIconView = new ImageView:
    image =
      if controller.hasSnapshot then postcardIconImage else cameraIconImage
    fitWidth = 80
    fitHeight = 60
    preserveRatio = true

  private val snapshotButton = new Button:
    text = "Snapshots"
    font = Font.font(
      iconsFont.getFamily,
      FontWeight.Normal,
      iconsFont.getSize
    )
    textFill = Color.White
    graphic = snapshotIconView
    contentDisplay = ContentDisplay.Top
    alignment = Pos.Center
    background = Background.fill(Color.Transparent)
    onAction = () => {
      if controller.hasSnapshot then
        // Restore snapshot and clear it
        println("Restoring snapshot...")
        controller.restoreSnapshot() match
          case Some(restoredGraph) =>
            graphView.updateGraph(restoredGraph)
            snapshotIconView.image = cameraIconImage
            updateUndoRedoButtons()
            updateAccuseButton()
            println("Snapshot restored successfully")
          case None =>
            println("Failed to restore snapshot")
      else
        // Save snapshot
        println("Saving snapshot...")
        controller.saveSnapshot()
        snapshotIconView.image = postcardIconImage
        println("Snapshot saved successfully")
    }

  private val accuseButton = createIconButton(
    "Accuse",
    handcuffsIconImage,
    60,
    60,
    () => {
      if controller.canAccuse then
        println("Accuse button clicked")
        showAccusationPopup()
      else
        println("Cannot accuse yet - prerequisites not met")
    }
  )

  // Update accuse button state based on canAccuse
  private def updateAccuseButton(): Unit =
    val canAccuse = controller.canAccuse
    accuseButton.disable = !canAccuse
    accuseButton.opacity = if canAccuse then 1.0 else 0.5
  private val undoButton = createIconButton(
    "Undo",
    undoIconImage,
    80,
    80,
    () => {
      println("Undo button clicked")
      handleUndo()
    }
  )
  private val redoButton = createIconButton(
    "Redo",
    redoIconImage,
    80,
    80,
    () => {
      println("Redo button clicked")
      handleRedo()
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
        children = Seq(undoButton, timerLabel, redoButton)

  // Initialize undo/redo button states
  updateUndoRedoButtons()

  // Initialize accuse button state
  updateAccuseButton()

  private object Config:
    val sceneWidth = 1280
    val sceneHeight = 720
    val popupBackgroundColor: Color = Color.rgb(240, 235, 220, 0.95)
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
    val postcardIconImage: Image = new Image(getClass.getResourceAsStream(
      gameboardImagesPath + "icons/postcard-icon.png"
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
    def createPopupLayout(content: Node): BorderPane =
      new BorderPane():
        center = content
        background = Background(Array(BackgroundFill(
          popupBackgroundColor,
          CornerRadii.Empty,
          Insets.Empty
        )))
