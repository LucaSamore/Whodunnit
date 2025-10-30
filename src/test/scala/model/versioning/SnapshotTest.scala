package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SnapshotTest extends AnyWordSpec with Matchers:
  "A Snapshot" when:
    "created with value" should:
      "contain the original value" in:
        val snapshot = Snapshot.snap(3)
        snapshot.subject shouldBe 3

      "preserve the value type" in:
        val snapshot = Snapshot.snap("Hello World")
        snapshot.subject shouldBe "Hello World"
