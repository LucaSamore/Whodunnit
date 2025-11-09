package model.hint

import model.game.{BaseOrientedGraph, Hint}
import model.generation.Constraint.HintKind.{Helpful, Misleading}
import model.hint.Metric.{coverageAgainst, density}
import model.hint.Trend.{Increasing, Stable, Worsening}

trait HintEngine:
  def evaluate[T](t: List[T])(using Rule[T]): Option[Hint]

object HintEngine extends HintEngine:
  override def evaluate[T](t: List[T])(using rule: Rule[T]): Option[Hint] =
    if rule.condition.eval(t) then Some(rule.hint) else None

object Rules:

  import model.hint.TrendAnalyzers.simpleTrendAnalyzer
  import model.generation.Producers.given

  given stableDensity: Rule[BaseOrientedGraph] = when(density) == Stable hence Hint(Helpful).toOption.get

  def increasingCoverage(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] =
      when(coverageAgainst(solution)) == Increasing hence Hint(Misleading).toOption.get
    rule

  def decreasingCoverage(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] = when(coverageAgainst(solution)) == Worsening hence Hint(Helpful).toOption.get
    rule

  def stableCoverageAndIncreasingDensity(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] =
      when(coverageAgainst(solution)) == Stable and when(density) == Increasing hence Hint(Helpful).toOption.get
    rule

  def increasingCoverageAndIncreasingDensity(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] =
      when(coverageAgainst(solution)) == Increasing and when(density) == Increasing hence Hint(Misleading).toOption.get
    rule
