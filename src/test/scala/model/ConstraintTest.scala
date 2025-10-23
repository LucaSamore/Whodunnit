package model

import model.Constraint.{
  CaseFilesRange,
  CharactersRange,
  PrerequisitesRange,
  Theme
}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ConstraintTest extends AnyWordSpec with Matchers:

  "Constraint.Theme" when:
    "created with valid value theme" should:
      "store the theme" in:
        val theme = Constraint.Theme("Murder Mystery")
        theme.value shouldBe "Murder Mystery"

  "Constraint.CharactersRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = Constraint.CharactersRange(2, 5)
        range.min shouldBe 2
        range.max shouldBe 5

  "Constraint.CaseFilesRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = Constraint.CaseFilesRange(3, 8)
        range.min shouldBe 3
        range.max shouldBe 8

  "Constraint.PrerequisitesRange" when:
    "created with valid range" should:
      "store min and max" in:
        val range = Constraint.PrerequisitesRange(1, 4)
        range.min shouldBe 1
        range.max shouldBe 4

  "DifficultyPresets.easy" should:
    "return Constraint for easy difficulty" in:
      val Constraint = DifficultyPresets.easy("Murder")

      Constraint should contain(Theme("Murder"))
      Constraint should contain(CharactersRange(2, 4))
      Constraint should contain(CaseFilesRange(2, 5))
      Constraint should contain(PrerequisitesRange(1, 2))

  "DifficultyPresets.medium" should:
    "return Constraint for medium difficulty" in:
      val Constraint = DifficultyPresets.medium("Espionage")

      Constraint should contain(Theme("Espionage"))
      Constraint should contain(CharactersRange(3, 5))
      Constraint should contain(CaseFilesRange(4, 8))
      Constraint should contain(PrerequisitesRange(1, 3))

  "DifficultyPresets.hard" should:
    "return Constraint for hard difficulty" in:
      val Constraint = DifficultyPresets.hard("Conspiracy")

      Constraint should contain(Theme("Conspiracy"))
      Constraint should contain(CharactersRange(4, 6))
      Constraint should contain(CaseFilesRange(7, 10))
      Constraint should contain(PrerequisitesRange(2, 5))
