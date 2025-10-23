package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ConstraintsTest extends AnyWordSpec with Matchers:

  "Constraints.Theme" when:
    "created with valid value theme" should :
      "store the theme" in :
        val theme = Constraints.Theme("Murder Mystery")
        theme.value shouldBe "Murder Mystery"

  "Constraint.CharactersRange" when :
    "created with valid range" should :
      "store min and max" in :
        val range = Constraints.CharactersRange(2, 5)
        range.min shouldBe 2
        range.max shouldBe 5

  "Constraint.CaseFilesRange" when :
    "created with valid range" should :
      "store min and max" in :
        val range = Constraints.CaseFilesRange(3, 8)
        range.min shouldBe 3
        range.max shouldBe 8

  "Constraint.PrerequisitesRange" when :
    "created with valid range" should :
      "store min and max" in :
        val range = Constraints.PrerequisitesRange(1, 4)
        range.min shouldBe 1
        range.max shouldBe 4

  "Difficulty enum" should :
    "have three levels" in :
      import Constraints.Difficulty.*
      Easy.toString shouldBe "Easy"
      Medium.toString shouldBe "Medium"
      Hard.toString shouldBe "Hard"