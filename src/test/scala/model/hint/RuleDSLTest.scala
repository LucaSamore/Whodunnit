package model.hint

import model.hint.HintKind.Misleading
import model.hint.Metric.{coverageFor, density}
import model.hint.Trend.{Increasing, Worsening}
import model.hint.TrendAnalyzers.simpleTrendAnalyzer
import model.knowledgegraph.BaseOrientedGraph
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

final class RuleDSLTest extends AnyFlatSpec with Matchers with GivenWhenThen:

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
      graph.withNodes(1, 2).withEdge(1, "link1", 2),
      graph.withNodes(1, 2),
      graph.withNodes(1, 2, 3)
    )
    val coverage = coverageFor(reference)

    When("checking both conditions with 'and'")
    val check = when(coverage) == Increasing and when(density) == Worsening

    Then("the combined check should evaluate to true")
    check.eval(history) shouldBe true

  it should "work with 'or' operator" in:
    Given("a history with increasing coverage and stable density")
    val reference = graph.withNodes(1, 2, 3).withEdge(1, "link1", 2)
    val history = List(
      graph.withNodes(1),
      graph.withNodes(1, 2),
      graph.withNodes(1, 2, 3)
    )
    val coverage = coverageFor(reference)

    When("checking with 'or' where one condition is true")
    val check = when(coverage) == Increasing or when(density) == Worsening

    Then("the check should evaluate to true")
    check.eval(history) shouldBe true

  "RuleCreation" should "create a rule with hence operator" in:
    Given("a metrick check")
    val check = when(density) == Increasing

    When("creating a rule with 'hence'")
    val rule = check hence Hint(Misleading)

    Then("the rule should have the correct hint")
    rule.hint shouldBe Hint(Misleading)
    rule.condition shouldBe check
