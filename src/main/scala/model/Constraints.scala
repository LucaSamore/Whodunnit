package model

sealed trait Constraints

object Constraints:
  case class Theme(value: String) extends Constraints