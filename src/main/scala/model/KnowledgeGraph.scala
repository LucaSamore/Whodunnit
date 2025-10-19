package model

import scala.collection.mutable

trait Graph:
  type Node
  type Edge

  def isEmpty: Boolean

abstract class BaseGraph extends Graph:
  private val data = mutable.Map[Node, List[(Node, Edge)]]()

  override def isEmpty: Boolean = data.isEmpty
