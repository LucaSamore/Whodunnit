package model.game

object ValidationResult:
  def validateSolution(
      accusedCharacter: Character,
      solution: Solution
  ): ValidationResult =
    if accusedCharacter == solution.culprit then
      CorrectSolution(solution.culprit, solution.motive)
    else
      IncorrectSolution(accusedCharacter, solution.culprit, solution.motive)

enum SubmissionState:
  case NotSubmitted
  case Submitting(character: Character)
  case Submitted(result: ValidationResult)

enum ValidationResult:
  case PrerequisitesNotMet

  case CorrectSolution(culprit: Character, motive: String)

  case IncorrectSolution(
      accusedCharacter: Character,
      actualCulprit: Character,
      motive: String
  )

object SolutionConfig:
  val PrerequisiteCoverageThreshold: Double = 1.0
  val TimeElapsedThreshold: Double = 0.85
