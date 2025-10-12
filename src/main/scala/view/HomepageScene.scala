package view

import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Background, BackgroundImage, BackgroundPosition, BackgroundRepeat, BackgroundSize, BorderPane, HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.*
import scalafx.scene.text.{Font, FontWeight, Text, TextAlignment}

private val sceneWidth = 1280
private val sceneHeight = 720

object HomepageScene extends Scene(sceneWidth, sceneHeight) {
  private val backgroundImage: Image = new Image(getClass.getResourceAsStream("/images/homepage/homepage_background.png"))
  private val titleFont: Font = Font.loadFont(getClass.getResourceAsStream("/fonts/Pacifico-Regular.ttf"), 85)
  private val homepageFont: Font = Font.loadFont(getClass.getResourceAsStream("/fonts/Pacifico-Regular.ttf"), 25)
  private val dropShadow: DropShadow = new DropShadow {
    color = Color.web("#000000")
    radius = 5
    offsetX = 3
    offsetY = 3
  }

  private val titleText = new Text("Whodunnit?") {
    font = Font.font(titleFont.getFamily, FontWeight.Black, titleFont.getSize)
    fill = Color.web("#F5DEB3")
    stroke = Color.web("#412114")
    strokeWidth = 2
    effect = dropShadow
  }

  private val howToPlayButton = new Button("How To Play") {
    minWidth = 250
    minHeight = 100
    alignment = Pos.Center
    font = homepageFont
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
  }

  private val playButton = new Button("Play Now\nas Investi-Gator") {
    minWidth = 400
    minHeight = 100
    textAlignment = TextAlignment.Center
    font = homepageFont
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
      println("Play Now clicked - Changing to Game scene")
      WhodunnitApp.changeScene(GameConfigurationScene)
    }
  }

  private val resumeButton = new Button("Resume Game") {
    minWidth = 400
    minHeight = 70
    alignment = Pos.Center
    font = homepageFont
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
      // WhodunnitApp.changeScene(new GameScene(savedGame))
    }
  }

  private val historyButton = new Button("History") {
    minWidth = 400
    minHeight = 70
    alignment = Pos.Center
    font = homepageFont
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
      //WhodunnitApp.changeScene(new HistoryScene())
    }
  }

  root = new HBox {
    prefWidth = 1280
    prefHeight = 170

    val bgImage = new BackgroundImage(
      backgroundImage,
      BackgroundRepeat.NoRepeat,
      BackgroundRepeat.NoRepeat,
      BackgroundPosition.Center,
      new BackgroundSize(BackgroundSize.Auto, BackgroundSize.Auto, true, true, false, true))
    background = new Background(Array(bgImage))
    val halfSceneWidth: Int = sceneWidth / 2
    val thirdSceneHeight: Int = sceneHeight / 3

    children = Seq(
      new BorderPane() {
        prefWidth = halfSceneWidth
        prefHeight = sceneHeight

        top = howToPlayButton

        center = new ImageView {
          image = new Image(getClass.getResourceAsStream("/images/homepage/investi_gator.png"))
          fitWidth = 500
          fitHeight = 500
          preserveRatio = true
        }
      },
      new VBox {
        prefWidth = halfSceneWidth
        prefHeight = sceneHeight
        fillHeight = true

        children = Seq(
          new BorderPane {
            prefWidth = halfSceneWidth
            prefHeight = thirdSceneHeight
            bottom = titleText
            BorderPane.setAlignment(titleText, Pos.Center)
          },
          new VBox {
            prefWidth = halfSceneWidth
            prefHeight = sceneHeight - thirdSceneHeight
            spacing = 25
            alignment = Pos.Center
            children = Seq(
              playButton,
              resumeButton,
              historyButton
            )
          }
        )
      }
    )
  }
}