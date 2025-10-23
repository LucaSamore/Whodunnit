package model

sealed trait Constraints

object Constraints:
  case class Theme(value: String) extends Constraints
  case class CharactersRange(min: Int, max: Int) extends Constraints
  case class CaseFilesRange(min: Int, max: Int) extends Constraints
  case class PrerequisitesRange(min: Int, max: Int) extends Constraints


