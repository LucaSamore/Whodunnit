package view

import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.control.*
import scalafx.geometry.*
import scalafx.scene.text.Font
import scalafx.stage.{Modality, Stage}

class CluesManagementScene extends Scene(1280, 720):

  import Config._

  private val titleLabel = new Label("Manage Clues"):
    font = titleFont
    textFill = Color.web("#1E1E1E")

  private val metadataForm = new VBox(10):
    children = Seq(
      baseLabel("Type"),
      new TextField():
        promptText =
          "Type"
      ,
      baseLabel("Title"),
      new TextField():
        promptText =
          "Title"
      ,
      baseLabel("Notes"),
      new TextArea():
        promptText = "Notes"
        prefRowCount = 6
    )

  private def showRelationshipPopup(existingRelationship: Option[Relationship] =
    None): Unit =
    val fromField = new TextField():
      promptText = "From"
      text = existingRelationship.map(_.from).getOrElse("")
    val toField = new TextField():
      promptText = "To"
      text = existingRelationship.map(_.to).getOrElse("")
    val relationshipField = new TextField():
      promptText = "Relationship"
      text = existingRelationship.map(_.relationshipType).getOrElse("")

    val popup = new Stage():
      initModality(Modality.ApplicationModal)
      title = if (existingRelationship.isDefined) "Modify Relationship"
      else "Add Relationship"
      resizable = false

    val saveButton = new Button("Save"):
      font = baseFont
      onAction = _ => {
        val action =
          if (existingRelationship.isDefined) "Modifying" else "Saving"
        println(
          s"$action: ${fromField.text.value} -> ${toField.text.value} (${relationshipField.text.value})"
        )
        popup.close()
      }

    val discardButton = new Button("Discard"):
      font = baseFont
      onAction = _ => {
        popup.close()
      }

    val popupForm = new VBox(10):
      padding = Insets(20)
      children = Seq(
        baseLabel("From:"),
        fromField,
        baseLabel("To:"),
        toField,
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
    maxWidth = sceneWidth / 2
    prefWidth = sceneWidth / 2
    prefHeight = sceneHeight - 40
    alignment = Pos.Center
    background = overlayBackground
    children = Seq( /* Specific GUI for each document type */ )

  private val documentPanelContainer = new StackPane:
    maxWidth = sceneWidth * 3 / 4
    prefWidth = sceneWidth * 3 / 4
    prefHeight = sceneHeight
    padding = Insets(20, 0, 20, 0)
    alignment = Pos.Center
    children = documentPanelOverlay

  private val rightPanelContent = new VBox(15):
    prefWidth = sceneWidth / 4 - 20
    padding = Insets(20)
    alignment = Pos.TopLeft
    children = Seq(titleLabel, metadataForm, relationshipForm)

  private val rightPanelOverlay = new ScrollPane:
    prefWidth = sceneWidth / 4
    prefHeight = sceneHeight
    maxWidth = sceneWidth / 4
    background = overlayBackground
    content = rightPanelContent
    fitToWidth = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style =
      """
        -fx-background: rgba(255, 255, 255, 0.7);
        -fx-background-color: rgba(255, 255, 255, 0.7);
      """.stripMargin

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
      children = Seq(documentPanelContainer, rightPanelOverlay))

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
    val titleFont: Font = Font.loadFont(
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

    def baseLabel(text: String): Label =
      new Label(text):
        font = baseFont
        textFill = Color.web("#1E1E1E")

    def subtitleLabel(text: String): Label =
      new Label(text):
        font = subtitleFont
        textFill = Color.web("#1E1E1E")
