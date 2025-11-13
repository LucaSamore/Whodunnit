package model.generation

import model.generation.Difficulty.{Easy, Hard, Medium}

sealed trait Constraint

enum Difficulty(val difficulty: String) extends Constraint:
  case Easy extends Difficulty("Easy")
  case Medium extends Difficulty("Medium")
  case Hard extends Difficulty("Hard")

enum HintKind extends Constraint:
  case Helpful
  case Misleading

final case class Theme(value: String) extends Constraint

final case class CharactersRange(min: Int, max: Int) extends Constraint

final case class CaseFilesRange(min: Int, max: Int) extends Constraint

final case class PrerequisitesRange(min: Int, max: Int) extends Constraint

final case class Context(content: String) extends Constraint

object Constraint:
  extension (c: Constraint)
    def toPromptDescription: String = c match
      case Theme(value)                 => s"Theme: $value"
      case CharactersRange(min, max)    => s"Number of characters: between $min and $max"
      case CaseFilesRange(min, max)     => s"Number of case files: between $min and $max"
      case PrerequisitesRange(min, max) => s"Solution prerequisites: between $min and $max"
      case HintKind.Helpful             => s"The hint to be generated must be ${HintKind.Helpful.toString}"
      case HintKind.Misleading          => s"The hint to be generated must be ${HintKind.Misleading.toString}"
      case Context(content)             => s"Additional context:\n\n $content"
      case difficulty: Difficulty       => formatDifficultyConstraints(difficulty)

  private def formatDifficultyConstraints(difficulty: Difficulty): String =
    val constraints = difficulty match
      case Easy   => DifficultyPresets.easy
      case Medium => DifficultyPresets.medium
      case Hard   => DifficultyPresets.hard
    val descriptions = constraints.map(_.toPromptDescription).mkString("\n")
    s"Difficulty: ${difficulty.difficulty}\n$descriptions"

object DifficultyPresets:

  val easy: Set[Constraint] =
    Set(
      CharactersRange(2, 4),
      CaseFilesRange(2, 5),
      PrerequisitesRange(1, 2)
    )

  val medium: Set[Constraint] =
    Set(
      CharactersRange(3, 5),
      CaseFilesRange(4, 8),
      PrerequisitesRange(1, 3)
    )

  val hard: Set[Constraint] =
    Set(
      CharactersRange(4, 6),
      CaseFilesRange(7, 10),
      PrerequisitesRange(2, 5)
    )
