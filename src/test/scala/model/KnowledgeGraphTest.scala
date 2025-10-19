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

  "Removing a node" should "make the graph empty if it was the only node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.removeNode(1)
    g.isEmpty shouldBe true

  it should "remove the node from the graph" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.removeNode(1)
    g.nodes.contains(1) shouldBe false
    g.nodes.size shouldBe 1

  it should "remove all outgoing edges from the removed node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.removeNode(1)
    g.outEdges(1).isEmpty shouldBe true

  it should "remove all incoming edges to the removed node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "edge1", 2)
    g.addEdge(3, "edge2", 2)
    g.removeNode(2)
    g.outEdges(1).contains("edge1") shouldBe false
    g.outEdges(3).contains("edge2") shouldBe false

  it should "not affect other edges when removing a node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "keep", 3)
    g.addEdge(1, "remove", 2)
    g.removeNode(2)
    g.outEdges(1).contains("keep") shouldBe true
    g.outEdges(1).size shouldBe 1

  it should "do nothing when removing a non-existing node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.removeNode(2)
    g.nodes.size shouldBe 1
    g.nodes.contains(1) shouldBe true

  "In edges" should "be empty if no nodes point to it" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.inEdges(1).isEmpty shouldBe true

  it should "contain edges from nodes pointing to it" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.inEdges(2).contains("test") shouldBe true

  it should "contain all edges pointing to the node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "edge1", 3)
    g.addEdge(2, "edge2", 3)
    g.inEdges(3).size shouldBe 2
    g.inEdges(3).contains("edge1") shouldBe true
    g.inEdges(3).contains("edge2") shouldBe true

  it should "be empty for a node with no incoming edges" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(2, "test", 3)
    g.inEdges(1).isEmpty shouldBe true

  it should "be empty for a non-existing node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.inEdges(2).isEmpty shouldBe true

  "Removing an edge" should "remove the edge between two nodes" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.removeEdge(1, "test", 2)
    g.outEdges(1).contains("test") shouldBe false

  it should "not affect other edges from the same source node" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "edge1", 2)
    g.addEdge(1, "edge2", 3)
    g.removeEdge(1, "edge1", 2)
    g.outEdges(1).contains("edge2") shouldBe true
    g.outEdges(1).size shouldBe 1

  it should "not affect edges with the same label to different targets" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "test", 2)
    g.addEdge(1, "test", 3)
    g.removeEdge(1, "test", 2)
    g.outEdges(1).contains("test") shouldBe true
    g.outEdges(1).size shouldBe 1

  it should "not affect edges from different source nodes" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addNode(3)
    g.addEdge(1, "test", 3)
    g.addEdge(2, "test", 3)
    g.removeEdge(1, "test", 3)
    g.outEdges(2).contains("test") shouldBe true

  it should "do nothing when removing a non-existing edge" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.addNode(2)
    g.addEdge(1, "test", 2)
    g.removeEdge(1, "other", 2)
    g.outEdges(1).contains("test") shouldBe true
    g.outEdges(1).size shouldBe 1

  it should "do nothing when source node does not exist" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(2)
    g.removeEdge(1, "test", 2)
    g.nodes.size shouldBe 1

  it should "do nothing when target node does not exist" in:
    val g = new BaseGraph:
      override type Node = Int
      override type Edge = String
    g.addNode(1)
    g.removeEdge(1, "test", 2)
    g.outEdges(1).isEmpty shouldBe true
