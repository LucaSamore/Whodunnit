package model.generation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConstraintTest extends AnyWordSpec with Matchers:

  "Constraint.Theme" when:
    "created with valid value theme" should:
      "store the theme" in:
        val theme = Theme("Murder Mystery")
        theme.value shouldBe "Murder Mystery"

  "Constraint.CharactersRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = CharactersRange(2, 5)
        range.min shouldBe 2
        range.max shouldBe 5

  "Constraint.CaseFilesRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = CaseFilesRange(3, 8)
        range.min shouldBe 3
        range.max shouldBe 8

  "Constraint.PrerequisitesRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = PrerequisitesRange(1, 4)
        range.min shouldBe 1
        range.max shouldBe 4

  "DifficultyPresets.easy" should:
    "return Constraint for easy difficulty" in:
      val constraints = DifficultyPresets.easy()

      constraints should contain(CharactersRange(2, 4))
      constraints should contain(CaseFilesRange(2, 5))
      constraints should contain(PrerequisitesRange(1, 2))
      constraints should have size 3
      constraints.collect { case t: Theme => t } shouldBe empty

  "DifficultyPresets.medium" should:
    "return Constraint for medium difficulty" in:
      val constraints = DifficultyPresets.medium()

      constraints should contain(CharactersRange(3, 5))
      constraints should contain(CaseFilesRange(4, 8))
      constraints should contain(PrerequisitesRange(1, 3))
      constraints should have size 3

  "DifficultyPresets.hard" should:
    "return Constraint for hard difficulty" in:
      val constraints = DifficultyPresets.hard()

      constraints should contain(CharactersRange(4, 6))
      constraints should contain(CaseFilesRange(7, 10))
      constraints should contain(PrerequisitesRange(2, 5))
      constraints should have size 3

  "Constraint.toPromptDescription" should:
    "describe Theme constraint" in:
      val theme = Theme("Murder Mystery")
      theme.toPromptDescription shouldBe "Theme: Murder Mystery"

    "describe CharactersRange constraint" in:
      val range = CharactersRange(2, 4)
      range.toPromptDescription shouldBe "Number of characters: between 2 and 4"

    "describe CaseFilesRange constraint" in:
      val range = CaseFilesRange(3, 8)
      range.toPromptDescription shouldBe "Number of case files: between 3 and 8"

    "describe PrerequisitesRange constraint" in:
      val range = PrerequisitesRange(1, 3)
      range.toPromptDescription shouldBe "Solution prerequisites: between 1 and 3"

  "Constraint.expandConstraints" should:
    "expand Easy difficulty to easy preset constraints without theme" in:
      import Difficulty.Easy

      val result = Constraint.expandConstraints(Seq(Theme("Murder"), Easy))

      result should contain(Theme("Murder"))
      result should contain(CharactersRange(2, 4))
      result should contain(CaseFilesRange(2, 5))
      result should contain(PrerequisitesRange(1, 2))
      result should have size 4

    "expand Medium difficulty to medium preset constraints" in:
      import Difficulty.Medium

      val result = Constraint.expandConstraints(Seq(Medium))

      result should contain(CharactersRange(3, 5))
      result should contain(CaseFilesRange(4, 8))
      result should contain(PrerequisitesRange(1, 3))
      result should have size 3

    "expand Hard difficulty to hard preset constraints" in:
      import Difficulty.Hard

      val result = Constraint.expandConstraints(Seq(Hard))

      result should contain(CharactersRange(4, 6))
      result should contain(CaseFilesRange(7, 10))
      result should contain(PrerequisitesRange(2, 5))
      result should have size 3

    "return only explicit constraints when no difficulty is provided" in:
      val result = Constraint.expandConstraints(
        Seq(Theme("Mystery"), CharactersRange(3, 5))
      )

      result should contain only (Theme("Mystery"), CharactersRange(3, 5))
      result should have size 2
