package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KnowledgeGraphTest extends AnyFlatSpec with Matchers:

  "A BaseGraph" must "be initially empty" in:
    val g = new BaseGraph:
      override type Node = Unit
      override type Edge = Unit
    g.isEmpty shouldBe true

  "An empty BaseGraph" must "not be empty after adding one Node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = Unit
    g.addNode(1)
    g.isEmpty shouldBe false

  it must "contain one node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = Unit
    g.addNode(1)
    g.nodes.size shouldBe 1
