package model

import scala.collection.mutable

trait Graph:
  type Node
  type Edge

  def nodes: Set[Node]
  def addNode(n: Node): Unit
  def addEdge(n1: Node, e: Edge, n2: Node): Unit
  def outEdges(n: Node): Set[Edge]
  def isEmpty: Boolean

abstract class BaseGraph extends Graph:
  private val data = mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet

  override def addNode(n: Node): Unit = data addOne n -> List()

  override def addEdge(n1: Node, e: Edge, n2: Node): Unit =
    if data.contains(n1) && data.contains(n2) && !outEdges(n1).contains(e) then
      data.update(n1, (n2, e) :: data(n1))

  override def outEdges(n: Node): Set[Edge] = data.get(n) match
    case Some(edges) => edges.map(_._2).toSet
    case _           => Set.empty

  override def isEmpty: Boolean = data.isEmpty
