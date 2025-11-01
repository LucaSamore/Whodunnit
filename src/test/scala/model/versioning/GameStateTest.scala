package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameStateTest extends AnyWordSpec with Matchers:

  "A game History" when:
    "newly created" should:
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)

      "have current state as None" in:
        gameHistory.currentState shouldBe None

    "deep copied" should:
      val maxSize = 5
      val originalHistory = GameHistory(maxSize)
      val copiedHistory = originalHistory.deepCopy()

      "be a deep copy (equal but distinct instance)" in:
        copiedHistory should not be theSameInstanceAs(originalHistory)
        copiedHistory shouldEqual originalHistory