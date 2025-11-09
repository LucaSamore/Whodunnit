package model.hint

import model.hint.Metric.MetricValue
import model.game.Hint

/** A composable condition that evaluates a predicate over a list of objects.
  *
  * MetricCheck forms the core of the hint DSL, allowing conditions to be combined with logical operators and converted
  * into rules.
  *
  * @tparam T
  *   the type of objects in the list being evaluated
  * @param eval
  *   a predicate function that evaluates the entire list
  * @example
  *   {{{
  *   val check = when(coverage) == Increasing and when(density) == Stable
  *   val matches = check.eval(history) // true or false
  *   }}}
  */
final case class MetricCheck[T](eval: List[T] => Boolean):
  /** Combines this check with another using logical AND.
    *
    * @param other
    *   the check to combine with
    * @return
    *   a new check that passes only if both checks pass
    */
  infix def and(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(obj => eval(obj) && other.eval(obj))

  /** Combines this check with another using logical OR.
    *
    * @param other
    *   the check to combine with
    * @return
    *   a new check that passes if either check passes
    */
  infix def or(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(obj => eval(obj) || other.eval(obj))

  /** Creates a rule by associating this condition with a hint.
    *
    * @param hint
    *   the hint to produce when this condition is satisfied
    * @return
    *   a rule pairing this condition with the given hint
    */
  infix def hence(hint: Hint): Rule[T] = Rule[T](this, hint)

/** A rule that produces a hint when its condition is satisfied.
  *
  * Rules are the output of the DSL and can be evaluated against a history to determine if they apply.
  *
  * @tparam T
  *   the type of objects in the history
  * @param condition
  *   the condition that must be met
  * @param hint
  *   the hint to produce when the condition is satisfied
  * @example
  *   {{{
  *   val rule = when(coverage) == Increasing hence Hint("Coverage is improving")
  *   }}}
  */
case class Rule[T](condition: MetricCheck[T], hint: Hint)

/** An expression representing a metric that can be checked against a trend.
  *
  * MetricExpr is an intermediate type in the DSL that bridges metrics and trends. It is created by the `when` function
  * and converted to a MetricCheck via the `==` operator.
  *
  * @tparam T
  *   the type of objects the metric operates on
  * @param compute
  *   a function that extracts a metric value from an object
  * @example
  *   {{{
  *   val expr = when(density)
  *   val check = expr == Increasing
  *   }}}
  */
final case class MetricExpr[T](compute: T => MetricValue):
  /** Creates a check that evaluates whether the metric follows the given trend.
    *
    * The metric is computed for each object in the history, and the resulting values are analyzed by the TrendAnalyzer
    * to determine if they match the trend.
    *
    * @param trend
    *   the expected trend (Increasing, Stable, or Worsening)
    * @param analyzer
    *   the trend analyzer to use (implicit)
    * @return
    *   a check that evaluates the trend of this metric
    */
  infix def ==(trend: Trend)(using analyzer: TrendAnalyzer): MetricCheck[T] =
    MetricCheck[T] { objs =>
      val values = objs.map(compute)
      analyzer.analyze(values) == trend
    }

/** DSL entry point that creates a metric expression from a metric function.
  *
  * This is the starting point for building metric checks in the DSL. It allows writing natural expressions like
  * `when(coverage) == Increasing`.
  *
  * @tparam T
  *   the type of objects the metric operates on
  * @param metric
  *   a function that computes a metric value from an object
  * @return
  *   a metric expression that can be checked against trends
  * @example
  *   {{{
  *   // Simple check
  *   val check = when(coverage) == Increasing
  *
  *   // Combined checks
  *   val rule =
  *     when(coverage) == Increasing and
  *     when(density) == Stable hence
  *     Hint("Good progress with stable density")
  *   }}}
  */
def when[T](metric: T => MetricValue): MetricExpr[T] = MetricExpr[T](metric)
