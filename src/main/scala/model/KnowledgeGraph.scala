package model

import scala.collection.mutable

trait Graph:
  type Node
  type Edge

  def nodes: Set[Node]
  def addNode(n: Node): Unit
  def isEmpty: Boolean

abstract class BaseGraph extends Graph:
  private val data = mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet
  override def addNode(n: Node): Unit = data addOne n -> List()
  override def isEmpty: Boolean = data.isEmpty
