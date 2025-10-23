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
      PrerequisitesRange(1, 2)
    )

  def medium(theme: String): Set[Constraints] =
    Set(
      Theme(theme),
      CharactersRange(3, 5),
      CaseFilesRange(4, 8),
      PrerequisitesRange(1, 3)
    )

  def hard(theme: String): Set[Constraints] =
    Set(
      Theme(theme),
      CharactersRange(4, 6),
      CaseFilesRange(7, 10),
      PrerequisitesRange(2, 5)
    )

