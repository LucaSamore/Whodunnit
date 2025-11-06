package view

import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{
  Background,
  BackgroundImage,
  BackgroundPosition,
  BackgroundRepeat,
  BackgroundSize,
  BorderPane,
  HBox,
  VBox
}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text, TextAlignment}
import controller.ControllerModule.*

class HomepageScene(controller: Controller[model.State])
  extends Scene(1280, 720):

  import Config._

  private val titleText = new Text("Whodunnit?"):
    font = Font.font(titleFont.getFamily, FontWeight.Black, titleFont.getSize)
    fill = Color.web("#F5DEB3")
    stroke = Color.web("#412114")
    strokeWidth = 2
    effect = dropShadow

  private val howToPlayButton = new Button("How To Play"):
    minWidth = 250
    minHeight = 100
    alignment = Pos.Center
    font = baseFont
    textFill = Color.web("#F5DEB3")
    effect = dropShadow
    style =
      """
            -fx-background-color: transparent;
      """
    onAction = _ => {
      println("How to Play clicked - Changing to Tutorial scene")
      // WhodunnitApp.changeScene(new TutorialScene())
    }

  private val playButton = new Button("Play Now\nas Investi-Gator"):
    minWidth = actionButtonsWidth
    minHeight = 100
    textAlignment = TextAlignment.Center
    font = baseFont
    textFill = Color.web("#F5DEB3")
    effect = dropShadow
    style =
      """
        -fx-background-color: transparent;
        -fx-border-color: #F5DEB3;
        -fx-border-width: 3;
        -fx-border-radius: 35;
        -fx-background-radius: 35;
      """
    onAction = _ => {
      println("[View] Play Now clicked - Changing to Game scene")
      controller.onPlayNowClicked()
      WhodunnitApp.changeScene(GameConfigurationScene(controller))
    }

  private val resumeButton = new Button("Resume Game"):
    minWidth = actionButtonsWidth
    minHeight = 70
    alignment = Pos.Center
    font = baseFont
    textFill = Color.web("#F5DEB3")
    effect = dropShadow
    style =
      """
        -fx-background-color: transparent;
        -fx-border-color: #F5DEB3;
        -fx-border-width: 3;
        -fx-border-radius: 35;
        -fx-background-radius: 35;
      """
    onAction = _ => {
      println("Resume Game clicked - Changing to Game scene")
      // WhodunnitApp.changeScene(new GameScene)
    }

  private val historyButton = new Button("History"):
    minWidth = actionButtonsWidth
    minHeight = 70
    alignment = Pos.Center
    font = baseFont
    textFill = Color.web("#F5DEB3")
    effect = dropShadow
    style =
      """
        -fx-background-color: transparent;
        -fx-border-color: #F5DEB3;
        -fx-border-width: 3;
        -fx-border-radius: 35;
        -fx-background-radius: 35;
      """
    onAction = _ => {
      println("History clicked - Changing to History scene")
      // WhodunnitApp.changeScene(new HistoryScene
    }

  root = new HBox:
    prefWidth = sceneWidth
    prefHeight = 170

    val bgImage = new BackgroundImage(
      backgroundImage,
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
    background = new Background(Array(bgImage))

    children = Seq(
      new BorderPane():
        prefWidth = halfSceneWidth
        prefHeight = sceneHeight

        top = howToPlayButton

        center = new ImageView:
          image = new Image(
            getClass.getResourceAsStream("/images/homepage/investi_gator.png")
          )
          fitWidth = 500
          fitHeight = 500
          preserveRatio =
            true
      ,
      new VBox:
        prefWidth = halfSceneWidth
        prefHeight = sceneHeight
        fillHeight = true

        children = Seq(
          new BorderPane:
            prefWidth = halfSceneWidth
            prefHeight = thirdSceneHeight
            bottom = titleText
            BorderPane.setAlignment(titleText, Pos.Center)
          ,
          new VBox:
            prefWidth = halfSceneWidth
            prefHeight = sceneHeight - thirdSceneHeight
            spacing = 25
            alignment = Pos.Center
            children = Seq(
              playButton,
              resumeButton,
              historyButton
            )
        )
    )

  private object Config:
    val sceneWidth: Int = 1280
    val sceneHeight: Int = 720
    val halfSceneWidth: Int = sceneWidth / 2
    val thirdSceneHeight: Int = sceneHeight / 3
    val actionButtonsWidth: Int = 400
    val backgroundImage: Image = new Image(
      getClass.getResourceAsStream("/images/homepage/homepage_background.png")
    )
    val titleFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/Pacifico-Regular.ttf"),
      85
    )
    val baseFont: Font = Font.loadFont(
      getClass.getResourceAsStream("/fonts/Pacifico-Regular.ttf"),
      25
    )
    val dropShadow: DropShadow = new DropShadow:
      color = Color.web("#000000")
      radius = 5
      offsetX = 3
      offsetY = 3
