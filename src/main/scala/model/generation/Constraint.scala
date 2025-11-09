package model.generation

sealed trait Constraint

object Constraint:
  enum Difficulty extends Constraint:
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
      case _: Difficulty => ""

  def expandConstraints(constraints: Seq[Constraint]): Seq[Constraint] =
    val (difficulties, others) = constraints.partition {
      case _: Difficulty => true
      case _             => false
    }

    val expandedFromDifficulty = difficulties.headOption match
      case Some(Difficulty.Easy) =>
        DifficultyPresets.easy()
      case Some(Difficulty.Medium) =>
        DifficultyPresets.medium()
      case Some(Difficulty.Hard) =>
        DifficultyPresets.hard()
      case _ =>
        Set.empty[Constraint]

    val explicitConstraints = others.toSet
    val uniqueDerivedConstraints = expandedFromDifficulty.filterNot { dc =>
      explicitConstraints.exists(_.getClass == dc.getClass)
    }
    (explicitConstraints ++ uniqueDerivedConstraints).toSeq

object DifficultyPresets:
  import Constraint.*

  def easy(): Set[Constraint] =
    Set(
      CharactersRange(2, 4),
      CaseFilesRange(2, 5),
      PrerequisitesRange(1, 2)
    )

  def medium(): Set[Constraint] =
    Set(
      CharactersRange(3, 5),
      CaseFilesRange(4, 8),
      PrerequisitesRange(1, 3)
    )

  def hard(): Set[Constraint] =
    Set(
      CharactersRange(4, 6),
      CaseFilesRange(7, 10),
      PrerequisitesRange(2, 5)
    )
