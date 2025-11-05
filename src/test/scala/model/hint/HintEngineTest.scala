package model.hint

import model.hint.Metric.{coverageFor, density}
import model.hint.Trend.{Increasing, Stable, Worsening}
import model.hint.TrendAnalyzers.simpleTrendAnalyzer
import model.knowledgegraph.BaseOrientedGraph
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class HintEngineTest extends AnyFlatSpec with Matchers with GivenWhenThen:

  private def graph: BaseOrientedGraph { type Node = Int; type Edge = String } =
    new BaseOrientedGraph:
      override type Node = Int
      override type Edge = String

  "MetricCheck" should "evaluate to true when condition matches" in:
    Given("a history of graphs with incrementing density")
    val history = List(
      graph.withNodes(1, 2, 3),
      graph.withNodes(1, 2, 3).withEdge(1, "link1", 2),
      graph.withNodes(1, 2, 3).withEdge(1, "link1", 2).withEdge(2, "link2", 3)
    )

    When("checking if density is increasing")
    val check = when(density) == Increasing

    Then("the check should evaluate to true")
    check.eval(history) shouldBe true

  it should "evaluate to false when condition does not match" in:
    Given("a history of graphs with incrementing density")
    val history = List(graph.withNodes(1, 2, 3))

    When("checking if density is increasing")
    val check = when(density) == Increasing

    Then("the check should evaluate to false")
    check.eval(history) shouldBe false

  "MetricCheck combinators" should "work with 'and' operator" in:
    Given("a history with increasing coverage and worsening density")
    val reference = graph.withNodes(1, 2, 3)
    val history = List(
      graph.withNodes(1, 2, 3).withEdge(1, "link1", 2).withEdge(2, "link2", 3),
      graph.withNodes(1, 2, 3).withEdge(1, "link1", 2),
      graph.withNodes(1, 2, 3)
    )

    When("checking both conditions with 'and'")
    val coverage = coverageFor(reference)
    val check = when(coverage) == Increasing and when(density) == Worsening

    Then("the combined check should evaluate to true")
    check.eval(history) shouldBe true
