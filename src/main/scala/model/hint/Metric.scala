package model.hint

import model.game.BaseOrientedGraph

/** Provides metric functions for analyzing graph structures in the investigation.
  *
  * This object defines extension methods for [[BaseOrientedGraph]] that compute various metrics used by the hint system
  * to analyze player progress. Metrics are computed as [[MetricValue]] (Double) to allow trend analysis over time.
  *
  * @see
  *   [[model.hint.Trend]] for trend classification
  * @see
  *   [[model.hint.TrendAnalyzer]] for analyzing metric sequences
  */
object Metric:
  /** Type alias for metric values, represented as Double precision floating point numbers. */
  type MetricValue = Double

  extension [G <: BaseOrientedGraph](graph: G)
    /** Computes the density of the graph.
      *
      * Density measures how many edges exist relative to the maximum possible number of edges in the graph. It is
      * calculated as the ratio of actual edges to the product of nodes and (nodes - 1).
      *
      * For empty graphs (no nodes), density is conventionally defined as 0.0.
      *
      * @return
      *   the density value between 0.0 and 1.0, where 0.0 indicates no edges and higher values indicate more
      *   connections
      * @example
      *   {{{
      * val graph = new CaseKnowledgeGraph()
      *   .withNodes(1, 2, 3)
      *   .withEdge(1, Link("knows"), 2)
      * val d = graph.density // Returns edges / (nodes * (nodes - 1))
      *   }}}
      */
    def density: MetricValue =
      val edgesCardinality = graph.edges.size
      val nodesCardinality = graph.nodes.size
      if (nodesCardinality > 0) then
        edgesCardinality.toDouble / nodesCardinality * (nodesCardinality - 1)
      else 0.0

    /** Computes the coverage of this graph relative to another reference graph.
      *
      * Coverage measures how much of the reference graph's structure (nodes and edges) is present in this graph. It is
      * calculated as the average of node coverage and edge coverage, where each is the ratio of intersecting elements
      * to reference elements.
      *
      * Special cases:
      *   - If the reference graph is empty, coverage is 1.0 (complete coverage by convention)
      *   - If this graph is empty but reference is not, coverage is 0.0 (no coverage)
      *   - For empty source sets within a ratio calculation, the ratio is 1.0 (complete coverage by convention)
      *
      * @param other
      *   the reference graph to compare against
      * @return
      *   the coverage value between 0.0 and 1.0, where 1.0 indicates complete coverage
      * @example
      *   {{{
      * val solution = solutionGraph.withNodes(1, 2).withEdge(1, "link", 2)
      * val player = playerGraph.withNodes(1, 2)
      * val cov = player.coverage(solution) // Returns 0.5 (nodes match, edges don't)
      *   }}}
      */
    def coverage(other: G): MetricValue =
      def ratio[A](source: Set[A], target: Set[A]): MetricValue =
        if source.isEmpty then 1.0
        else source.intersect(target).size.toDouble / source.size

      (other.isEmpty, graph.isEmpty) match
        case (true, _)     => 1.0
        case (false, true) => 0.0
        case _             =>
          (ratio(
            other.nodes.asInstanceOf[Set[graph.Node]],
            graph.nodes
          ) + ratio(
            other.edges.asInstanceOf[Set[(graph.Node, graph.Edge, graph.Node)]],
            graph.edges
          )) / 2.0

  /** Creates a metric function that computes coverage against a fixed reference graph.
    *
    * This is a convenience function for creating reusable metrics in the hint DSL. It returns a function that takes a
    * graph and computes its coverage relative to the provided reference graph.
    *
    * @tparam G
    *   the type of graph (must extend BaseOrientedGraph)
    * @param refGraph
    *   the reference graph to compute coverage against
    * @return
    *   a function from graph to metric value representing coverage
    * @example
    *   {{{
    * val solutionGraph = case.solution.prerequisite
    * val coverageMetric = coverageAgainst(solutionGraph)
    *
    * // Use in DSL
    * val rule = when(coverageMetric) == Increasing hence Misleading
    *   }}}
    */
  def coverageAgainst[G <: BaseOrientedGraph](refGraph: G): G => MetricValue =
    (graph: G) => graph.coverage(refGraph)
