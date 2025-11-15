package model.game

import model.*
import upickle.default._

import scala.collection.mutable

/** A generic graph trait defining the fundamental structure and operations for a graph data structure.
  *
  * This trait uses type members to provide flexibility in defining what constitutes a node and an edge in concrete
  * implementations. It supports basic graph operations such as adding/removing nodes and edges, querying graph
  * structure, and provides utility methods for graph construction.
  *
  * @example
  *   {{{
  *   trait MyGraph extends Graph:
  *     type Node = String
  *     type Edge = Int
  *   }}}
  */
trait Graph:
  /** The type of nodes in this graph. Must be defined by concrete implementations. */
  type Node

  /** The type of edges in this graph. Must be defined by concrete implementations. */
  type Edge

  /** Returns the set of all nodes in the graph.
    *
    * @return
    *   an immutable set containing all nodes
    */
  def nodes: Set[Node]

  /** Returns the set of all edges in the graph as triples of (source node, edge, target node).
    *
    * @return
    *   an immutable set of edge triples
    */
  def edges: Set[(Node, Edge, Node)]

  /** Adds a node to the graph.
    *
    * @param n
    *   the node to add
    */
  def addNode(n: Node): Unit

  /** Removes a node from the graph. Also removes all edges connected to this node.
    *
    * @param n
    *   the node to remove
    */
  def removeNode(n: Node): Unit

  /** Adds a directed edge from node n1 to node n2.
    *
    * Both nodes must already exist in the graph. If the edge already exists, this operation has no effect.
    *
    * @param n1
    *   the source node
    * @param e
    *   the edge to add
    * @param n2
    *   the target node
    */
  def addEdge(n1: Node, e: Edge, n2: Node): Unit

  /** Removes a directed edge from node n1 to node n2.
    *
    * @param n1
    *   the source node
    * @param e
    *   the edge to remove
    * @param n2
    *   the target node
    */
  def removeEdge(n1: Node, e: Edge, n2: Node): Unit

  /** Returns all incoming edges to the specified node.
    *
    * @param n
    *   the target node
    * @return
    *   a set of edges pointing to the node
    */
  def inEdges(n: Node): Set[Edge]

  /** Returns all outgoing edges from the specified node.
    *
    * @param n
    *   the source node
    * @return
    *   a set of edges originating from the node
    */
  def outEdges(n: Node): Set[Edge]

  /** Checks if the graph is empty (contains no nodes).
    *
    * @return
    *   true if the graph has no nodes, false otherwise
    */
  def isEmpty: Boolean

  /** Adds multiple nodes to the graph and returns this graph instance.
    *
    * This is a convenience method for fluent graph construction.
    *
    * @param nodes
    *   the nodes to add
    * @return
    *   this graph instance
    */
  def withNodes(nodes: Node*): this.type =
    nodes.foreach(addNode)
    this

  /** Adds an edge to the graph and returns this graph instance.
    *
    * This is a convenience method for fluent graph construction.
    *
    * @param n1
    *   the source node
    * @param edge
    *   the edge to add
    * @param n2
    *   the target node
    * @return
    *   this graph instance
    */
  def withEdge(n1: Node, edge: Edge, n2: Node): this.type =
    addEdge(n1, edge, n2)
    this

/** Abstract base class providing a concrete implementation of a directed graph using an adjacency list representation.
  *
  * This class implements the Graph trait by storing nodes and their outgoing edges in a mutable map. Each node maps to
  * a list of (target node, edge) pairs. The implementation supports efficient edge queries and modifications.
  *
  * The Node and Edge types remain abstract and must be specified by concrete subclasses or mixed-in traits.
  */
abstract class BaseOrientedGraph extends Graph:

  protected val data: mutable.Map[Node, List[(Node, Edge)]] = mutable.Map[Node, List[(Node, Edge)]]()

  override def nodes: Set[Node] = data.keys.toSet

  override def edges: Set[(Node, Edge, Node)] =
    (for
      (src, edgeList) <- data
      (dest, edge) <- edgeList
    yield (src, edge, dest)).toSet

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

/** A refinement mixin trait for graphs where edges must have semantic meaning.
  *
  * This trait constrains the Edge type to be a structural type with a `semantic` property (a String). This ensures that
  * all edges in a KnowledgeGraph carry meaningful labels describing the relationship between nodes.
  *
  * @example
  *   {{{
  *   case class SemanticEdge(semantic: String, weight: Int)
  *
  *   class MyKG extends BaseOrientedGraph with KnowledgeGraph:
  *     type Node = String
  *     type Edge = SemanticEdge
  *   }}}
  */
trait KnowledgeGraph extends Graph:
  /** The edge type, constrained to have a semantic property. */
  type Edge <: { def semantic: String }

/** A mixin trait that specifies concrete types for nodes and edges in a case investigation context.
  *
  * This trait defines:
  *   - Node as Entity (which can be Character, CaseFile, or CustomEntity)
  *   - Edge as Link (a semantic relationship between entities)
  *
  * This trait uses a self-type to ensure it can only be mixed into classes that extend Graph.
  */
trait CaseNodesAndEdges:
  self: Graph =>

  /** Concrete node type representing case entities. */
  type Node = Entity

  /** Concrete edge type representing semantic links between entities. */
  type Edge = Link

/** A semantic link representing a relationship between two entities in a case.
  *
  * @param semantic
  *   the semantic description of the relationship (e.g., "met with", "is enemy of", "mentions")
  */
final case class Link(semantic: String) derives ReadWriter

/** A concrete implementation of a directed knowledge graph for case investigation.
  *
  * CaseKnowledgeGraph combines:
  *   - BaseOrientedGraph (provides the adjacency list implementation)
  *   - KnowledgeGraph (enforces semantic edges)
  *   - CaseNodesAndEdges (specifies Entity nodes and Link edges)
  *
  * This graph is used by players to construct their hypotheses during investigation. Nodes represent case entities
  * (characters, case files, or custom entities), and edges represent semantic relationships between them.
  *
  * The graph is mutable to support incremental construction during gameplay, but provides a `deepCopy` method for
  * creating immutable snapshots (e.g., for history tracking).
  *
  * @example
  *   {{{
  *   val graph = new CaseKnowledgeGraph()
  *   val suspect = Character("John Doe", CaseRole.Suspect)
  *   val victim = Character("Jane Smith", CaseRole.Victim)
  *
  *   graph.addNode(suspect)
  *   graph.addNode(victim)
  *   graph.addEdge(suspect, Link("threatened"), victim)
  *   }}}
  */
class CaseKnowledgeGraph extends BaseOrientedGraph with KnowledgeGraph with CaseNodesAndEdges:

  /** Creates a deep copy of this knowledge graph.
    *
    * The copy includes all nodes and edges. This method is used for creating snapshots of the graph state for
    * history/undo functionality.
    *
    * @return
    *   a new CaseKnowledgeGraph with the same nodes and edges
    */
  def deepCopy(): CaseKnowledgeGraph =
    val newGraph = new CaseKnowledgeGraph
    nodes.foreach(newGraph.addNode)
    nodes
      .flatMap(n1 =>
        outEdges(n1).flatMap(e =>
          nodes
            .filter(n2 => data.get(n1).exists(_.contains((n2, e))))
            .map((n1, e, _))
        )
      )
      .foreach { case (n1, e, n2) => newGraph.addEdge(n1, e, n2) }
    newGraph

  /** Returns a JSON string representation of this graph.
    *
    * @return
    *   a JSON string containing the graph's nodes and edges
    */
  override def toString: String =
    write(SerializableGraph(nodes, edges))

/** A serializable representation of a case knowledge graph.
  *
  * This case class is used as an intermediate format for JSON serialization/deserialization of CaseKnowledgeGraph
  * instances. It stores the graph structure as sets of nodes and edge triples.
  *
  * @param nodes
  *   the set of all entities in the graph
  * @param edges
  *   the set of all semantic relationships as (source, link, target) triples
  */
final case class SerializableGraph(nodes: Set[Entity], edges: Set[(Entity, Link, Entity)]) derives ReadWriter

/** Implicit ReadWriter for CaseKnowledgeGraph serialization and deserialization.
  *
  * This given instance enables JSON serialization of CaseKnowledgeGraph instances using upickle. The serialization
  * process converts the graph to a SerializableGraph intermediate format, while deserialization reconstructs the graph
  * by adding nodes and edges to a new instance.
  *
  * This is used for:
  *   - Passing the player's graph as context to the LLM for hint generation
  *   - Parsing the prerequisite graph from the case solution JSON
  */
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
