package view

import controller.CaseGenerationController
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ComboBox, RadioButton, ToggleGroup}
import scalafx.scene.image.Image
import scalafx.scene.layout.*
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text, TextAlignment}

abstract class GameConfigurationScene extends Scene(1280, 720):

  protected def controller: CaseGenerationController
  protected def navigateTo(page: ScenePage): Unit

  private object Theme:
    val primaryColor: Color = Color.rgb(30, 30, 30, 0.75)
    val primaryColorLight: Color = Color.rgb(30, 30, 30, 0.25)
    val backgroundColor: Color = Color.rgb(245, 225, 202)
    val transparentColor: Color = Color.Transparent

    val cornerRadius: CornerRadii = CornerRadii(10)
    val borderWidth: Int = 4
    val smallBorderWidth: Int = 2

  private object Typography:
    private def loadFont(size: Int): Font =
      Font.loadFont(
        getClass.getResourceAsStream("/fonts/LibreBaskerville-Regular.ttf"),
        size
      )

    val titleFont: Font = loadFont(32)
    val sectionFont: Font = loadFont(24)
    val subsectionFont: Font = loadFont(16)

  private object Styles:
    def borderedBox(borderWidth: Int = Theme.borderWidth): String =
      s"""
        -fx-background-color: transparent;
        -fx-border-color: rgba(30, 30, 30, 0.75);
        -fx-border-width: ${borderWidth}px;
        -fx-border-radius: 10px;
        -fx-background-radius: 10px;
        -fx-text-fill: rgba(30, 30, 30, 0.75);
      """

    def radioBox(isSelected: Boolean): String =
      s"""
        -fx-border-radius: 4px;
        -fx-background-radius: 4px;
        -fx-border-color: rgba(30, 30, 30, 0.75);
        -fx-border-width: 2px;
        -fx-background-color: ${
          if isSelected then "rgba(30, 30, 30, 0.25)" else "transparent"
        };
        -fx-padding: 8px;
      """

    val hiddenDot: String =
      """
        -fx-background-color: transparent;
        -fx-pref-width: 0;
        -fx-pref-height: 0;
      """

  private val themeComboBox: ComboBox[String] = new ComboBox[String] {
    maxWidth = Double.MaxValue
    prefHeight = 48
    minHeight = 48
    promptText = "Choose a theme"
    items = ObservableBuffer(
      "Murder",
      "Theft",
      "Hacking",
      "Fantasy"
    )
    style =
      Styles.borderedBox() + s"-fx-font-family: '${Typography.sectionFont.getName}';"
  }

  private val difficultyToggleGroup: ToggleGroup = ToggleGroup()

  private val easyRadio: RadioButton =
    createCustomRadioButton(difficultyToggleGroup)
  private val mediumRadio: RadioButton =
    createCustomRadioButton(difficultyToggleGroup)
  private val hardRadio: RadioButton =
    createCustomRadioButton(difficultyToggleGroup)

  private val loadingOverlay: StackPane = new StackPane:
    visible = false
    managed = false
    background = new Background(Array(
      new BackgroundFill(
        Color.rgb(0, 0, 0, 0.7),
        CornerRadii.Empty,
        Insets.Empty
      )
    ))
    children = Seq(
      new VBox:
        alignment = Pos.Center
        spacing = 20
        children = Seq(
          new Text("Generating case..."):
            font = Typography.sectionFont
            fill = Color.White
            textAlignment =
              TextAlignment.Center
          ,
          new Text("Please wait while creating your mystery case"):
            font = Typography.subsectionFont
            fill = Color.White
            textAlignment = TextAlignment.Center
        )
    )

  private def showLoadingState(): Unit =
    loadingOverlay.visible = true
    loadingOverlay.managed = true

  private def hideLoadingState(): Unit =
    loadingOverlay.visible = false
    loadingOverlay.managed = false

  private def showErrorMessage(message: String): Unit =
    // TODO: Improve error display (e.g., Alert dialog)
    println(s"[View] Showing error to user: $message")

  private def createBackground(): Background =
    val image = BackgroundImage(
      image = Image(
        getClass.getResourceAsStream("/images/game-configuration/desk.png")
      ),
      repeatX = BackgroundRepeat.NoRepeat,
      repeatY = BackgroundRepeat.NoRepeat,
      position = BackgroundPosition.Center,
      size = BackgroundSize(
        width = BackgroundSize.Auto,
        height = BackgroundSize.Auto,
        widthAsPercentage = true,
        heightAsPercentage = true,
        contain = false,
        cover = true
      )
    )
    Background(Array(image))

  private def createContentBox(): Background =
    Background(Array(
      BackgroundFill(
        fill = Theme.backgroundColor,
        radii = Theme.cornerRadius,
        insets = Insets.Empty
      )
    ))

  private def createHorizontalDivider(): Region =
    new Region {
      prefHeight = 4
      minHeight = 4
      maxHeight = 4
      style = Styles.borderedBox(Theme.smallBorderWidth)
    }

  private def createStyledText(
      content: String,
      textFont: Font,
      alignment: TextAlignment = TextAlignment.Left
  ): Text =
    new Text(content) {
      font = textFont
      textAlignment = alignment
      fill = Theme.primaryColor
    }

  private def createCustomRadioButton(group: ToggleGroup): RadioButton =
    val radioButton = new RadioButton {
      toggleGroup = group

      delegate.skinProperty().addListener((_, _, newSkin) => {
        if newSkin != null then
          val radioBox = delegate.lookup(".radio")
          if radioBox != null then
            radioBox.setStyle(Styles.radioBox(false))

            val dot = radioBox.lookup(".dot")
            if dot != null then
              dot.setStyle(Styles.hiddenDot)
      })
    }

    radioButton.selected.onChange { (_, _, isSelected) =>
      val radioBox = radioButton.delegate.lookup(".radio")
      if radioBox != null then
        radioBox.setStyle(Styles.radioBox(isSelected))
    }

    radioButton

  private def createDifficultyOption(
      label: String,
      radioButton: RadioButton
  ): HBox =
    new HBox {
      alignment = Pos.CenterLeft
      spacing = 16
      children = Seq(
        radioButton,
        createStyledText(label, Typography.subsectionFont)
      )
    }

  private def createHeader(): VBox =
    new VBox {
      alignment = Pos.Center
      spacing = 16
      children = Seq(
        createStyledText(
          "Game Configuration",
          Typography.titleFont,
          TextAlignment.Center
        )
      )
    }

  private def createThemeSection(): VBox =
    new VBox {
      spacing = 16
      children = Seq(
        createStyledText("Theme", Typography.sectionFont),
        themeComboBox
      )
    }

  private def createDifficultySection(): VBox =
    new VBox {
      spacing = 16
      children = Seq(
        createStyledText("Difficulty", Typography.sectionFont),
        new VBox {
          spacing = 16
          children = Seq(
            createDifficultyOption("Easy", easyRadio),
            createDifficultyOption("Medium", mediumRadio),
            createDifficultyOption("Hard", hardRadio)
          )
        }
      )
    }

  private def createBody(): VBox =
    new VBox {
      spacing = 32
      fillWidth = true
      children = Seq(
        createThemeSection(),
        createDifficultySection()
      )
    }

  private def handleCancel(): Unit =
    println("Cancel button clicked")
    navigateTo(ScenePage.Homepage)

  private def handlePlay(): Unit =
    val selectedTheme = Option(themeComboBox.value.value).getOrElse("None")

    val selectedDifficulty =
      Option(difficultyToggleGroup.selectedToggle.value).flatMap { toggle =>
        if toggle == easyRadio.delegate then Some("Easy")
        else if toggle == mediumRadio.delegate then Some("Medium")
        else if toggle == hardRadio.delegate then Some("Hard")
        else None
      }.getOrElse("Easy")

    println(s"[View] Selected Theme: $selectedTheme")
    println(s"[View] Selected Difficulty: $selectedDifficulty")

    showLoadingState()

    controller.onPlayClicked(
      selectedDifficulty,
      selectedTheme,
      onSuccess = () => {
        Platform.runLater {
          println(s"[View] Case generation successful, switching to Game Board")
          hideLoadingState()
          navigateTo(ScenePage.GameBoard)
        }
      },
      onError = (errorMessage: String) => {
        Platform.runLater {
          println(s"[View] Error in generation: $errorMessage")
          hideLoadingState()
          showErrorMessage(errorMessage)
        }
      }
    )

  private def createActionButton(label: String, onClick: => Unit): Button =
    new Button(label) {
      prefWidth = 150
      prefHeight = 32
      alignment = Pos.Center
      style = Styles.borderedBox()
      font = Typography.sectionFont
      onAction = _ => onClick
    }

  private def createFooter(): HBox =
    new HBox {
      spacing = 100
      alignment = Pos.Center
      children = Seq(
        createActionButton("Cancel", handleCancel()),
        createActionButton("Play", handlePlay())
      )
    }

  root = new StackPane {
    background = createBackground()
    children = Seq(
      new VBox {
        maxWidth = 480
        maxHeight = 640
        spacing = 32
        alignment = Pos.Center
        padding = Insets(top = 64, right = 32, bottom = 64, left = 32)
        background = createContentBox()
        children = Seq(
          createHorizontalDivider(),
          createHeader(),
          createHorizontalDivider(),
          createBody(),
          createHorizontalDivider(),
          createFooter()
        )
      },
      loadingOverlay
    )
  }
