package view

import controller.ControllerModule.Controller
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.control.*
import scalafx.geometry.*
import scalafx.scene.text.Font
import scalafx.stage.{Modality, Stage}

abstract class CluesManagementScene[S] extends Scene(1280, 720):

  import Config._

  protected def controller: Controller[S]
  protected def navigateTo(page: ScenePage): Unit

  private val notesTextArea = new TextArea():
    promptText = "Notes"
    prefRowCount = 6

  private val metadataForm = new VBox(10):
    children = Seq(
      baseLabel("Type: " + "Email"),
      baseLabel("Notes"),
      notesTextArea,
      new HBox():
        alignment = Pos.CenterRight
        children = Seq(
          new Button("Save"):
            font = baseFont
            onAction =
              _ => println(s"Saving notes: ${notesTextArea.text.value}")
        )
    )

  private def showRelationshipPopup(toModifyRelationship: Option[Relationship] =
    None): Unit =

    val fromComboBox = new ComboBox[String](mockEntities):
      editable = true
      promptText = "Select or type new entity"
      value = toModifyRelationship match
        case Some(rel) => rel.from
        case None      => ""

    val toComboBox = new ComboBox[String](mockEntities):
      editable = true
      promptText = "Select or type new entity"
      value = toModifyRelationship match
        case Some(rel) => rel.to
        case None      => ""

    val relationshipField = new TextField():
      promptText = "Relationship"
      text = toModifyRelationship match
        case Some(rel) => rel.relationshipType
        case None      => ""

    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = toModifyRelationship match
        case Some(_) => "Modify Relationship"
        case None    => "Add Relationship"
      resizable = false

    val saveButton = new Button("Save"):
      font = baseFont
      onAction = _ => {
        val action = toModifyRelationship match
          case Some(_) => "Modifying"
          case None    => "Saving"

        val fromValue = Option(fromComboBox.value.value).filter(_.nonEmpty)
        val toValue = Option(toComboBox.value.value).filter(_.nonEmpty)

        val newEntities = List(fromValue, toValue).flatten
          .filterNot(entity => mockEntities.contains(entity))
          .distinct

        newEntities.foreach { entity =>
          println(s"Created new entity: $entity")
        }

        (fromValue, toValue) match
          case (Some(from), Some(to)) =>
            println(s"$action: $from -> $to (${relationshipField.text.value})")
          case _ =>
            println("Warning: From or To field is empty")

        popup.close()
      }

    val discardButton = new Button("Discard"):
      font = baseFont
      onAction = _ => popup.close()

    val popupForm = new VBox(10):
      padding = Insets(20)
      children = Seq(
        baseLabel("From:"),
        fromComboBox,
        baseLabel("To:"),
        toComboBox,
        baseLabel("Relationship:"),
        relationshipField,
        new HBox(10):
          alignment = Pos.Center
          children = Seq(saveButton, discardButton)
      )

    val popupLayout = new BorderPane():
      center = popupForm

    popup.scene = new Scene(400, 300):
      root = popupLayout

    popup.showAndWait()

  private val addButton = new Button("+"):
    font = baseFont
    onAction = _ => showRelationshipPopup()

  private def createRelationshipItem(rel: Relationship): HBox =
    val modifyButton = new Button("Modify"):
      font = baseFont
      onAction = _ => showRelationshipPopup(Some(rel))

    val deleteButton = new Button("Delete"):
      font = baseFont
      onAction =
        _ => println(s"Deleting relationship: ${rel.from} -> ${rel.to}")

    new HBox(10):
      padding = Insets(10)
      alignment = Pos.CenterLeft
      hgrow = Priority.Always
      children = Seq(
        new VBox(5):
          hgrow = Priority.Always
          children = Seq(
            baseLabel(s"From: ${rel.from}"),
            baseLabel(s"To: ${rel.to}"),
            baseLabel(s"Type: ${rel.relationshipType}")
          )
        ,
        new VBox(5):
          alignment = Pos.CenterRight
          children = Seq(modifyButton, deleteButton)
      )
      style =
        """
           -fx-border-color: #1E1E1E;
           -fx-border-width: 1;
           -fx-border-radius: 5;
           -fx-background-radius: 5;
        """

  private val relationshipForm = new VBox(8):
    children = Seq(
      new BorderPane:
        left = subtitleLabel("Relationships")
        right =
          addButton
      ,
      new VBox(10):
        children = mockRelationships.map(createRelationshipItem)
    )

  private val documentPanelOverlay = new VBox(15):
    maxWidth = sceneWidth / 3
    prefWidth = sceneWidth / 3
    prefHeight = sceneHeight - 40
    alignment = Pos.Center
    background = overlayBackground
    children = Seq( /* Specific GUI for each document type */ )

  private val documentPanelContainer = new StackPane:
    maxWidth = sceneWidth / 2
    prefWidth = sceneWidth / 2
    prefHeight = sceneHeight
    padding = Insets(20, 0, 20, 0)
    alignment = Pos.Center
    children = documentPanelOverlay

  private val backButton = new Button("Back to Main Page"):
    font = baseFont
    onAction = _ => {
      println("Returning to main page")
      // WhodunnitApp.changeScene(new GameScene)
    }
    maxWidth = Double.MaxValue

  private val backButtonSection = new VBox(10):
    padding = Insets(0, 0, 15, 0)
    children = Seq(
      backButton,
      new Separator()
    )

  private def createDocumentButton(docName: String): Button =
    new Button(docName):
      font = baseFont
      maxWidth = Double.MaxValue
      disable = docName == currentDocument
      onAction = _ => println(s"Navigating to document: $docName")

  private val documentButtonsBox = new VBox(8):
    children = mockDocuments.map(createDocumentButton)

  private val leftPanelContent = new VBox(15):
    prefWidth = sceneWidth / 4 - 20
    padding = Insets(20)
    alignment = Pos.TopLeft
    children = Seq(
      backButtonSection,
      subtitleLabel("Clues Navigation"),
      documentButtonsBox
    )

  private val rightPanelContent = new VBox(15):
    prefWidth = sceneWidth / 4 - 20
    padding = Insets(20)
    alignment = Pos.TopLeft
    children = Seq(titleLabel("Manage Clues"), metadataForm, relationshipForm)

  root = new StackPane:
    background = new Background(Array(BackgroundImage(
      Config.backgroundImage,
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Center,
      new BackgroundSize(
        BackgroundSize.Auto,
        BackgroundSize.Auto,
        true,
        true,
        false,
        true
      )
    )))
    children = Seq(new HBox:
      prefWidth = sceneWidth
      prefHeight = sceneHeight
      children =
        Seq(
          panelOverlay(leftPanelContent),
          documentPanelContainer,
          panelOverlay(rightPanelContent)
        ))

  private object Config:
    val sceneWidth: Int = 1280
    val sceneHeight: Int = 720
    val backgroundImage: Image = new Image(getClass.getResourceAsStream(
      "/images/clues-management/clues-management_background.png"
    ))
    val baseFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/LibreBaskerville-Regular.ttf"),
      12
    )
    private val subtitleFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/LibreBaskerville-Regular.ttf"),
      18
    )
    private val titleFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/LibreBaskerville-Regular.ttf"),
      35
    )
    val overlayBackground: Background = new Background(Array(
      new BackgroundFill(
        Color.rgb(255, 255, 255, 0.7),
        new CornerRadii(0),
        Insets(0)
      )
    ))
    case class Relationship(from: String, to: String, relationshipType: String)
    val mockRelationships: List[Relationship] = List(
      Relationship("Lucia", "This", "is the sender"),
      Relationship("Luca", "This", "is the receiver"),
      Relationship("That", "Other", "is connected with"),
      Relationship("Lucia", "This", "is the sender"),
      Relationship("Luca", "This", "is the receiver"),
      Relationship("That", "Other", "is connected with"),
      Relationship("Lucia", "This", "is the sender"),
      Relationship("Luca", "This", "is the receiver"),
      Relationship("That", "Other", "is connected with")
    )
    val mockEntities: List[String] =
      List("Lucia", "Luca", "This", "That", "Other", "Entity1", "Entity2")
    val mockDocuments: List[String] =
      List(
        "This",
        "Email 2",
        "Email 3",
        "SMS 1",
        "SMS 2",
        "Lettera 1",
        "Telefonata 1"
      )
    val currentDocument = "This"

    def baseLabel(text: String): Label =
      new Label(text):
        font = baseFont
        textFill = Color.web("#1E1E1E")
    def subtitleLabel(text: String): Label =
      new Label(text):
        font = subtitleFont
        textFill = Color.web("#1E1E1E")
    def titleLabel(text: String): Label =
      new Label(text):
        font = titleFont
        textFill = Color.web("#1E1E1E")
    def panelOverlay(panelContent: VBox): ScrollPane =
      new ScrollPane:
        prefWidth = sceneWidth / 4
        prefHeight = sceneHeight
        maxWidth = sceneWidth / 4
        background = overlayBackground
        content = panelContent
        fitToWidth = true
        hbarPolicy = ScrollPane.ScrollBarPolicy.Never
        vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
        style =
          """
            -fx-background: rgba(255, 255, 255, 0.7);
            -fx-background-color: rgba(255, 255, 255, 0.7);
          """.stripMargin
