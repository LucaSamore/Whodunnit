package view

import model.game
import model.game.{
  CaseFile,
  CaseFileType,
  CaseKnowledgeGraph,
  Character,
  CustomEntity,
  Entity,
  Link
}
import scalafx.Includes.*
import scalafx.geometry.{Insets, Point2D, Pos}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Pane, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text, TextAlignment}

case class DragState(startPosition: Point2D, startNodePosition: Point2D)

class GraphNode(
    val entity: Entity,
    nodeImage: Image,
    initialPosition: Point2D,
    nodeSize: Double,
    nodeFont: Font
) extends StackPane:

  private var position: Point2D = initialPosition
  private var dragState: Option[DragState] = None
  private val positionListeners =
    scala.collection.mutable.ListBuffer[() => Unit]()

  def getPosition: Point2D = position
  def addPositionListener(listener: () => Unit): Unit =
    positionListeners += listener
  private def notifyPositionListeners(): Unit = positionListeners.foreach(_())

  private def updateLayout(pos: Point2D): Unit =
    layoutX = pos.x - nodeSize / 2
    layoutY = pos.y - nodeSize / 2

  private def moveToPosition(newPosition: Point2D): Unit =
    position = newPosition
    updateLayout(newPosition)
    notifyPositionListeners()

  private val labelText = entity match
    case character: Character       => character.name
    case file: CaseFile             => file.title
    case customEntity: CustomEntity => customEntity.entityType

  private val label = new Text:
    text = labelText
    fill = Color.White
    textAlignment = TextAlignment.Center
    wrappingWidth = nodeSize + 40
    font = nodeFont

  children = Seq(
    new ImageView:
      image = nodeImage
      fitWidth = nodeSize
      fitHeight = nodeSize
      preserveRatio = true
    ,
    new VBox:
      alignment = Pos.BottomCenter
      translateY = nodeSize / 2 + 10
      children = Seq(label)
  )

  updateLayout(initialPosition)
  style = "-fx-cursor: hand;"

  onMousePressed = (event: MouseEvent) =>
    dragState = Some(DragState(Point2D(event.sceneX, event.sceneY), position))
    style = "-fx-cursor: move;"

  onMouseDragged = (event: MouseEvent) =>
    dragState.foreach { state =>
      val delta =
        Point2D(event.sceneX, event.sceneY).subtract(state.startPosition)
      moveToPosition(state.startNodePosition.add(delta))
    }

  onMouseReleased = (_: MouseEvent) =>
    dragState = None
    style = "-fx-cursor: hand;"

class GraphEdge(
    val sourceNode: GraphNode,
    val targetNode: GraphNode,
    link: Link,
    edgeFont: Font,
    arrowImage: Image
) extends Pane:

  private val labelOffset = Point2D(0, 20)

  private val edgeLabel = new Text:
    text = link.semantic
    fill = Color.White
    textAlignment = TextAlignment.Center
    font = edgeFont

  private val arrowImageView = new ImageView:
    image = arrowImage
    opacity = 0.5

  children = Seq(arrowImageView, edgeLabel)

  def updateEdge(): Unit =
    val midPoint = sourceNode.getPosition.midpoint(targetNode.getPosition)
    val angle = math.atan2(
      targetNode.getPosition.y - sourceNode.getPosition.y,
      targetNode.getPosition.x - sourceNode.getPosition.x
    )
    val distance = sourceNode.getPosition.distance(targetNode.getPosition)

    val arrowWidth = math.max(60.0, math.min(1000.0, distance * 0.9))
    val arrowHeight = 15.0

    arrowImageView.fitWidth = arrowWidth
    arrowImageView.fitHeight = arrowHeight
    arrowImageView.rotate = math.toDegrees(angle)
    arrowImageView.layoutX = midPoint.x - arrowWidth / 2
    arrowImageView.layoutY = midPoint.y - arrowHeight / 2

    val perpendicularAngle = angle + math.Pi / 2
    val offsetX = if labelOffset.x == 0 then
      labelOffset.y * math.cos(perpendicularAngle)
    else labelOffset.x
    val offsetY = if labelOffset.x == 0 then
      labelOffset.y * math.sin(perpendicularAngle)
    else labelOffset.y
    val labelPosition = midPoint.add(offsetX, offsetY)

    edgeLabel.layoutX =
      labelPosition.x - edgeLabel.layoutBounds.value.getWidth / 2
    edgeLabel.layoutY =
      labelPosition.y - edgeLabel.layoutBounds.value.getHeight / 2

class KnowledgeGraphView(
    val knowledgeGraph: CaseKnowledgeGraph,
    viewDimensions: (Double, Double)
) extends Pane:

  private val (viewWidth, viewHeight) = viewDimensions
  private val nodeSize = viewWidth / 30.0
  private val areaMargin = viewWidth * 3.0 / 25.0
  private val nodeMargin = viewWidth / 10
  private val minNodeDistance = nodeSize * 5
  private val viewPadding = viewWidth / 25.0

  prefWidth = viewWidth
  prefHeight = viewHeight
  padding = Insets(viewPadding)

  private val nodeFont = Font.loadFont(
    getClass.getResourceAsStream("/fonts/GloriaHallelujah-Regular.ttf"),
    viewWidth / 100
  )

  private val imagesPath = "/images/gameboard/icons/"
  private lazy val images = Map(
    "character" -> new Image(
      getClass.getResourceAsStream(imagesPath + "man-icon.png")
    ),
    "document" -> new Image(
      getClass.getResourceAsStream(imagesPath + "document-icon.png")
    ),
    "email" -> new Image(
      getClass.getResourceAsStream(imagesPath + "email-icon.png")
    ),
    "sms" -> new Image(
      getClass.getResourceAsStream(imagesPath + "SMS-icon.png")
    ),
    "default" -> new Image(
      getClass.getResourceAsStream(imagesPath + "pin-icon.png")
    ),
    "arrow" -> new Image(
      getClass.getResourceAsStream(imagesPath + "arrow-icon.png")
    )
  )

  private val graphNodes = scala.collection.mutable.Map[Entity, GraphNode]()

  private def selectNodeImage(entity: Entity): Image = entity match
    case _: Character       => images("character")
    case caseFile: CaseFile => caseFile.kind match
        case CaseFileType.Email        => images("email")
        case game.CaseFileType.Message => images("sms")
        case _                         => images("document")
    case _ => images("default")

  private def generateRandomPositions(entities: Seq[Entity])
      : Map[Entity, Point2D] =
    val random = new scala.util.Random()
    val xRange = (areaMargin + nodeMargin, viewWidth - areaMargin - nodeMargin)
    val yRange = (
      areaMargin + nodeMargin - viewHeight * 0.3,
      viewHeight - areaMargin - nodeMargin * 2
    )

    entities.map { entity =>
      entity -> Point2D(
        xRange._1 + random.nextDouble() * (xRange._2 - xRange._1),
        yRange._1 + random.nextDouble() * (yRange._2 - yRange._1)
      )
    }.toMap

  private def optimizeNodePositions(
      entities: Seq[Entity],
      initialPositions: Map[Entity, Point2D]
  ): Map[Entity, Point2D] =
    val positions = scala.collection.mutable.Map(initialPositions.toSeq: _*)
    val xRange = (areaMargin + nodeMargin, viewWidth - areaMargin - nodeMargin)
    val yRange = (
      areaMargin + nodeMargin - viewHeight * 0.3,
      viewHeight - areaMargin - nodeMargin * 2
    )

    def constrainPosition(point: Point2D): Point2D = Point2D(
      math.max(xRange._1, math.min(xRange._2, point.x)),
      math.max(yRange._1, math.min(yRange._2, point.y))
    )

    for
      _ <- 0 until 100; entity1 <- entities; entity2 <- entities
      if entity1 != entity2
    do
      val position1 = positions(entity1)
      val position2 = positions(entity2)
      val distance = position1.distance(position2)

      if distance < minNodeDistance && distance > 0 then
        val pushForce = (minNodeDistance - distance) / 2
        val angle =
          math.atan2(position2.y - position1.y, position2.x - position1.x)
        val pushVector =
          Point2D(pushForce * math.cos(angle), pushForce * math.sin(angle))

        positions(entity1) = constrainPosition(position1.subtract(pushVector))
        positions(entity2) = constrainPosition(position2.add(pushVector))

    positions.toMap

  def renderGraph(): Unit =
    children.clear()
    graphNodes.clear()

    val entities = knowledgeGraph.nodes.toSeq
    val optimizedPositions =
      optimizeNodePositions(entities, generateRandomPositions(entities))

    entities.foreach { entity =>
      val node = new GraphNode(
        entity,
        selectNodeImage(entity),
        optimizedPositions(entity),
        nodeSize,
        nodeFont
      )
      graphNodes(entity) = node
      children.add(node)
    }

    for
      sourceEntity <- entities
      targetEntity <- entities
      link <- knowledgeGraph.outEdges(sourceEntity)
      if knowledgeGraph.inEdges(targetEntity).contains(link)
      if graphNodes.contains(sourceEntity) && graphNodes.contains(targetEntity)
    do
      val edge = new GraphEdge(
        graphNodes(sourceEntity),
        graphNodes(targetEntity),
        link,
        nodeFont,
        images("arrow")
      )
      children.add(0, edge)
      edge.updateEdge()
      graphNodes(sourceEntity).addPositionListener(() => edge.updateEdge())
      graphNodes(targetEntity).addPositionListener(() => edge.updateEdge())

object KnowledgeGraphView:
  def apply(
      knowledgeGraph: CaseKnowledgeGraph,
      viewDimensions: (Double, Double)
  ): KnowledgeGraphView =
    val view = new KnowledgeGraphView(knowledgeGraph, viewDimensions)
    view.renderGraph()
    view
