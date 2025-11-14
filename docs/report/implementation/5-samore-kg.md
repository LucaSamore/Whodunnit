# Knowledge Graph

One of my main contributions to this project was the design and implementation of the `KnowledgeGraph`.
As mentioned in the Detailed Design section, the implementation leverages **family polymorphism** to create a flexible design that supports various types of graphs.
This choice was primarily driven by the need to represent the solution's prerequisite graph, which may not always be a semantic graph, while ensuring the user-constructed graph is always a concrete `CaseKnowledgeGraph`.

The design is structured in layers, starting from a generic `Graph` trait and progressively adding constraints and concrete types:

```scala
trait Graph:
  type Node
  type Edge

  def nodes: Set[Node]
  def edges: Set[(Node, Edge, Node)]
  def addNode(n: Node): Unit
  def removeNode(n: Node): Unit
  def addEdge(n1: Node, e: Edge, n2: Node): Unit
  def removeEdge(n1: Node, e: Edge, n2: Node): Unit
  // more graph methods...

abstract class BaseOrientedGraph extends Graph:
  protected val data: mutable.Map[Node, List[(Node, Edge)]] = mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet

  override def edges: Set[(Node, Edge, Node)] =
    (for
      (src, edgeList) <- data
      (dest, edge) <- edgeList
    yield (src, edge, dest)).toSet

  // more graph methods implementation...
```

This layered approach, combining an abstract class with mixin traits, allows for a clear separation of concerns: `BaseOrientedGraph` provides the storage and logic for a directed graph, `KnowledgeGraph` enforces the semantic constraint on edges, and `CaseNodesAndEdges` specifies the concrete types for nodes and edges.

```scala
trait KnowledgeGraph extends Graph:
  type Edge <: { def semantic: String }
  
  // (optional) additional methods on KGs

trait CaseNodesAndEdges:
  self: Graph =>
  type Node = Entity // a case entity (e.g., Character, Document or Custom)
  type Edge = Link   // relationship between two case entities
```

The graph's logic was thoroughly verified through unit tests using ScalaTest's `AnyWordSpec` style, ensuring the reliability of all core operations.

```scala
"A BaseOrientedGraph" when:
  "adding edges" should:
    "connect two nodes" in:
      val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
      g.outEdges(1) should contain("test")
  
  "removing edges" should:
    "remove the edge between two nodes" in:
      val g = graph.withNodes(1, 2).withEdge(1, "test", 2)
      g.removeEdge(1, "test", 2)
      g.outEdges(1) should not contain "test"
```

Furthermore, the `CaseKnowledgeGraph` is designed to be serializable to and deserializable from JSON. This is achieved by providing a `given` instance of `upickle.default.ReadWriter`.
Serialization is important for passing the current state of the player's graph as context to the LLM for hint generation.
Deserialization is used to parse the prerequisite graph, which is part of the solution generated for each case.

```scala
case class SerializableGraph(nodes: Set[Entity], 
                             edges: Set[(Entity, Link, Entity)]) derives ReadWriter

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
```