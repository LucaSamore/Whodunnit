package model.knowledgegraph

import model.casegeneration.Entity
import model.*

import scala.collection.mutable

trait Graph:
  type Node
  type Edge

  def nodes: Set[Node]
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

abstract class BaseGraph extends Graph:
  private val data = mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet

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

case class Link(semantic: String)

class CaseKnowledgeGraph extends BaseGraph
    with KnowledgeGraph
    with CaseNodesAndEdges
