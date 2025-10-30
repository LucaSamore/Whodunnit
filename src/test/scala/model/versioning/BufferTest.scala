package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BufferTest extends AnyWordSpec with Matchers:

  private val maxSize = 3
  "A (mutable) base Buffer" when:
    "newly created" should:
      val buffer = BaseBuffer[Int](maxSize)

      "have size 0" in:
        buffer.currentSize shouldBe 0

      "is empty" in:
        buffer.isEmpty shouldBe true

    "elements added" should:
      val buffer = BaseBuffer[Int](maxSize)
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

    "elements added up to capacity" should:
      val buffer = BaseBuffer[Int](maxSize)
      buffer.add(1)
      buffer.add(2)
      buffer.add(3)

      "have size equal to capacity" in:
        buffer.currentSize shouldBe maxSize

      "replace elements when adding beyond capacity" in:
        buffer.add(4)
        buffer.elementList shouldBe List(1, 2, 4)

  "A circular Buffer" when:
    "newly created" should:
      val buffer = CircularBuffer[Int](maxSize)

      "have size 0" in:
        buffer.currentSize shouldBe 0

      "is empty" in:
        buffer.isEmpty shouldBe true

    "elements added beyond capacity" should:
      val buffer = CircularBuffer[Int](maxSize)
      buffer.add(1)
      buffer.add(2)
      buffer.add(3)
      buffer.add(4)

      "have size equal to capacity" in:
        buffer.currentSize shouldBe maxSize

      "not contain the replaced element" in:
        buffer.contains(1) shouldBe false

      "have elements in correct order" in:
        buffer.elementList shouldBe List(2, 3, 4)

  "A navigable Buffer" when:
    "newly created" should:
      val buffer = NavigableBuffer[Int](maxSize)
      buffer.add(1)
      buffer.add(2)
      buffer.add(3)

      "have cursor at initial position" in:
        buffer.currentPosition shouldBe 0

      "have current element at last inserted" in:
        buffer.currentElement shouldBe Some(3)

      "can not move cursor forward at initial position" in:
        buffer.moveForward() shouldBe false
        buffer.currentPosition shouldBe 0

      "move cursor backward correctly" in:
        buffer.moveBackward() shouldBe true
        buffer.currentPosition shouldBe 1
        buffer.currentElement shouldBe Some(2)

      "reset cursor correctly" in:
        buffer.resetCursor()
        buffer.currentPosition shouldBe 0

      "move cursor forward correctly" in:
        val buffer2 = NavigableBuffer[Int](maxSize)
        buffer2.add(1)
        buffer2.add(2)
        buffer2.add(3)
        buffer2.moveBackward() shouldBe true
        buffer2.moveForward() shouldBe true
        buffer2.currentPosition shouldBe 0
        buffer2.currentElement shouldBe Some(3)

      "can not move cursor backward at last position" in:
        val buffer3 = NavigableBuffer[Int](maxSize)
        buffer3.add(1)
        buffer3.add(2)
        buffer3.add(3)
        buffer3.moveBackward() shouldBe true
        buffer3.moveBackward() shouldBe true
        buffer3.moveBackward() shouldBe false
        buffer3.currentPosition shouldBe 2
