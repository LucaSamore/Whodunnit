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

    "elements are added" should:
      val maxSize = 3
      val originalHistory = GameHistory(maxSize)
      // Mock KnowledgeGraph instances
      case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
        def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)

      val kg1 = MockKnowledgeGraph(1)
      val kg2 = MockKnowledgeGraph(2)

      originalHistory.addState(kg1)
      originalHistory.addState(kg2)

      "maintain the correct current state" in:
        originalHistory.currentState shouldBe Some(kg2)
