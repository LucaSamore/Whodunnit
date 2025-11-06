package model.hint

import model.hint.Trend.Increasing
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import model.hint.TrendAnalyzers.advancedTrendAnalyzer

class TrendAnalyzerTest extends AnyFlatSpec with Matchers with GivenWhenThen:

  "TrendAnalyzer" should "detect increasing trend" in:
    Given("values with clearly increasing trend")
    val values = List(0.1, 0.2, 0.3, 0.4, 0.5)

    When("analyzing the trend")
    val trend = summon[TrendAnalyzer].analyze(values)

    Then("it should detect increasing")
    trend shouldBe Increasing
