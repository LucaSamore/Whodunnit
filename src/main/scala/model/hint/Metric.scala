package model.hint

import model.knowledgegraph.BaseOrientedGraph

object Metric:
  type MetricValue = Double

  extension [G <: BaseOrientedGraph](graph: G)
    def density: MetricValue =
      // TODO: specify in the doc that if the graph is empty, we say the density is conventionally zero
      val edgesCardinality = graph.edges.size
      val nodesCardinality = graph.nodes.size
      if (nodesCardinality > 0) then
        edgesCardinality.toDouble / nodesCardinality * (nodesCardinality - 1)
      else 0.0

    private def coverage(other: G): MetricValue =
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

  def coverageFor[G <: BaseOrientedGraph](refGraph: G): G => MetricValue =
    (graph: G) => graph.coverage(refGraph)
