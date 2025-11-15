package view.board

import model.game.Hint
import scalafx.beans.property.BooleanProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Label, ScrollPane}
import scalafx.scene.layout.{Priority, Region, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationsPanel(customFont: Font, hints: Option[Seq[Hint]] = None) extends VBox:

  private val isVisible = BooleanProperty(false)

  maxWidth = 400
  maxHeight = 300
  alignment = Pos.TopLeft
  padding = Insets(10)
  spacing = 5

  style = """
    -fx-background-color: #6d492c;
    -fx-background-radius: 8;
    -fx-border-color: rgba(100, 100, 100, 0.5);
    -fx-border-width: 2;
    -fx-border-radius: 8;
  """

  visible <== isVisible
  managed <== isVisible

  private val headerLabel = new Label:
    text = "Notifications"
    font = Font.font(
      customFont.getFamily,
      FontWeight.Bold,
      18
    )
    textFill = Color.White
    padding = Insets(0, 0, 10, 0)

  private val notificationsContainer = new VBox:
    spacing = 5
    padding = Insets(5)
    fillWidth = true

  private val scrollPane = new ScrollPane:
    content = notificationsContainer
    fitToWidth = true
    hbarPolicy = ScrollPane.ScrollBarPolicy.Never
    vbarPolicy = ScrollPane.ScrollBarPolicy.AsNeeded
    style = """
      -fx-background-color: transparent;
      -fx-background: transparent;
    """
    VBox.setVgrow(this, Priority.Always)

  children = Seq(headerLabel, scrollPane)

  addMockNotifications()

  def toggleVisibility(): Unit =
    isVisible.value = !isVisible.value

  def addNotification(message: String): Unit =
    val timestamp = System.currentTimeMillis()
    val notification = createNotificationItem(message, timestamp)
    notificationsContainer.children.add(0, notification)

  private def formatTimestamp(timestamp: Long): String =
    val dateTime = LocalDateTime.ofInstant(
      java.time.Instant.ofEpochMilli(timestamp),
      java.time.ZoneId.systemDefault()
    )
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    dateTime.format(formatter)

  private def createNotificationItem(message: String, timestamp: Long): Region =
    new VBox:
      spacing = 6
      padding = Insets(8)
      style = """
        -fx-background-color: rgba(245, 222, 179, 0.7);
        -fx-background-radius: 5;
        -fx-border-color: rgba(139, 90, 43, 0.3);
        -fx-border-width: 1;
        -fx-border-radius: 5;
      """

      private val messageLabel = new Label:
        text = message
        textFill = Color.White
        font = Font.font(
          customFont.getFamily,
          FontWeight.Normal,
          15
        )
        wrapText = true
        maxWidth = 350
        lineSpacing = -10

      private val timestampLabel = new Label:
        text = formatTimestamp(timestamp)
        textFill = Color.rgb(200, 200, 200)
        font = Font.font(
          customFont.getFamily,
          FontWeight.Normal,
          13
        )

      children = Seq(messageLabel, timestampLabel)

  private def addMockNotifications(): Unit = hints match
    case Some(hints) => hints map (_.description) foreach (addNotification)
    case _           =>

object NotificationsPanel:
  def apply(customFont: Font, hints: Option[Seq[Hint]] = None) = new NotificationsPanel(customFont, hints)
