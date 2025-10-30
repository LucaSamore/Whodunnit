package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BufferTest extends AnyWordSpec with Matchers:

  "A Buffer" when:
    "newly created" should:
      val buffer = Buffer(5, List.empty[Int])

      "have size 0" in:
        buffer.size shouldBe 0

      "be empty" in:
        buffer.isEmpty shouldBe true
