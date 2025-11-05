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
