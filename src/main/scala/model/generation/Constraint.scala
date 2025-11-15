package model.generation

import model.generation.Difficulty.{Easy, Hard, Medium}

/** Base trait for all constraint types used in case generation.
  *
  * Constraints define parameters that guide the generating cases, hints, or other game content. They can specify
  * difficulty levels, theme, or additional context.
  */
sealed trait Constraint

/** Predefined difficulty levels affecting case complexity.
  *
  * Each difficulty level maps to a preset combination of constraints that control the number of characters, case files,
  * and solution prerequisites.
  *
  * @param difficulty
  *   the human-readable difficulty name
  */
enum Difficulty(val difficulty: String) extends Constraint:
  case Easy extends Difficulty("Easy")
  case Medium extends Difficulty("Medium")
  case Hard extends Difficulty("Hard")

/** Specifies whether a hint should help or mislead the player. */
enum HintKind extends Constraint:
  case Helpful
  case Misleading

/** Specifies a thematic setting for case generation.
  *
  * @param value
  *   the theme description (e.g., "Hacking", "Murder")
  */
final case class Theme(value: String) extends Constraint

/** Constrains the number of characters in a case.
  *
  * @param min
  *   minimum number of characters
  * @param max
  *   maximum number of characters
  */
final case class CharactersRange(min: Int, max: Int) extends Constraint

/** Constrains the number of case files (evidence documents) in a case.
  *
  * @param min
  *   minimum number of case files
  * @param max
  *   maximum number of case files
  */
final case class CaseFilesRange(min: Int, max: Int) extends Constraint

/** Constrains the complexity of the solution knowledge graph.
  *
  * Prerequisites are the minimum number of relationships the player must discover to solve the case.
  *
  * @param min
  *   minimum number of solution prerequisites
  * @param max
  *   maximum number of solution prerequisites
  */
final case class PrerequisitesRange(min: Int, max: Int) extends Constraint

/** Provides additional context or instructions for content generation.
  *
  * @param content
  *   free-form text describing additional requirements or context
  */
final case class Context(content: String) extends Constraint

/** Companion object providing utility functions for constraints. */
object Constraint:
  extension (c: Constraint)
    /** Converts a constraint to its prompt representation.
      *
      * @return
      *   a formatted string describing the constraint
      */
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

/** Predefined constraint combinations for each difficulty level. */
object DifficultyPresets:

  /** Easy difficulty: fewer characters, files, and simpler solutions. */
  val easy: Set[Constraint] =
    Set(
      CharactersRange(2, 4),
      CaseFilesRange(2, 5),
      PrerequisitesRange(1, 2)
    )

  /** Medium difficulty: moderate complexity across all dimensions. */
  val medium: Set[Constraint] =
    Set(
      CharactersRange(3, 5),
      CaseFilesRange(4, 8),
      PrerequisitesRange(1, 3)
    )

  /** Hard difficulty: more characters, files, and complex solution graphs. */
  val hard: Set[Constraint] =
    Set(
      CharactersRange(4, 6),
      CaseFilesRange(7, 10),
      PrerequisitesRange(2, 5)
    )
