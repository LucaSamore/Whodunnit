package model.hint

import model.hint.Metric.MetricValue

/** Represents the trend of a metric over time in the player's investigation history.
  *
  * Trends are computed by [[TrendAnalyzer]] instances that analyze sequences of metric values to determine if the
  * metric is improving, declining, or remaining constant.
  *
  * @see
  *   [[TrendAnalyzer]] for trend computation strategies
  * @see
  *   [[model.hint.Metric]] for available metrics
  */
enum Trend:
  /** The metric values are consistently increasing over time, indicating improvement. */
  case Increasing

  /** The metric values remain relatively constant over time, showing no significant change. */
  case Stable

  /** The metric values are consistently decreasing over time, indicating decline. */
  case Worsening

/** Analyzes sequences of metric values to determine their trend.
  *
  * A TrendAnalyzer examines a list of Double values (typically representing a metric computed at different points in
  * the player's history) and classifies the overall pattern as [[Trend.Increasing]], [[Trend.Worsening]], or
  * [[Trend.Stable]].
  *
  * Different implementations can use varying strategies for trend detection, from simple first-to-last comparison to
  * more sophisticated sliding window analysis.
  *
  * @see
  *   [[TrendAnalyzers]] for concrete implementations
  */
trait TrendAnalyzer:
  /** Analyzes a sequence of metric values and determines the overall trend.
    *
    * @param values
    *   a list of metric values ordered chronologically (typically from oldest to newest)
    * @return
    *   the detected trend: [[Trend.Increasing]], [[Trend.Stable]], or [[Trend.Worsening]]
    */
  def analyze(values: List[MetricValue]): Trend

/** Provides concrete implementations of [[TrendAnalyzer]] with different analysis strategies.
  *
  * This object contains given instances that can be used implicitly in contexts requiring trend analysis. Each analyzer
  * uses a different algorithm to detect trends in metric sequences.
  */
object TrendAnalyzers:

  /** A simple trend analyzer that compares the first and last values in the sequence.
    *
    * This analyzer uses a straightforward approach: it takes the difference between the last and first values and uses
    * the sign of that difference to determine the trend. If the values increase, the trend is [[Trend.Increasing]]; if
    * they decrease, it's [[Trend.Worsening]]; otherwise, it's [[Trend.Stable]].
    *
    * This implementation returns [[Trend.Stable]] for sequences with fewer than 2 elements.
    *
    * @example
    *   {{{
    * given TrendAnalyzer = simpleTrendAnalyzer
    * val values = List(0.1, 0.3, 0.2, 0.5) // Overall increasing
    * val trend = summon[TrendAnalyzer].analyze(values) // Returns Trend.Increasing
    *   }}}
    */
  given simpleTrendAnalyzer: TrendAnalyzer with
    override def analyze(values: List[MetricValue]): Trend =
      (values.headOption, values.lastOption) match
        case (Some(first), Some(last)) if values.size > 1 =>
          Math.signum(last - first) match
            case 1.0  => { println("[Model] Trend is Increasing"); Trend.Increasing }
            case -1.0 => { println("[Model] Trend is Worsening"); Trend.Worsening }
            case _    => { println("[Model] Trend is Stable"); Trend.Stable }
        case _ => { println("[Model] Trend is Stable"); Trend.Stable }

  /** An advanced trend analyzer that uses sliding window analysis with a configurable threshold.
    *
    * This analyzer examines consecutive pairs of values using a sliding window approach. It counts how many transitions
    * are increasing versus total transitions, then compares this ratio against a threshold to determine the overall
    * trend.
    *
    * The threshold (default 0.6 or 60%) determines the sensitivity: a ratio above the threshold indicates
    * [[Trend.Increasing]], below (1 - threshold) indicates [[Trend.Worsening]], and values in between are considered
    * [[Trend.Stable]].
    *
    * This implementation provides more robust trend detection for noisy data and returns [[Trend.Stable]] for sequences
    * with fewer than 2 elements.
    *
    * @example
    *   {{{
    * given TrendAnalyzer = advancedTrendAnalyzer
    * val values = List(0.1, 0.15, 0.14, 0.2, 0.25) // Mostly increasing
    * val trend = summon[TrendAnalyzer].analyze(values) // Returns Trend.Increasing
    *   }}}
    */
  given advancedTrendAnalyzer: TrendAnalyzer with

    /** The threshold ratio for determining trends (default: 0.6).
      *
      * A ratio of increasing transitions above this threshold results in [[Trend.Increasing]]. A ratio below (1 -
      * threshold) results in [[Trend.Worsening]]. Ratios between these bounds result in [[Trend.Stable]].
      */
    private val threshold = 0.6

    override def analyze(values: List[MetricValue]): Trend =
      if values.length < 2 then Trend.Stable
      else
        val (increasing, total) = values.sliding(2).foldLeft((0, 0)) {
          case ((inc, tot), List(prev, curr)) =>
            val direction = (curr - prev).sign.toInt
            (inc + (if direction > 0 then 1 else 0), tot + 1)
          case (acc, _) => acc
        }
        val ratio = increasing.toDouble / total
        if ratio >= threshold then { println("[Model] Trend is Increasing"); Trend.Increasing }
        else if ratio <= 1 - threshold then { println("[Model] Trend is Worsening"); Trend.Worsening }
        else { println("[Model] Trend is Stable"); Trend.Stable }
