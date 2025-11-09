package model.game

import model.game
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HistoryTest extends AnyWordSpec with Matchers:

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

      "preserve cursor position after undo operations" in:
        case class MockCaseKnowledgeGraph(id: Int) extends CaseKnowledgeGraph:
          override def deepCopy(): CaseKnowledgeGraph =
            MockCaseKnowledgeGraph(id)
        val kg1 = MockCaseKnowledgeGraph(1)
        val kg2 = MockCaseKnowledgeGraph(2)
        val kg3 = MockCaseKnowledgeGraph(3)

        val h1 = GameHistory(maxSize)
          .addState(kg1)
          .addState(kg2)
          .addState(kg3)

        val (h2, _) = h1.undo()
        val copied = h2.deepCopy()

        // copy should have the same current state
        copied.currentState shouldBe h2.currentState
        copied.currentState shouldBe Some(kg2)
        copied shouldEqual h2

        // verify that undoing both histories yields the same result
        val (h3, undoState) = h2.undo()
        val (copiedH3, copiedUndoState) = copied.undo()
        undoState shouldBe copiedUndoState
        undoState shouldBe Some(kg1)

    "elements are added" should:
      val maxSize = 3
      case class MockCaseKnowledgeGraph(id: Int) extends CaseKnowledgeGraph:
        override def deepCopy(): CaseKnowledgeGraph = MockCaseKnowledgeGraph(id)
      val kg1 = MockCaseKnowledgeGraph(1)
      val kg2 = MockCaseKnowledgeGraph(2)

      val history1 = GameHistory(maxSize)
      val history2 = history1.addState(kg1)
      val history3 = history2.addState(kg2)

      "maintain the correct current state" in:
        history3.currentState shouldBe Some(kg2)

      "not modify the original history" in:
        history1.currentState shouldBe None
        history2.currentState shouldBe Some(kg1)
        history3.currentState shouldBe Some(kg2)

      "have the right size" in:
        history1.states.size shouldBe 0
        history2.states.size shouldBe 1
        history3.states.size shouldBe 2

    "undo operation is called" should:
      val maxSize = 3
      case class MockCaseKnowledgeGraph(id: Int)
          extends game.CaseKnowledgeGraph:
        override def deepCopy(): CaseKnowledgeGraph = MockCaseKnowledgeGraph(id)
      val kg1 = MockCaseKnowledgeGraph(1)
      val kg2 = MockCaseKnowledgeGraph(2)
      val history = GameHistory(maxSize)
        .addState(kg1)
        .addState(kg2)

      "revert to the previous state" in:
        val (_, state: Option[CaseKnowledgeGraph]) =
          history.undo()
        state shouldBe Some(kg1)

      "not modify the original history" in:
        val (_, _) = history.undo()
        history.currentState shouldBe Some(kg2)

    "redo operation is called" should:
      val maxSize = 3
      case class MockCaseKnowledgeGraph(id: Int)
          extends game.CaseKnowledgeGraph:
        override def deepCopy(): CaseKnowledgeGraph = MockCaseKnowledgeGraph(id)

      val kg1 = MockCaseKnowledgeGraph(1)
      val kg2 = MockCaseKnowledgeGraph(2)
      val kg3 = MockCaseKnowledgeGraph(3)
      val history = GameHistory(maxSize)
        .addState(kg1)
        .addState(kg2)
        .addState(kg3)

      "reload to the subsequent state" in:
        val (h1, _) = history.undo()
        val (h2, _) = h1.undo()
        val (h3, state) = h2.redo()
        state shouldBe Some(kg2)

      "not modify the original history" in:
        val (h1, _) = history.undo()
        val (h2, _) = h1.undo()
        val (h3, _) = h2.redo()
        history.currentState shouldBe Some(kg3)

    /*
    "combination of undo and redo are called" should:
      val maxSize = 3
      val originalHistory = GameHistory(maxSize)
      case class MockCaseKnowledgeGraph(id: Int)
          extends game.CaseKnowledgeGraph:
        override def deepCopy(): CaseKnowledgeGraph = MockCaseKnowledgeGraph(id)

      val kg1 = MockCaseKnowledgeGraph(1)
      val kg2 = MockCaseKnowledgeGraph(2)
      val kg3 = MockCaseKnowledgeGraph(3)
      originalHistory.addState(kg1)
      originalHistory.addState(kg2)
      originalHistory.addState(kg3)

      "work correctly" in:
        originalHistory.redo() shouldBe None
        originalHistory.currentState shouldBe Some(kg3)
        originalHistory.undo() shouldBe Some(kg2)
        originalHistory.redo() shouldBe Some(kg3)
        originalHistory.currentState shouldBe Some(kg3)
        originalHistory.redo() shouldBe None
        originalHistory.currentState shouldBe Some(kg3)

      "adding a new state after undo clears redo history" in:
        originalHistory.undo() shouldBe Some(kg2)
        originalHistory.undo() shouldBe Some(kg1)
        val kg4 = MockCaseKnowledgeGraph(4)
        originalHistory.addState(kg4)
        originalHistory.redo() shouldBe None // redo should not be possible anymore
        originalHistory.currentState shouldBe Some(kg4)
        originalHistory.undo() shouldBe Some(kg1)
        originalHistory.undo() shouldBe None
     */
