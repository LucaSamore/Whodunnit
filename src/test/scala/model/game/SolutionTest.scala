package model.game

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import model.hint.Metric
import model.hint.Metric.coverage

import scala.concurrent.duration.DurationInt

class SolutionTest extends AnyWordSpec with Matchers:

  private val witness = Character("Witness Brown", CaseRole.Witness)
  private val innocentSuspect = Character("Alice Johnson", CaseRole.Suspect)
  private val guilty = Character("Robert Martinez", CaseRole.Suspect)
  private val victim = Character("Charlie Davis", CaseRole.Victim)

  private val actualMotive = "Financial gain from inheritance dispute"

  private val solution = Solution(
    new CaseKnowledgeGraph()
      .withNodes(guilty, innocentSuspect, victim, witness)
      .withEdge(guilty, Link("had conflict with"), victim)
      .withEdge(guilty, Link("benefited from death of"), victim),
    guilty,
    actualMotive
  )

  private val prerequisite = new CaseKnowledgeGraph()
    .withNodes(guilty, victim)
    .withEdge(guilty, Link("killed"), victim)

  private val emptyGraph = new CaseKnowledgeGraph()

  private val partialGraph = new CaseKnowledgeGraph()
    .withNodes(guilty, victim) // only nodes, no edges

  private val fullGraph = new CaseKnowledgeGraph()
    .withNodes(guilty, victim)
    .withEdge(guilty, Link("killed"), victim)

  "ValidationResult.validateSolution" should:
    "return CorrectSolution" when:
      "player accuses the right culprit" in:
        val result = ValidationResult.validateSolution(guilty, solution)

        result shouldBe a[ValidationResult.CorrectSolution]
        result match
          case ValidationResult.CorrectSolution(culprit, motive) =>
            culprit shouldBe guilty
            culprit.name shouldBe "Robert Martinez"
            motive shouldBe actualMotive
          case _ => fail("Expected CorrectSolution when accusing the guilty character")

    "return IncorrectSolution" when:
      "player accuses an innocent suspect" in:
        val result = ValidationResult.validateSolution(innocentSuspect, solution)

        result shouldBe a[ValidationResult.IncorrectSolution]
        result match
          case ValidationResult.IncorrectSolution(accused, actualCulprit, motive) =>
            accused shouldBe innocentSuspect
            accused.name shouldBe "Alice Johnson"
            actualCulprit shouldBe guilty
            actualCulprit.name shouldBe "Robert Martinez"
            motive shouldBe actualMotive
          case _ => fail("Expected IncorrectSolution when accusing wrong suspect")

      "player wrongly accuses the victim" in:
        val result = ValidationResult.validateSolution(victim, solution)

        result match
          case ValidationResult.IncorrectSolution(accused, actualCulprit, motive) =>
            accused shouldBe victim
            accused.name shouldBe "Charlie Davis"
            accused.role shouldBe CaseRole.Victim
            actualCulprit shouldBe guilty
            motive shouldBe actualMotive
          case _ => fail("Expected IncorrectSolution when accusing the victim")

      "player accuses the witness" in:
        val result = ValidationResult.validateSolution(witness, solution)

        result match
          case ValidationResult.IncorrectSolution(accused, actualCulprit, motive) =>
            accused shouldBe witness
            accused.role shouldBe CaseRole.Witness
            actualCulprit shouldBe guilty
            motive shouldBe actualMotive
          case _ => fail("Expected IncorrectSolution")

  "SubmissionState workflow" should:
    "transition to Submitting" when:
      "player selects a suspect and clicks submit button" in:
        val submittingState = SubmissionState.Submitting(guilty)

        submittingState match
          case SubmissionState.Submitting(character) =>
            character shouldBe guilty
            character.name shouldBe "Robert Martinez"
          case _ => fail("Expected Submitting state during accusation process")

    "transition to Submitted" when:
      "validation confirms correct accusation" in:
        val validationResult = ValidationResult.CorrectSolution(
          guilty,
          actualMotive
        )
        val submittedState = SubmissionState.Submitted(validationResult)

        submittedState match
          case SubmissionState.Submitted(result) =>
            result shouldBe a[ValidationResult.CorrectSolution]
            result match
              case ValidationResult.CorrectSolution(culprit, motive) =>
                culprit shouldBe guilty
                motive shouldBe actualMotive
              case _ => fail("Expected CorrectSolution in submitted state")
          case _ => fail("Expected Submitted state after validation")

    "transition to Submitted with IncorrectSolution" when:
      "validation reveals wrong accusation" in:
        val validationResult = ValidationResult.IncorrectSolution(
          innocentSuspect,
          guilty,
          actualMotive
        )
        val submittedState = SubmissionState.Submitted(validationResult)

        submittedState match
          case SubmissionState.Submitted(result) =>
            result shouldBe a[ValidationResult.IncorrectSolution]
            result match
              case ValidationResult.IncorrectSolution(accused, actual, motive) =>
                accused shouldBe innocentSuspect
                actual shouldBe guilty
                motive shouldBe actualMotive
              case _ => fail("Expected IncorrectSolution in submitted state")
          case _ => fail("Expected Submitted state after validation")

    "return CorrectSolution" when:
      "validateSubmission confirms the accused is the actual culprit" in:
        val result = ValidationResult.CorrectSolution(guilty, actualMotive)

        result match
          case ValidationResult.CorrectSolution(culprit, motive) =>
            culprit shouldBe guilty
            culprit.name shouldBe "Robert Martinez"
            motive shouldBe actualMotive
            // This triggers: showGameEndPopup(hasWon = true)
            // Popup displays: "YOU WON!" in green
            // with culprit name and motive
          case _ => fail("Expected CorrectSolution")

    "return IncorrectSolution" when:
      "validateSubmission reveals the accused is not the culprit" in:
        val result = ValidationResult.IncorrectSolution(
          innocentSuspect,
          guilty,
          actualMotive
        )

        result match
          case ValidationResult.IncorrectSolution(accused, actual, motive) =>
            accused shouldBe innocentSuspect
            accused.name shouldBe "Alice Johnson"
            actual shouldBe guilty
            actual.name shouldBe "Robert Martinez"
            motive shouldBe actualMotive
            // This triggers: showGameEndPopup(hasWon = false)
            // Popup displays: "YOU LOSE!" in red
            // with actual culprit and motive revealed
          case _ => fail("Expected IncorrectSolution")

  "Prerequisite validation logic" should:
    "be 'false if the player graph is empty" in:
      val actualCoverage = emptyGraph.coverage(prerequisite)
      actualCoverage shouldBe 0.0
      val prerequisitesMet = actualCoverage >= SolutionConfig.PrerequisiteCoverageThreshold
      prerequisitesMet shouldBe false

    "be 'false' if player graph is partial coverate (50%)" in:
      val actualCoverage = partialGraph.coverage(prerequisite)
      actualCoverage shouldBe 0.5 // (1.0 edges + 0.0 edges) / 2
      val prerequisitesMet = actualCoverage >= SolutionConfig.PrerequisiteCoverageThreshold // 0.5 >= 1.0
      prerequisitesMet shouldBe false

    "be 'true' if player graph is completly coverate" in:
      val actualCoverage = fullGraph.coverage(prerequisite)
      actualCoverage shouldBe 1.0
      val prerequisitesMet = actualCoverage >= SolutionConfig.PrerequisiteCoverageThreshold // 1.0 >= 1.0
      prerequisitesMet shouldBe true

    "be 'false' if 80% of the time has passed" in:
      val totalTime = 100.minutes
      val elapsedTime = 80.minutes // 80% elapsed

      val elapsedPercentage = elapsedTime / totalTime
      (elapsedPercentage >= SolutionConfig.TimeElapsedThreshold) shouldBe false

    "be 'true' if exactly 85% of the time has passed" in:
      val totalTime = 100.minutes
      val elapsedTime = 85.minutes // 85% elapsed

      val elapsedPercentage = elapsedTime / totalTime
      (elapsedPercentage >= SolutionConfig.TimeElapsedThreshold) shouldBe true

    "be 'true' if 95% of the time has passed" in:
      val totalTime = 100.minutes
      val elapsedTime = 95.minutes // 95% elapsed

      val elapsedPercentage = elapsedTime / totalTime
      (elapsedPercentage >= SolutionConfig.TimeElapsedThreshold) shouldBe true

  "Prerequisite OR logic (time or coverage)" should:
    "allow accusation when coverage is true (even if time is false)" in:
      val coverageMet = fullGraph.coverage(prerequisite) >= 1.0 // true
      val timeMet = (0.5 >= 0.85) // false

      (coverageMet || timeMet) shouldBe true

    "allow accusation when time is true (even if coverage is false)" in:
      val coverageMet = emptyGraph.coverage(prerequisite) >= 1.0 // false
      val timeMet = (0.85 >= 0.85) // true

      (coverageMet || timeMet) shouldBe true

    "allow accusation when both are true" in:
      val coverageMet = fullGraph.coverage(prerequisite) >= 1.0 // true
      val timeMet = (0.9 >= 0.85) // true

      (coverageMet || timeMet) shouldBe true

    "not allow accusation when both are false" in:
      val coverageMet = partialGraph.coverage(prerequisite) >= 1.0 // false
      val timeMet = (0.5 >= 0.85) // false

      (coverageMet || timeMet) shouldBe false

  "SolutionConfig thresholds" should:
    "have correct values" in:
      SolutionConfig.PrerequisiteCoverageThreshold shouldBe 1.0
      SolutionConfig.TimeElapsedThreshold shouldBe 0.85
