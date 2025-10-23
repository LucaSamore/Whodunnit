package model.case_generation

sealed trait Constraint

object Constraint:
  enum Difficulty:
    case Easy, Medium, Hard

  case class Theme(value: String) extends Constraint
  case class CharactersRange(min: Int, max: Int) extends Constraint
  case class CaseFilesRange(min: Int, max: Int) extends Constraint
  case class PrerequisitesRange(min: Int, max: Int) extends Constraint

  extension (c: Constraint)
    def toPromptDescription: String = c match
      case Theme(value) =>
        s"Theme: $value"
      case CharactersRange(min, max) =>
        s"Number of characters: between $min and $max"
      case CaseFilesRange(min, max) =>
        s"Number of case files: between $min and $max"
      case PrerequisitesRange(min, max) =>
        s"Solution prerequisites: between $min and $max"

object DifficultyPresets:
  import Constraint.Difficulty.{Easy, Hard, Medium}
  import Constraint.*

  def fromDifficulty(difficulty: Difficulty, theme: String): Set[Constraint] =
    difficulty match
      case Easy   => easy(theme)
      case Medium => medium(theme)
      case Hard   => hard(theme)

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
