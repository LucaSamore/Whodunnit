package model.game

import model.game
import model.versioning.Snapshot
import model.versioning.Snapshot.Snapshotters.given_Snapshottable_History
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime

class TimeMachineTest extends AnyWordSpec with Matchers:

  "A game Time Machine" when:
    "newly created" should:
      val timeMachine = GameTimeMachine[History]()

      "have no snapshot" in:
        timeMachine.hasSnapshot shouldBe false

    "newly created with a snapshot" should:
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      val snapshot = Snapshot(gameHistory, LocalDateTime.now())
      val timeMachine = GameTimeMachine[History](Some(snapshot))

      "have a snapshot" in:
        timeMachine.hasSnapshot shouldBe true

    "a snapshot is taken" should:
      val timeMachine = GameTimeMachine[History]()
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      val beforeSnapshot = LocalDateTime.now()
      timeMachine.save(gameHistory)
      val afterSnapshot = LocalDateTime.now()

      "have a snapshot" in:
        timeMachine.hasSnapshot shouldBe true

      "snapshot time should be within the correct time bounds" in:
        val snapshotTime = timeMachine.snapshotTime
        snapshotTime shouldBe defined
        snapshotTime.get.isAfter(beforeSnapshot) || snapshotTime.get.isEqual(
          beforeSnapshot
        ) shouldBe true
        snapshotTime.get.isBefore(afterSnapshot) || snapshotTime.get.isEqual(
          afterSnapshot
        ) shouldBe true

    "a snapshot is cleared" should:
      val timeMachine = GameTimeMachine[History]()
      val maxSize = 5
      val gameHistory = GameHistory(maxSize)
      timeMachine.save(gameHistory)
      timeMachine.clear()

      "have no snapshot" in:
        timeMachine.hasSnapshot shouldBe false

    "a snapshot is restored" should:
      val timeMachine = GameTimeMachine[History]()
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
        case class MockCaseKnowledgeGraph(id: Int)
            extends game.CaseKnowledgeGraph:
          override def deepCopy(): CaseKnowledgeGraph =
            MockCaseKnowledgeGraph(id)
        modifiedHistory.addState(MockCaseKnowledgeGraph(1))
        modifiedHistory should not equal gameHistory
        val restoredAfterModification = timeMachine.restore()
        restoredAfterModification shouldBe Some(gameHistory)

    "restoring without a snapshot" should:
      val timeMachine = GameTimeMachine[History]()
      val restoredHistory = timeMachine.restore()
      "return None" in:
        restoredHistory shouldBe None
