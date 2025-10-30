package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BufferTest extends AnyWordSpec with Matchers:

  "A mutable Buffer" when:
    "newly created" should:
      val maxSize = 5
      val buffer = Buffer[Int](maxSize, new Array(maxSize))

      "have size 0" in:
        buffer.currentSize shouldBe 0

      "is empty" in:
        buffer.isEmpty shouldBe true

    "elements added" should:
      val maxSize = 5
      val buffer = Buffer[Int](maxSize, new Array(maxSize))
      buffer.add(1)
      buffer.add(2)
      buffer.add(3)

      "have correct size" in:
        buffer.currentSize shouldBe 3

      "not be empty" in:
        buffer.isEmpty shouldBe false

      "contain the added elements" in:
        buffer.contains(1) shouldBe true
        buffer.contains(2) shouldBe true
        buffer.contains(3) shouldBe true

      "have elements in correct order" in:
        buffer.elementList shouldBe List(1, 2, 3)
