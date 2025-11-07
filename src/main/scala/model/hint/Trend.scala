package model.hint

import model.hint.Metric.MetricValue

enum Trend:
  case Increasing
  case Stable
  case Worsening

trait TrendAnalyzer:
  def analyze(values: List[MetricValue]): Trend

object TrendAnalyzers:
  given simpleTrendAnalyzer: TrendAnalyzer with
    override def analyze(values: List[MetricValue]): Trend =
      (values.headOption, values.lastOption) match
        case (Some(first), Some(last)) if values.size > 1 =>
          Math.signum(last - first) match
            case 1.0  => Trend.Increasing
            case -1.0 => Trend.Worsening
            case _    => Trend.Stable
        case _ => Trend.Stable

  given advancedTrendAnalyzer: TrendAnalyzer with

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
        if ratio >= threshold then Trend.Increasing
        else if ratio <= 1 - threshold then Trend.Worsening
        else Trend.Stable
