package model.hint

import model.hint.Metric.MetricValue

// TODO: move Hint to a more appropriate file
case class Hint(description: String)

final case class MetricCheck[T](eval: List[T] => Boolean):
  infix def and(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(history => eval(history) && other.eval(history))

  infix def or(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(history => eval(history) || other.eval(history))

  infix def soThen(hint: Hint): Rule[T] = Rule[T](this, hint)

final case class Rule[T](condition: MetricCheck[T], hint: Hint)

final case class MetricExpr[T](compute: T => MetricValue):
  infix def is(trend: Trend)(using analyzer: TrendAnalyzer): MetricCheck[T] =
    MetricCheck[T] { history =>
      val values = history.map(compute)
      analyzer.analyze(values) == trend
    }

def when[T](metric: T => MetricValue): MetricExpr[T] = MetricExpr[T](metric)
