package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.time.LocalDateTime

class SnapshotTest extends AnyWordSpec with Matchers:
  "A Snapshot" when:
    "created with value" should:
      "contain the original value" in:
        val snapshot = Snapshot.snap(3)
        snapshot.subject shouldBe 3

      "preserve the value type" in:
        val snapshot = Snapshot.snap("Hello World")
        snapshot.subject shouldBe "Hello World"

      "use current time as default" in:
        val before = LocalDateTime.now
        val snapshot = Snapshot.snap(3)
        val after = LocalDateTime.now
        snapshot.timestamp should (be >= before and be <= after)

    "restored" should:
      "return the original value" in:
        val original = 42
        val snapshot = Snapshot.snap(original)
        val restored = Snapshot.restore(snapshot)

        restored shouldBe original

    "created with an immutable object" should:
      "capture the current state" in:
        val history = History(List.empty)
        val snapshot = Snapshot.snap(history.add(3))

        snapshot.subject.elements should contain(3)

      "not be affected by subsequent changes" in:
        val history = History(List.empty)
        val snapshot = Snapshot.snap(history.add(3))
        history.add(3)

        snapshot.subject.elements should contain only 3

      "isolate multiple snapshots from each other" in:
        val history = History(List.empty)
        val snapshot1 = Snapshot.snap(history.add(3))
        val snapshot2 = Snapshot.snap(history.add(3).add(5))

        snapshot1.subject.elements.size shouldBe 1
        snapshot2.subject.elements.size shouldBe 2

    "created with an mutable object" should:
      "capture the current state" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot = Snapshot.snap(history)

        snapshot.subject.elements should contain(3)

      "not be affected by subsequent changes" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot = Snapshot.snap(history)
        history.add(3)

        snapshot.subject.elements should contain only 3

      "isolate multiple snapshots from each other" in:
        val history = MutableHistory(new Array(5))
        history.add(3)
        val snapshot1 = Snapshot.snap(history)
        history.add(5)
        val snapshot2 = Snapshot.snap(history)

        snapshot1.subject.elements.length shouldBe 1
        snapshot2.subject.elements.length shouldBe 2
