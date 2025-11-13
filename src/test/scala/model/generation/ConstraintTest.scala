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
      val constraints = DifficultyPresets.easy

      constraints should contain(CharactersRange(2, 4))
      constraints should contain(CaseFilesRange(2, 5))
      constraints should contain(PrerequisitesRange(1, 2))
      constraints should have size 3
      constraints.collect { case t: Theme => t } shouldBe empty

  "DifficultyPresets.medium" should:
    "return Constraint for medium difficulty" in:
      val constraints = DifficultyPresets.medium

      constraints should contain(CharactersRange(3, 5))
      constraints should contain(CaseFilesRange(4, 8))
      constraints should contain(PrerequisitesRange(1, 3))
      constraints should have size 3

  "DifficultyPresets.hard" should:
    "return Constraint for hard difficulty" in:
      val constraints = DifficultyPresets.hard

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

    "describe Helpful Hint constraint" in:
      val hint = HintKind.Helpful

      hint.toPromptDescription shouldBe "The hint to be generated must be Helpful"

    "describe Misleading Hint constraint" in:
      val hint = HintKind.Misleading

      hint.toPromptDescription shouldBe "The hint to be generated must be Misleading"

    "describe Easy difficulty constraint" in:
      val easy = Difficulty.Easy

      easy.toPromptDescription shouldBe "Difficulty: Easy\n" +
        "Number of characters: between 2 and 4\n" +
        "Number of case files: between 2 and 5\n" +
        "Solution prerequisites: between 1 and 2"

    "describe Medium difficulty constraint" in:
      val easy = Difficulty.Medium

      easy.toPromptDescription shouldBe "Difficulty: Medium\n" +
        "Number of characters: between 3 and 5\n" +
        "Number of case files: between 4 and 8\n" +
        "Solution prerequisites: between 1 and 3"

    "describe Hard difficulty constraint" in:
      val easy = Difficulty.Hard

      easy.toPromptDescription shouldBe "Difficulty: Hard\n" +
        "Number of characters: between 4 and 6\n" +
        "Number of case files: between 7 and 10\n" +
        "Solution prerequisites: between 2 and 5"

    "return only explicit constraints when no difficulty is provided" in:
      val result = Seq(Theme("Mystery"), CharactersRange(3, 5))

      result should contain only (Theme("Mystery"), CharactersRange(3, 5))
      result should have size 2
