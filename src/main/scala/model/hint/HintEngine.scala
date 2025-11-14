package model.hint

import model.game.BaseOrientedGraph
import model.generation.HintKind
import model.generation.HintKind.{Helpful, Misleading}
import model.hint.Metric.{coverageAgainst, density}
import model.hint.Trend.{Increasing, Stable, Worsening}

trait HintEngine:
  def evaluate[T](t: List[T])(using Rule[T]): Option[HintKind]

object HintEngine extends HintEngine:
  override def evaluate[T](t: List[T])(using rule: Rule[T]): Option[HintKind] =
    if rule.condition.eval(t) then Some(rule.hint) else None

object Rules:

  import model.hint.TrendAnalyzers.simpleTrendAnalyzer
  import model.generation.Producers.given

  given stableDensity: Rule[BaseOrientedGraph] = when(density) == Stable hence Helpful

  def increasingCoverage(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] = when(coverageAgainst(solution)) == Increasing hence Misleading
    rule
