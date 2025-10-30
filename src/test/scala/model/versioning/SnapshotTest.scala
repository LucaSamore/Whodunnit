package model.versioning

import model.versioning.Snapshot.Snapshotters.{
  given_Snapshottable_History,
  given_Snapshottable_Int,
  given_Snapshottable_MutableHistory,
  given_Snapshottable_String
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SnapshotTest extends AnyWordSpec with Matchers:
  "A Snapshot" when:
    "created with value" should:
      "contain the original value" in:
        val snapshot = Snapshot(3)
        snapshot.subject shouldBe 3

      "preserve the value type" in:
        val snapshot = Snapshot("Hello World")
        snapshot.subject shouldBe "Hello World"

    "restored" should:
      "return the original value" in:
        val original = 42
        val snapshot = Snapshot(original)
        val restored = Snapshot.restore(snapshot)

        restored shouldBe original

    "created with an immutable object" should:
      "capture the current state" in:
        val history = History(List.empty)
        val snapshot = Snapshot(history.add(3))

        snapshot.subject.elements should contain(3)

      "not be affected by subsequent changes" in:
        val history = History(List.empty)
        val snapshot = Snapshot(history.add(3))
        history.add(3)

        snapshot.subject.elements should contain only 3

      "isolate multiple snapshots from each other" in:
        val history = History(List.empty)
        val snapshot1 = Snapshot(history.add(3))
        val snapshot2 = Snapshot(history.add(3).add(5))

        snapshot1.subject.elements.size shouldBe 1
        snapshot2.subject.elements.size shouldBe 2

    "created with an mutable object" should:
      "capture the current state" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot = Snapshot(history)

        snapshot.subject.elements should contain(3)

      "not be affected by subsequent changes" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot = Snapshot(history)
        history.add(3)

        snapshot.subject.elements should contain only 3

      "isolate multiple snapshots from each other" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot1 = Snapshot(history)
        history.add(5)
        val snapshot2 = Snapshot(history)

        snapshot1.subject.elements.length shouldBe 1
        snapshot2.subject.elements.length shouldBe 2
