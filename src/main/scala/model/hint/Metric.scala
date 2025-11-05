package model.hint

import model.knowledgegraph.BaseOrientedGraph

object Metric:
  type MetricValue = Double

  extension (bog: BaseOrientedGraph)
    def density: MetricValue =
      // TODO: specify in the doc that if the graph is empty, we say the density is conventionally zero
      val edgesCardinality = bog.edges.size
      val nodesCardinality = bog.nodes.size
      if (nodesCardinality > 0) then
        edgesCardinality.toDouble / nodesCardinality * (nodesCardinality - 1)
      else 0.0
