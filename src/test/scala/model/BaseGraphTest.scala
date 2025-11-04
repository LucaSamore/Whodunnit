package model

import model.knowledgegraph.BaseGraph
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class BaseGraphTest extends AnyWordSpec with Matchers:

  private def graph: BaseGraph { type Node = Int; type Edge = String } =
    new BaseGraph:
      override type Node = Int
      override type Edge = String

  "A BaseGraph" when:
    "created" should:
      "be initially empty" in:
        graph.isEmpty shouldBe true

      "not be empty after adding one node" in:
        graph.withNodes(1).isEmpty shouldBe false

      "contain one node after adding it" in:
        graph.withNodes(1).nodes.size shouldBe 1

    "querying out edges" should:
      "be empty for an orphan node" in:
        graph.withNodes(1).outEdges(1) shouldBe empty

      "not be empty when node has outgoing edges" in:
        graph.withNodes(
          1,
          2
        ).withEdge(1, "test", 2).outEdges(1).isEmpty shouldBe false

    "adding edges" should:
      "connect two nodes" in:
        graph.withNodes(
          1,
          2
        ).withEdge(1, "test", 2).outEdges(1) should contain("test")

      "not connect non-existing nodes" in:
        graph.withNodes(
          1,
          2
        ).withEdge(1, "test", 3).outEdges(1) should not contain "test"

      "prevent duplicate edges" in:
        graph.withNodes(1, 2).withEdge(
          1,
          "test",
          2
        ).withEdge(1, "test", 2).outEdges(1).size shouldBe 1

    "removing nodes" should:
      "make graph empty when it was the only node" in:
        val g = graph.withNodes(1)
        g.removeNode(1)
        g.isEmpty shouldBe true

      "remove the node from nodes set" in:
        val g = graph.withNodes(1, 2)
        g.removeNode(1)
        g.nodes should not contain 1

      "decrease node count" in:
        val g = graph.withNodes(1, 2)
        g.removeNode(1)
        g.nodes.size shouldBe 1

      "remove all outgoing edges" in:
        val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
        g.removeNode(1)
        g.outEdges(1) shouldBe empty

      "remove incoming edges from all sources" in:
        val g = graph.withNodes(
          1,
          2,
          3
        ).withEdge(1, "edge1", 2).withEdge(3, "edge2", 2)
        g.removeNode(2)
        g.outEdges(1) should not contain "edge1"
        g.outEdges(3) should not contain "edge2"

      "preserve unaffected edges" in:
        val g = graph.withNodes(
          1,
          2,
          3
        ).withEdge(1, "keep", 3).withEdge(1, "remove", 2)
        g.removeNode(2)
        g.outEdges(1) should contain("keep")

      "maintain correct edge count after removal" in:
        val g = graph.withNodes(
          1,
          2,
          3
        ).withEdge(1, "keep", 3).withEdge(1, "remove", 2)
        g.removeNode(2)
        g.outEdges(1).size shouldBe 1

      "be idempotent for non-existing nodes" in:
        val g = graph.withNodes(1)
        g.removeNode(2)
        g.nodes should contain(1)
        g.nodes.size shouldBe 1

    "querying in edges" should:
      "be empty when no nodes point to it" in:
        graph.withNodes(1, 2).withEdge(1, "test", 2).inEdges(1) shouldBe empty

      "contain edges from nodes pointing to it" in:
        graph.withNodes(
          1,
          2
        ).withEdge(1, "test", 2).inEdges(2) should contain("test")

      "contain all incoming edges" in:
        val g = graph.withNodes(
          1,
          2,
          3
        ).withEdge(1, "edge1", 3).withEdge(2, "edge2", 3)
        g.inEdges(3).size shouldBe 2
        g.inEdges(3) should contain allOf ("edge1", "edge2")

      "be empty for nodes with no incoming edges" in:
        graph.withNodes(
          1,
          2,
          3
        ).withEdge(2, "test", 3).inEdges(1) shouldBe empty

      "be empty for non-existing nodes" in:
        graph.withNodes(1).inEdges(2) shouldBe empty

    "removing edges" should:
      "remove the edge between two nodes" in:
        val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
        g.removeEdge(1, "test", 2)
        g.outEdges(1) should not contain "test"

      "preserve other edges from same source" in:
        val g = graph.withNodes(
          1,
          2,
          3
        ).withEdge(1, "edge1", 2).withEdge(1, "edge2", 3)
        g.removeEdge(1, "edge1", 2)
        g.outEdges(1) should contain("edge2")
        g.outEdges(1).size shouldBe 1

      "preserve edges with same label to different targets" in:
        val g =
          graph.withNodes(1, 2, 3).withEdge(1, "test", 2).withEdge(1, "test", 3)
        g.removeEdge(1, "test", 2)
        g.outEdges(1) should contain("test")

      "not affect edges from different sources" in:
        val g =
          graph.withNodes(1, 2, 3).withEdge(1, "test", 3).withEdge(2, "test", 3)
        g.removeEdge(1, "test", 3)
        g.outEdges(2) should contain("test")

      "be idempotent for non-existing edges" in:
        val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
        g.removeEdge(1, "other", 2)
        g.outEdges(1) should contain("test")
        g.outEdges(1).size shouldBe 1

      "be safe when source node does not exist" in:
        val g = graph.withNodes(2)
        g.removeEdge(1, "test", 2)
        g.nodes.size shouldBe 1

      "be safe when target node does not exist" in:
        val g = graph.withNodes(1)
        g.removeEdge(1, "test", 2)
        g.outEdges(1) shouldBe empty
