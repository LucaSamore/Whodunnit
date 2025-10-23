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