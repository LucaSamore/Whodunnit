package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BaseGraphTest extends AnyFlatSpec with Matchers:

  private def graph: BaseGraph { type Node = Int; type Edge = String } =
    new BaseGraph:
      override type Node = Int
      override type Edge = String

  private def emptyGraph: BaseGraph { type Node = Unit; type Edge = Unit } =
    new BaseGraph:
      override type Node = Unit
      override type Edge = Unit

  extension (g: BaseGraph { type Node = Int; type Edge = String })
    private def withNodes(nodes: Int*): g.type =
      nodes.foreach(g.addNode)
      g

    private def withEdge(n1: Int, edge: String, n2: Int): g.type =
      g.addEdge(n1, edge, n2)
      g

  "A BaseGraph" must "be initially empty" in:
    emptyGraph.isEmpty shouldBe true

  "An empty BaseGraph" must "not be empty after adding one node" in:
    graph.withNodes(1).isEmpty shouldBe false

  it must "contain one node after adding it" in:
    graph.withNodes(1).nodes.size shouldBe 1

  "Out edges" should "be empty for an orphan node" in:
    graph.withNodes(1).outEdges(1).isEmpty shouldBe true

  it should "not be empty when node has outgoing edges" in:
    graph.withNodes(
      1,
      2
    ).withEdge(1, "test", 2).outEdges(1).isEmpty shouldBe false

  "Adding an edge" should "connect two nodes" in:
    graph.withNodes(
      1,
      2
    ).withEdge(1, "test", 2).outEdges(1) should contain("test")

  it should "not connect non-existing nodes" in:
    graph.withNodes(
      1,
      2
    ).withEdge(1, "test", 3).outEdges(1) should not contain "test"

  it should "prevent duplicate edges" in:
    graph.withNodes(
      1,
      2
    ).withEdge(1, "test", 2).withEdge(1, "test", 2).outEdges(1).size shouldBe 1

  "Removing a node" should "make graph empty when it was the only node" in:
    val g = graph.withNodes(1)
    g.removeNode(1)
    g.isEmpty shouldBe true

  it should "remove the node from nodes set" in:
    val g = graph.withNodes(1, 2)
    g.removeNode(1)
    g.nodes should not contain 1

  it should "decrease node count" in:
    val g = graph.withNodes(1, 2)
    g.removeNode(1)
    g.nodes.size shouldBe 1

  it should "remove all outgoing edges" in:
    val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
    g.removeNode(1)
    g.outEdges(1) shouldBe empty

  it should "remove incoming edges from first source" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "edge1", 2).withEdge(3, "edge2", 2)
    g.removeNode(2)
    g.outEdges(1) should not contain "edge1"

  it should "remove incoming edges from other sources" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "edge1", 2).withEdge(3, "edge2", 2)
    g.removeNode(2)
    g.outEdges(3) should not contain "edge2"

  it should "preserve unaffected edges" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "keep", 3).withEdge(1, "remove", 2)
    g.removeNode(2)
    g.outEdges(1) should contain("keep")

  it should "maintain correct edge count after removal" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "keep", 3).withEdge(1, "remove", 2)
    g.removeNode(2)
    g.outEdges(1).size shouldBe 1

  it should "be idempotent for non-existing nodes" in:
    val g = graph.withNodes(1)
    g.removeNode(2)
    g.nodes.size shouldBe 1

  it should "preserve existing nodes when removing non-existing node" in:
    val g = graph.withNodes(1)
    g.removeNode(2)
    g.nodes should contain(1)

  "In edges" should "be empty when no nodes point to it" in:
    graph.withNodes(1, 2).withEdge(1, "test", 2).inEdges(1) shouldBe empty

  it should "contain edges from nodes pointing to it" in:
    graph.withNodes(
      1,
      2
    ).withEdge(1, "test", 2).inEdges(2) should contain("test")

  it should "have correct size for multiple incoming edges" in:
    graph.withNodes(
      1,
      2,
      3
    ).withEdge(1, "edge1", 3).withEdge(2, "edge2", 3).inEdges(3).size shouldBe 2

  it should "contain all incoming edges" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "edge1", 3).withEdge(2, "edge2", 3)
    g.inEdges(3) should contain allOf ("edge1", "edge2")

  it should "be empty for nodes with no incoming edges" in:
    graph.withNodes(1, 2, 3).withEdge(2, "test", 3).inEdges(1) shouldBe empty

  it should "be empty for non-existing nodes" in:
    graph.withNodes(1).inEdges(2) shouldBe empty

  "Removing an edge" should "remove the edge between two nodes" in:
    val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
    g.removeEdge(1, "test", 2)
    g.outEdges(1) should not contain "test"

  it should "preserve other edges from same source" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "edge1", 2).withEdge(1, "edge2", 3)
    g.removeEdge(1, "edge1", 2)
    g.outEdges(1) should contain("edge2")

  it should "maintain correct edge count from source" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "edge1", 2).withEdge(1, "edge2", 3)
    g.removeEdge(1, "edge1", 2)
    g.outEdges(1).size shouldBe 1

  it should "preserve edges with same label to different targets" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "test", 2).withEdge(1, "test", 3)
    g.removeEdge(1, "test", 2)
    g.outEdges(1) should contain("test")

  it should "not affect edges from different sources" in:
    val g =
      graph.withNodes(1, 2, 3).withEdge(1, "test", 3).withEdge(2, "test", 3)
    g.removeEdge(1, "test", 3)
    g.outEdges(2) should contain("test")

  it should "be idempotent for non-existing edges" in:
    val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
    g.removeEdge(1, "other", 2)
    g.outEdges(1) should contain("test")

  it should "maintain edge count when removing non-existing edge" in:
    val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
    g.removeEdge(1, "other", 2)
    g.outEdges(1).size shouldBe 1

  it should "be safe when source node does not exist" in:
    val g = graph.withNodes(2)
    g.removeEdge(1, "test", 2)
    g.nodes.size shouldBe 1

  it should "be safe when target node does not exist" in:
    val g = graph.withNodes(1)
    g.removeEdge(1, "test", 2)
    g.outEdges(1) shouldBe empty
