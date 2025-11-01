package model.versioning

import model.versioning.Snapshot.Snapshotters.given_Snapshottable_GameHistory
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
      case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
        def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)
      val kg2 = MockKnowledgeGraph(2)
      originalHistory.addState(MockKnowledgeGraph(1))
      originalHistory.addState(kg2)

      "maintain the correct current state" in:
        originalHistory.currentState shouldBe Some(kg2)

    "undo operation is called" should:
      val maxSize = 3
      val originalHistory = GameHistory(maxSize)
      case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
        def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)
      val kg1 = MockKnowledgeGraph(1)
      originalHistory.addState(kg1)
      originalHistory.addState(MockKnowledgeGraph(2))

      "revert to the previous state" in:
        originalHistory.undo() shouldBe Some(kg1)

    "redo operation is called" should:
      val maxSize = 3
      val originalHistory = GameHistory(maxSize)
      case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
        def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)
      val kg2 = MockKnowledgeGraph(2)
      originalHistory.addState(MockKnowledgeGraph(1))
      originalHistory.addState(kg2)
      originalHistory.addState(MockKnowledgeGraph(3))

      "reload to the subsequent state" in:
        originalHistory.undo()
        originalHistory.undo()
        originalHistory.redo() shouldBe Some(kg2)

    "combination of undo and redo are called" should:
      val maxSize = 3
      val originalHistory = GameHistory(maxSize)
      case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
        def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)

      val kg1 = MockKnowledgeGraph(1)
      val kg2 = MockKnowledgeGraph(2)
      val kg3 = MockKnowledgeGraph(3)
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
        val kg4 = MockKnowledgeGraph(4)
        originalHistory.addState(kg4)
        originalHistory.redo() shouldBe None // redo should not be possible anymore
        originalHistory.currentState shouldBe Some(kg4)
        originalHistory.undo() shouldBe Some(kg1)
        originalHistory.undo() shouldBe None

  "A game Time Machine" when:
    "newly created" should:
      val timeMachine = HistoryTimeMachine[GameHistory]()

      "have no snapshot" in:
        timeMachine.hasSnapshot shouldBe false

    "a snapshot is taken" should:
      val timeMachine = HistoryTimeMachine[GameHistory]()
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      timeMachine.save(gameHistory)

      "have a snapshot" in:
        timeMachine.hasSnapshot shouldBe true

    "a snapshot is cleared" should:
      val timeMachine = HistoryTimeMachine[GameHistory]()
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      timeMachine.save(gameHistory)
      timeMachine.clear()

      "have no snapshot" in:
        timeMachine.hasSnapshot shouldBe false

    "a snapshot is restored" should:
      val timeMachine = HistoryTimeMachine[GameHistory]()
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      timeMachine.save(gameHistory)
      val restoredHistory = timeMachine.restore()

      "restore the correct state" in:
        restoredHistory shouldBe Some(gameHistory)

      "preserve equality but be a distinct instance" in:
        restoredHistory.get should not be theSameInstanceAs(gameHistory)
        restoredHistory.get shouldEqual gameHistory

      "preserve the state if modified after snapshot" in:
        val modifiedHistory = gameHistory.deepCopy()
        case class MockKnowledgeGraph(id: Int) extends KnowledgeGraph:
          def deepCopy(): KnowledgeGraph = MockKnowledgeGraph(id)
        modifiedHistory.addState(MockKnowledgeGraph(1))
        modifiedHistory should not equal gameHistory
        val restoredAfterModification = timeMachine.restore()
        restoredAfterModification shouldBe Some(gameHistory)

    "restoring without a snapshot" should:
      val timeMachine = HistoryTimeMachine[GameHistory]()
      val restoredHistory = timeMachine.restore()
      "return None" in:
        restoredHistory shouldBe None
