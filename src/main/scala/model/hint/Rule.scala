package model.hint

import model.hint.Metric.MetricValue

// TODO: move Hint to a more appropriate file
case class Hint(kind: HintKind)

enum HintKind:
  case Helpful
  case Misleading

// T is a game obj (e.g., case KG)

final case class MetricCheck[T](eval: List[T] => Boolean):
  infix def and(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(history => eval(history) && other.eval(history))

  infix def or(other: MetricCheck[T]): MetricCheck[T] =
    MetricCheck(history => eval(history) || other.eval(history))

  infix def hence(hint: Hint): Rule[T] = Rule[T](this, hint)

final case class Rule[T](condition: MetricCheck[T], hint: Hint)

final case class MetricExpr[T](compute: T => MetricValue):
  infix def ==(trend: Trend)(using analyzer: TrendAnalyzer): MetricCheck[T] =
    MetricCheck[T] { history =>
      val values = history.map(compute)
      analyzer.analyze(values) == trend
    }

def when[T](metric: T => MetricValue): MetricExpr[T] = MetricExpr[T](metric)
