package model

sealed trait Constraints

object Constraints:
  enum Difficulty:
    case Easy, Medium, Hard

  case class Theme(value: String) extends Constraints
  case class CharactersRange(min: Int, max: Int) extends Constraints
  case class CaseFilesRange(min: Int, max: Int) extends Constraints
  case class PrerequisitesRange(min: Int, max: Int) extends Constraints

object DifficultyPresets:
  import Constraints.{Theme, CharactersRange, CaseFilesRange, PrerequisitesRange}
  def easy(theme: String): Set[Constraints] =
    Set(
      Theme(theme),
      CharactersRange(2, 4),
      CaseFilesRange(2, 5),
      PrerequisitesRange(0, 2)
    )

