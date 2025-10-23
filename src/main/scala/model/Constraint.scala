package model

sealed trait Constraint

object Constraint:
  enum Difficulty:
    case Easy, Medium, Hard

  case class Theme(value: String) extends Constraint
  case class CharactersRange(min: Int, max: Int) extends Constraint
  case class CaseFilesRange(min: Int, max: Int) extends Constraint
  case class PrerequisitesRange(min: Int, max: Int) extends Constraint

object DifficultyPresets:
  import Constraint.{
    CaseFilesRange,
    CharactersRange,
    PrerequisitesRange,
    Theme
  }

  def easy(theme: String): Set[Constraint] =
    Set(
      Theme(theme),
      CharactersRange(2, 4),
      CaseFilesRange(2, 5),
      PrerequisitesRange(1, 2)
    )

  def medium(theme: String): Set[Constraint] =
    Set(
      Theme(theme),
      CharactersRange(3, 5),
      CaseFilesRange(4, 8),
      PrerequisitesRange(1, 3)
    )

  def hard(theme: String): Set[Constraint] =
    Set(
      Theme(theme),
      CharactersRange(4, 6),
      CaseFilesRange(7, 10),
      PrerequisitesRange(2, 5)
    )
