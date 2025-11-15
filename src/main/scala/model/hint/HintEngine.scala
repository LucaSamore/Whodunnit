package model.hint

import model.game.BaseOrientedGraph
import model.generation.HintKind
import model.generation.HintKind.{Helpful, Misleading}
import model.hint.Metric.{coverageAgainst, density}
import model.hint.Trend.{Increasing, Stable, Worsening}

/** Engine for evaluating hint generation rules against player history.
  *
  * Analyzes a sequence of historical states using predefined rules to determine if a hint should be generated. Rules
  * are defined using the hint DSL and can analyze metrics like graph density, coverage, and their trends.
  *
  * @example
  *   {{{
  * val history: List[CaseKnowledgeGraph] = List(state1, state2, state3)
  * given rule: Rule[CaseKnowledgeGraph] = when(density) == Stable hence Helpful
  *
  * HintEngine.evaluate(history) // Returns Some(Helpful) if density is stable
  *   }}}
  */
trait HintEngine:
  /** Evaluates a rule against historical states.
    *
    * @param t
    *   the list of historical states to analyze
    * @param rule
    *   the rule to evaluate (implicit)
    * @return
    *   Some(HintKind) if the rule's condition is met, None otherwise
    */
  def evaluate[T](t: List[T])(using Rule[T]): Option[HintKind]

/** Default HintEngine implementation. */
object HintEngine extends HintEngine:
  override def evaluate[T](t: List[T])(using rule: Rule[T]): Option[HintKind] =
    if rule.condition.eval(t) then Some(rule.hint) else None

/** Predefined rules for common hint generation scenarios.
  *
  * Contains ready-to-use rules that analyze player behavior patterns to determine appropriate hint moments.
  */
object Rules:

  import model.hint.TrendAnalyzers.simpleTrendAnalyzer
  import model.generation.Producers.given

  /** Generates a Helpful hint when the player's graph density remains stable.
    *
    * Useful when the player is refining existing connections rather than exploring new avenues.
    */
  given stableDensity: Rule[BaseOrientedGraph] = when(density) == Stable hence Helpful

  /** Creates a rule that generates a Misleading hint when coverage of the solution is increasing.
    *
    * Used to add challenge when the player is making good progress toward the correct answer.
    *
    * @param solution
    *   the solution graph used as reference for coverage calculation
    * @return
    *   a rule that evaluates to Misleading when coverage increases
    */
  def increasingCoverage(solution: BaseOrientedGraph): Rule[BaseOrientedGraph] =
    given rule: Rule[BaseOrientedGraph] = when(coverageAgainst(solution)) == Increasing hence Misleading
    rule
