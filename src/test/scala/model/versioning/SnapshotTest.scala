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
