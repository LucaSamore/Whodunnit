package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ConstraintsTest extends AnyWordSpec with Matchers:

  "Constraints.Theme" when:
    "created with valid value theme" should :
      "store the theme" in :
        val theme = Constraints.Theme("Murder Mystery")
        theme.value shouldBe "Murder Mystery"