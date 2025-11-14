package view

import controller.CluesManagementController
import model.game.{CaseFile, Entity, Link}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.scene.image.Image
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.{Modality, Stage}

abstract class CluesManagementScene extends Scene(1280, 720):
  import CluesManagementSceneConfig.*

  protected def controller: CluesManagementController
  protected def navigateTo(page: ScenePage): Unit

  private val cluesVisualization = CluesVisualization(controller)
  private val relationshipsPanel = RelationshipsPanel(controller)

  root = new StackPane:
    background = createBackgroundImage(backgroundImage)
    children = new HBox:
      prefWidth = sceneWidth
      prefHeight = sceneHeight
      children = Seq(
        createPanel(createLeftPanel()),
        cluesVisualization.centerPanel,
        createPanel(createRightPanel())
      )

  private def createLeftPanel(): VBox = new VBox(15):
    prefWidth = sceneWidth / 4 - 20
    padding = Insets(20)
    alignment = Pos.TopLeft
    children = Seq(
      createBackButton(),
      new Separator(),
      cluesVisualization.navigationPanel
    )

  private def createRightPanel(): VBox = new VBox(15):
    prefWidth = sceneWidth / 4 - 20
    padding = Insets(20)
    alignment = Pos.TopLeft
    children = Seq(
      titleLabel("Manage Clues"),
      cluesVisualization.metadataPanel,
      new Region:
        vgrow = Priority.Always
        prefHeight =
          100
      ,
      relationshipsPanel.panel
    )

  private def createBackButton(): Button = new Button("Back to Main Page"):
    font = baseFont
    maxWidth = Double.MaxValue
    onAction = _ => navigateTo(ScenePage.GameBoard)

class CluesVisualization(controller: CluesManagementController):
  import CluesManagementSceneConfig.*

  private val selectedDocument = ObjectProperty[Option[CaseFile]]:
    controller.state.investigativeCase.flatMap(
      _.caseFiles.headOption
    )

  private val contentArea = new Label():
    wrapText = true
    font = baseFont
    style =
      "-fx-font-size: 18px; -fx-control-inner-background: transparent; -fx-background-color: transparent;"

  private val documentMetadataDisplay = createDocumentMetadataDisplay()

  private val documentsBox = new VBox(8)

  val centerPanel: StackPane = new StackPane:
    maxWidth = sceneWidth / 2
    prefWidth = sceneWidth / 2
    prefHeight = sceneHeight
    padding = Insets(20, 0, 20, 0)
    alignment = Pos.Center
    children = new VBox(15):
      maxWidth = sceneWidth / 3
      prefWidth = sceneWidth / 3
      prefHeight = sceneHeight - 40
      alignment = Pos.TopCenter
      padding = Insets(20)
      background = overlayBackground
      children = Seq(contentArea)

  val navigationPanel: VBox = new VBox(8):
    children = Seq(subtitleLabel("Clues Navigation"), documentsBox)

  val metadataPanel: VBox = new VBox(10):
    children = Seq(
      documentMetadataDisplay.typeLabel,
      documentMetadataDisplay.senderLabel,
      documentMetadataDisplay.receiverLabel,
      documentMetadataDisplay.dateLabel
    )

  init()

  private def init(): Unit =
    selectedDocument.onChange: (_, _, newDoc) =>
      newDoc.foreach(updateMetaDataCurrentDocument)
    refreshDocumentsList()
    selectedDocument.value.foreach(updateMetaDataCurrentDocument)

  private case class DocumentMetadataDisplay(
      typeLabel: Label,
      senderLabel: Label,
      receiverLabel: Label,
      dateLabel: Label
  )

  private def createDocumentMetadataDisplay(): DocumentMetadataDisplay =
    DocumentMetadataDisplay(
      typeLabel = baseLabel(""),
      senderLabel = baseLabel(""),
      receiverLabel = baseLabel(""),
      dateLabel = baseLabel("")
    )

  private def updateMetaDataCurrentDocument(doc: CaseFile): Unit =
    contentArea.text = doc.content
    documentMetadataDisplay.typeLabel.text = s"Type: ${doc.kind}"
    documentMetadataDisplay.senderLabel.text =
      s"Sender: ${doc.sender.map(_.name).getOrElse("Not Available")}"
    documentMetadataDisplay.receiverLabel.text =
      s"Receiver: ${doc.receiver.map(_.name).getOrElse("Not Available")}"
    documentMetadataDisplay.dateLabel.text =
      s"Date: ${doc.date.map(_.toString).getOrElse("Not Available")}"
    refreshDocumentsList()

  private def refreshDocumentsList(): Unit =
    val caseFiles = controller.state.investigativeCase
      .map(_.caseFiles)
      .getOrElse(Seq.empty)
    documentsBox.children = caseFiles.map(createDocumentButton)

  private def createDocumentButton(caseFile: CaseFile): Button =
    new Button(caseFile.title):
      font = baseFont
      maxWidth = Double.MaxValue
      onAction = _ => selectedDocument.value = Some(caseFile)

      selectedDocument.onChange: (_, _, newDocument) =>
        disable = newDocument.contains(caseFile)
      disable = selectedDocument.value.contains(caseFile)

class RelationshipsPanel(controller: CluesManagementController):
  import CluesManagementSceneConfig.*

  private val relationshipsBox = new VBox(10)

  val panel: VBox = new VBox(8):
    children = Seq(
      new BorderPane:
        left = subtitleLabel("Relationships")
        right =
          createAddButton()
      ,
      relationshipsBox
    )

  init()

  private def init(): Unit = refreshRelationshipPanel()

  private def refreshRelationshipPanel(): Unit =
    relationshipsBox.children = controller.getRelationships.map {
      case (from, link, to) => createRelationshipCard(from, link, to)
    }

  private def createAddButton(): Button = new Button("+"):
    font = baseFont
    onAction = _ => showRelationshipDialog(None)

  private def createRelationshipCard(
      from: Entity,
      link: Link,
      to: Entity
  ): HBox =
    new HBox(10):
      padding = Insets(10)
      alignment = Pos.CenterLeft
      hgrow = Priority.Always
      style =
        "-fx-border-color: #1E1E1E; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;"
      children = Seq(
        new VBox(5):
          hgrow = Priority.Always
          children = Seq(
            baseLabel(s"From: ${controller.getEntityDisplayName(from)}"),
            baseLabel(s"To: ${controller.getEntityDisplayName(to)}"),
            baseLabel(s"Type: ${link.semantic}")
          )
        ,
        new VBox(5):
          alignment = Pos.CenterRight
          children = Seq(
            createModifyButton(from, link, to),
            createDeleteButton(from, link, to)
          )
      )

  private def createModifyButton(from: Entity, link: Link, to: Entity): Button =
    new Button("Modify"):
      font = baseFont
      onAction = _ => showRelationshipDialog(Some((from, link, to)))

  private def createDeleteButton(from: Entity, link: Link, to: Entity): Button =
    new Button("Delete"):
      font = baseFont
      onAction = _ =>
        controller.removeAndSaveRelationship(from, link, to)
        refreshRelationshipPanel()

  private def showRelationshipDialog(relationshipToEdit: Option[(
      Entity,
      Link,
      Entity
  )])
      : Unit =
    val dialog =
      RelationshipDialog(
        controller,
        relationshipToEdit,
        controller.getEntityDisplayName
      )
    dialog.showAndWait()

    dialog.result.foreach: (from, link, to) =>
      relationshipToEdit match
        case Some(oldRelationship) =>
          controller.modifyRelationship(oldRelationship, (from, link, to))
        case None => controller.addAndSaveRelationship(from, link, to)
      refreshRelationshipPanel()

class RelationshipDialog(
    controller: CluesManagementController,
    relationshipToEdited: Option[(Entity, Link, Entity)],
    getEntityDisplayName: Entity => String
) extends Stage:
  import CluesManagementSceneConfig.*

  initModality(Modality.ApplicationModal)
  title = if relationshipToEdited.isDefined then "Modify Relationship"
  else "Add Relationship"
  resizable = false

  private val entityNames = ObservableBuffer(
    controller.getEntities.map(getEntityDisplayName)*
  )
  private val fromCombo = createEntityCombo(
    relationshipToEdited.map { case (from, _, _) =>
      getEntityDisplayName(from)
    }.getOrElse("")
  )
  private val toCombo = createEntityCombo(
    relationshipToEdited.map { case (_, _, to) =>
      getEntityDisplayName(to)
    }.getOrElse("")
  )
  private val typeField =
    createTypeField(relationshipToEdited.map(_._2.semantic).getOrElse(""))
  private val saveButton = new Button("Save"):
    font = baseFont
    onAction = _ => handleSave()
  private val discardButton = new Button("Discard"):
    font = baseFont
    onAction = _ => close()

  scene = new Scene(400, 300):
    root = new BorderPane:
      center = new VBox(10):
        padding = Insets(20)
        children = Seq(
          baseLabel("From:"),
          fromCombo,
          baseLabel("To:"),
          toCombo,
          baseLabel("Relationship:"),
          typeField,
          new HBox(10):
            alignment = Pos.Center
            children = Seq(saveButton, discardButton)
        )

  private def createEntityCombo(initialValue: String): ComboBox[String] =
    new ComboBox[String](entityNames):
      editable = true
      promptText = "Select or type new entity"
      value = initialValue
      this.value.onChange((_, _, _) => updateSaveButtonState())

  private def createTypeField(initialValue: String): TextField =
    new TextField:
      promptText = "Relationship"
      text = initialValue
      this.text.onChange((_, _, _) => updateSaveButtonState())

  private def updateSaveButtonState(): Unit =
    saveButton.disable = !isValid

  updateSaveButtonState()

  private def isValid: Boolean =
    Option(fromCombo.value.value).exists(_.trim.nonEmpty) &&
      Option(toCombo.value.value).exists(_.trim.nonEmpty) &&
      typeField.text.value.trim.nonEmpty

  var result: Option[(Entity, Link, Entity)] = None

  private def handleSave(): Unit =
    for
      fromName <- Option(fromCombo.value.value).map(_.trim).filter(_.nonEmpty)
      toName <- Option(toCombo.value.value).map(_.trim).filter(_.nonEmpty)
      fromEntity <- Option(controller.findOrCreateEntity(fromName))
      toEntity <- Option(controller.findOrCreateEntity(toName))
    do
      result = Some((fromEntity, Link(typeField.text.value), toEntity))
      close()

object CluesManagementSceneConfig:
  val sceneWidth: Int = 1280
  val sceneHeight: Int = 720
  val backgroundImage: Image = Image:
    getClass.getResourceAsStream(
      "/images/clues-management/clues-management_background.png"
    )
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
  def baseLabel(text: String): Label = new Label(text):
    font = baseFont
    textFill = Color.web("#1E1E1E")
  def subtitleLabel(text: String): Label = new Label(text):
    font = subtitleFont
    textFill = Color.web("#1E1E1E")
  def titleLabel(text: String): Label = new Label(text):
    font = titleFont
    textFill = Color.web("#1E1E1E")
  def createPanel(contentPanel: VBox): ScrollPane = new ScrollPane:
    prefWidth = sceneWidth / 4
    prefHeight = sceneHeight
    maxWidth = sceneWidth / 4
    background = overlayBackground
    this.content = contentPanel
    fitToWidth = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style =
      "-fx-background: rgba(255, 255, 255, 0.7); -fx-background-color: rgba(255, 255, 255, 0.7);"
  def createBackgroundImage(image: Image): Background = new Background(Array(
    BackgroundImage(
      image,
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
    )
  ))
