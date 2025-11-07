package view

import controller.ControllerModule.Controller
import model.casegeneration.Case
import model.knowledgegraph.CaseKnowledgeGraph
import scalafx.Includes.eventClosureWrapperWithZeroParam
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ContentDisplay, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, TextAlignment}
import scalafx.stage.{Modality, Stage}

//class GameBoardScene(knowledgeGraph: CaseKnowledgeGraph) extends Scene(1280, 720):
abstract class GameBoardScene[S] extends Scene:
  protected def controller: Controller[S]
  protected def navigateTo(page: ScenePage): Unit

  import Config.*

  val mockKnowledgeGraph = CaseKnowledgeGraph()

  private val graphView = KnowledgeGraphView(
    mockKnowledgeGraph,
    viewDimensions = (sceneWidth, sceneHeight)
  )

  private def showPlotPopup(): Unit =
    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = "Case Plot"
      resizable = false

    val (plotTitle, plotContent) =
      controller.getGameState.investigativeCase match
        case Some(currentCase) =>
          (currentCase.plot.title, currentCase.plot.content)
        case None =>
          ("No Case Available", "No plot information available.")

    val titleLabel = new Label(plotTitle):
      font = iconsFont
      textFill = Color.web("#1E1E1E")
      wrapText = true
      textAlignment = TextAlignment.Center
      maxWidth = 560
      alignment = Pos.Center

    val contentArea = new Label(plotContent):
      text = plotContent
      textFill = Color.web("#1E1E1E")
      wrapText = true
      textAlignment = TextAlignment.Center
      maxWidth = 560
      alignment = Pos.Center
      alignment = Pos.Center

    val closeButton = new Button("Close"):
      font = iconsFont
      prefWidth = 100
      onAction = _ => popup.close()

    val popupContent = new VBox(15):
      padding = Insets(20)
      alignment = Pos.TopCenter
      children = Seq(
        titleLabel,
        contentArea,
        new HBox():
          alignment = Pos.Center
          children = Seq(closeButton)
      )

    val popupLayout = new BorderPane():
      center = popupContent
      style =
        """
          -fx-background-color: rgba(240, 235, 220, 0.95);
        """

    popup.scene = new Scene(600, 350):
      root = popupLayout

    popup.showAndWait()

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
      navigateTo(ScenePage.Accuse)
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
        children = Seq(plotButton, cluesButton, snapshotButton, accuseButton)

    center = new StackPane:
      children = Seq(
        new StackPane:
          prefWidth = sceneWidth
          prefHeight = sceneHeight
          children = Seq(graphView)
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
