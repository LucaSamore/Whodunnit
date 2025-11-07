package model.hint

import model.game.Hint

enum HintKind:
  case Helpful
  case Misleading

trait HintEngine:
  def evaluate[T](t: List[T])(using Rule[T]): Option[Hint]

object HintEngine extends HintEngine:
  override def evaluate[T](t: List[T])(using rule: Rule[T]): Option[Hint] =
    if rule.condition.eval(t) then Some(rule.hint) else None
