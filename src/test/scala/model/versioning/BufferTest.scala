package model.versioning

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BufferTest extends AnyWordSpec with Matchers:

  private val maxSize = 3
  "A (mutable) base Buffer" when:
    "newly created" should:
      val buffer = BaseBuffer[Int](maxSize)

      "have size 0" in:
        buffer.size shouldBe 0

      "is empty" in:
        buffer.isEmpty shouldBe true

    "elements pushed" should:
      val buffer = BaseBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)

      "have correct size" in:
        buffer.size shouldBe 3

      "not be empty" in:
        buffer.isEmpty shouldBe false

      "contain the pushed elements" in:
        buffer.contains(1) shouldBe true
        buffer.contains(2) shouldBe true
        buffer.contains(3) shouldBe true

      "have elements in correct order" in:
        buffer.elements shouldBe List(1, 2, 3)

    "elements pushed up to capacity" should:
      val buffer = BaseBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)

      "have size equal to capacity" in:
        buffer.size shouldBe maxSize

      "replace elements when pushing beyond capacity" in:
        buffer.push(4)
        buffer.elements shouldBe List(1, 2, 4)

  "A circular Buffer" when:
    "newly created" should:
      val buffer = CircularBuffer[Int](maxSize)

      "have size 0" in:
        buffer.size shouldBe 0

      "is empty" in:
        buffer.isEmpty shouldBe true

    "elements pushed beyond capacity" should:
      val buffer = CircularBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)
      buffer.push(4)

      "have size equal to capacity" in:
        buffer.size shouldBe maxSize

      "not contain the replaced element" in:
        buffer.contains(1) shouldBe false

      "have elements in correct order" in:
        buffer.elements shouldBe List(2, 3, 4)

  "A navigable Buffer" when:
    "newly created" should:
      val buffer = NavigableBuffer[Int](maxSize)

      "have cursor at initial position" in:
        buffer.currentPosition shouldBe 0

      "have current element at first inserted" in:
        buffer.currentElement shouldBe None

    "elements add" should:
      val buffer = NavigableBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)

      "have cursor at initial position" in:
        buffer.currentPosition shouldBe 0

      "have current element at first inserted" in:
        buffer.currentElement shouldBe Some(1)

    "cursor moved" should:
      val buffer = NavigableBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)

      "can not move cursor backward at initial position" in:
        buffer.moveBackward() shouldBe false
        buffer.currentPosition shouldBe 0

      "move cursor backward correctly" in:
        buffer.moveForward() shouldBe true
        buffer.currentPosition shouldBe 1
        buffer.currentElement shouldBe Some(2)

      "reset cursor correctly" in:
        buffer.resetCursor()
        buffer.currentPosition shouldBe 0

      "move cursor forward correctly" in:
        val buffer2 = NavigableBuffer[Int](maxSize)
        buffer2.push(1)
        buffer2.push(2)
        buffer2.push(3)
        buffer2.moveForward() shouldBe true
        buffer2.moveBackward() shouldBe true
        buffer2.currentPosition shouldBe 0
        buffer2.currentElement shouldBe Some(1)

      "can not move cursor backward at last position" in:
        val buffer3 = NavigableBuffer[Int](maxSize)
        buffer3.push(1)
        buffer3.push(2)
        buffer3.push(3)
        buffer3.moveForward() shouldBe true
        buffer3.moveForward() shouldBe true
        buffer3.moveForward() shouldBe false
        buffer3.currentPosition shouldBe 2
        buffer3.currentElement shouldBe Some(3)

  "A inverse navigable Buffer" when:
    "newly created" should:
      val buffer = InverseNavigableBuffer[Int](maxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)

      "have cursor at initial position" in:
        buffer.currentPosition shouldBe 0

      "have current element at last inserted" in:
        buffer.currentElement shouldBe Some(3)

    "cursor moved" should:
      val inverseMaxSize = 4
      val buffer = InverseNavigableBuffer[Int](inverseMaxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)
      buffer.push(4)

      "can not move cursor backward at initial position" in:
        buffer.moveForward() shouldBe false
        buffer.currentPosition shouldBe 0

      "move cursor forward correctly" in:
        buffer.moveBackward() shouldBe true
        buffer.currentPosition shouldBe 1
        buffer.currentElement shouldBe Some(3)

      "reset cursor correctly" in:
        buffer.resetCursor()
        buffer.currentPosition shouldBe 0

      "move cursor backward correctly" in:
        val buffer2 = InverseNavigableBuffer[Int](inverseMaxSize)
        buffer2.push(1)
        buffer2.push(2)
        buffer2.push(3)
        buffer2.push(4)
        buffer2.moveBackward() shouldBe true
        buffer2.moveForward() shouldBe true
        buffer2.currentPosition shouldBe 0
        buffer2.currentElement shouldBe Some(4)

      "can not move cursor forward at last position" in:
        val buffer3 = InverseNavigableBuffer[Int](inverseMaxSize)
        buffer3.push(1)
        buffer3.push(2)
        buffer3.push(3)
        buffer3.push(4)
        buffer3.moveBackward() shouldBe true
        buffer3.moveBackward() shouldBe true
        buffer3.moveBackward() shouldBe true
        buffer3.moveBackward() shouldBe false
        buffer3.currentPosition shouldBe 3
        buffer3.currentElement shouldBe Some(1)

  "A ring navigable Buffer" when:
    val ringNavigableMaxSize = 4

    "newly created" should:
      "have cursor at initial position" in:
        val buffer = RingNavigableBuffer[Int](ringNavigableMaxSize)
        buffer.push(1)
        buffer.push(2)
        buffer.push(3)
        buffer.currentPosition shouldBe 0

    "elements pushed when cursor is not at initial position" should:
      val buffer = RingNavigableBuffer[Int](ringNavigableMaxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)
      buffer.push(4) // should push normally
      buffer.push(5) // should replace oldest (1)

      "have size equal to capacity" in:
        buffer.size shouldBe 4

      "have elements in correct order" in:
        buffer.elements shouldBe List(2, 3, 4, 5)

      "have cursor unchanged" in:
        buffer.currentPosition shouldBe 0

      "have current element correct" in:
        buffer.currentElement shouldBe Some(5)

    "elements pushed when cursor is not in initial position" should:
      val buffer = RingNavigableBuffer[Int](ringNavigableMaxSize)
      buffer.push(1)
      buffer.push(2)
      buffer.push(3)
      buffer.moveBackward() // cursor at 1, current element 2
      buffer.moveBackward() // cursor at 2, current element 1
      buffer.push(4) // should push and decrease size

      "have size decreased" in:
        buffer.size shouldBe 2

      "have elements in correct order" in:
        buffer.elements shouldBe List(1, 4)

      "have cursor adjusted correctly" in:
        buffer.currentPosition shouldBe 0

      "have current element correct" in:
        buffer.currentElement shouldBe Some(4)
