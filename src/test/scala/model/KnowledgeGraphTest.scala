package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KnowledgeGraphTest extends AnyFlatSpec with Matchers:

  "A BaseGraph" must "be initially empty" in:
    val g = new BaseGraph:
      override type Node = Unit
      override type Edge = Unit
    g.isEmpty shouldBe true

  "An empty BaseGraph" must "not be empty after adding one node" in:
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

  "Out edges" should "be empty if the node is orphan" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.outEdges(1).isEmpty shouldBe true

  it should "not be empty if the node is not orphan" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.outEdges(1).isEmpty shouldBe false

  "Adding an edge" should "connect a node with another" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.outEdges(1).contains("test") shouldBe true

  it should "not connect non existing nodes" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 3)
    g.outEdges(1).contains("test") shouldBe false

  it should "not be done if the edge between the given nodes already exist" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.addEdge(1, "test", 2)
    g.outEdges(1).size shouldBe 1
