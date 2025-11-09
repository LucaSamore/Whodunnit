package model.game

import model.generation.{Constraint, Producer, ProductionError}

trait Hint:
  def description: String

object Hint:
  def apply(constraints: Constraint*)(using producer: Producer[Hint]): Either[ProductionError, Hint] =
    producer.produce(constraints*)

final case class HintImpl(override val description: String) extends Hint
