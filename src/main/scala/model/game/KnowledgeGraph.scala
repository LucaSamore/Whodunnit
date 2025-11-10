package model.game

import model.*
import upickle.default._

import scala.collection.mutable

trait Graph:
  type Node
  type Edge

  def nodes: Set[Node]
  def edges: Set[(Node, Edge, Node)]
  def addNode(n: Node): Unit
  def removeNode(n: Node): Unit
  def addEdge(n1: Node, e: Edge, n2: Node): Unit
  def removeEdge(n1: Node, e: Edge, n2: Node): Unit
  def inEdges(n: Node): Set[Edge]
  def outEdges(n: Node): Set[Edge]
  def isEmpty: Boolean

  def withNodes(nodes: Node*): this.type =
    nodes.foreach(addNode)
    this

  def withEdge(n1: Node, edge: Edge, n2: Node): this.type =
    addEdge(n1, edge, n2)
    this

abstract class BaseOrientedGraph extends Graph:
  protected val data: mutable.Map[Node, List[(Node, Edge)]] =
    mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet

  override def edges: Set[(Node, Edge, Node)] =
    (for
      (src, edgeList) <- data
      (dest, edge) <- edgeList
    yield (src, edge, dest)).toSet

  override def addNode(n: Node): Unit = data.addOne(n -> List())

  override def removeNode(n: Node): Unit =
    data.remove(n)
    data.mapValuesInPlace((_, edges) => edges.filterNot(_._1 == n))

  override def addEdge(n1: Node, e: Edge, n2: Node): Unit =
    if data.contains(n1) && data.contains(n2) && !data(n1).contains((n2, e))
    then
      data.update(n1, (n2, e) :: data(n1))

  override def removeEdge(n1: Node, e: Edge, n2: Node): Unit =
    data.updateWith(n1):
      case Some(edges) => Some(edges.filterNot(_ == (n2, e)))
      case None        => None

  override def inEdges(n: Node): Set[Edge] = data.values.flatten.collect:
    case (target, edge) if target == n => edge
  .toSet

  override def outEdges(n: Node): Set[Edge] = data.get(n) match
    case Some(edges) => edges.map(_._2).toSet
    case _           => Set.empty

  override def isEmpty: Boolean = data.isEmpty

trait KnowledgeGraph extends Graph:
  type Edge <: { def semantic: String }

trait CaseNodesAndEdges:
  self: Graph =>
  type Node = Entity
  type Edge = Link

final case class Link(semantic: String) derives ReadWriter

class CaseKnowledgeGraph extends BaseOrientedGraph with KnowledgeGraph with CaseNodesAndEdges:
  def deepCopy(): CaseKnowledgeGraph =
    val newGraph = new CaseKnowledgeGraph
    nodes.foreach(newGraph.addNode)
    nodes
      .flatMap(n1 =>
        outEdges(n1).flatMap(e =>
          nodes
            .filter(n2 => data.get(n1).exists(_.contains((n2, e))))
            .map((n1, e, _))
        )
      )
      .foreach { case (n1, e, n2) => newGraph.addEdge(n1, e, n2) }
    newGraph

final case class SerializableGraph(nodes: Set[Entity], edges: Set[(Entity, Link, Entity)]) derives ReadWriter

given ReadWriter[CaseKnowledgeGraph] = readwriter[ujson.Value].bimap[CaseKnowledgeGraph](
  graph => writeJs(SerializableGraph(graph.nodes, graph.edges)),
  json => {
    val sg = read[SerializableGraph](json)
    val graph = new CaseKnowledgeGraph
    sg.nodes.foreach(graph.addNode)
    sg.edges.foreach { case (n1, e, n2) => graph.addEdge(n1, e, n2) }
    graph
  }
)
