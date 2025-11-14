package model.versioning

/** A generic buffer interface for storing a fixed number of elements.
  *
  * This trait defines the core operations for a bounded buffer that can store elements up to a specified capacity.
  * Different implementations can provide various behaviors when the buffer is full (e.g., overwriting, circular).
  */
trait Buffer:
  /** The type of elements stored in this buffer */
  type Element

  /** The maximum number of elements this buffer can hold */
  val capacity: Int

  /** Checks if the buffer is empty.
    *
    * @return
    *   true if the buffer contains no elements, false otherwise
    */
  def isEmpty: Boolean

  /** Returns all elements currently stored in the buffer.
    *
    * @return
    *   a sequence of all elements in the buffer
    */
  def elements: Seq[Element]

  /** Sets an element at a specific index in the buffer.
    *
    * @param index
    *   the index where to set the element
    * @param element
    *   the element to set
    */
  def set(index: Int, element: Element): Unit

  /** Adds an element to the buffer.
    *
    * If the buffer is not full, the element is added. If full, the behavior depends on the implementation (see
    * replaceOnFull).
    *
    * @param element
    *   the element to add
    */
  def push(element: Element): Unit

  /** Defines the behavior when pushing to a full buffer.
    *
    * This method is called by push when the buffer is at capacity. Different implementations can override this to
    * provide different full-buffer behaviors.
    *
    * @param element
    *   the element to add to the full buffer
    */
  def replaceOnFull(element: Element): Unit

  /** Checks if the buffer contains a specific element.
    *
    * @param element
    *   the element to search for
    * @return
    *   true if the element is in the buffer, false otherwise
    */
  def contains(element: Element): Boolean

  /** Returns the current number of elements in the buffer.
    *
    * @return
    *   the number of elements currently stored
    */
  def size: Int

/** Base implementation of a fixed-capacity buffer.
  *
  * This abstract class provides the fundamental buffer operations using an internal array. When the buffer is full, new
  * elements replace the last element by default.
  *
  * @tparam E
  *   the type of elements stored in the buffer (requires ClassTag for array creation)
  * @param capacity
  *   the maximum number of elements the buffer can hold
  */
abstract class BaseBuffer[E](override val capacity: Int)(using
    reflect.ClassTag[E]
) extends Buffer:
  type Element = E
  private val buffer: Array[Option[E]] = Array.fill(capacity)(None)
  protected var _size: Int = 0

  override def size: Int = _size

  override def isEmpty: Boolean = _size == 0

  override def elements: Seq[E] = buffer.take(_size).flatten

  override def set(index: Int, element: E): Unit =
    buffer(index) = Some(element)

  override def push(element: E): Unit =
    if (_size < capacity) then
      buffer(_size) = Some(element)
      _size += 1
    else
      replaceOnFull(element)

  override def replaceOnFull(element: E): Unit =
    buffer(capacity - 1) = Some(element)

  override def contains(element: E): Boolean =
    buffer.take(_size).flatten.contains(element)

/** Adds circular (ring) buffer behavior to a Buffer.
  *
  * When the buffer is full, new elements overwrite the oldest elements in a circular fashion. The head pointer tracks
  * the position of the oldest element. This trait must be mixed into a Buffer implementation.
  */
trait CircularBuffer extends Buffer:
  protected var head: Int = 0

  abstract override def replaceOnFull(element: Element): Unit =
    set(head, element)
    head = (head + 1) % capacity

  abstract override def elements: Seq[Element] =
    if size < capacity then
      super.elements
    else
      (0 until capacity).map(i => super.elements((head + i) % capacity))

/** Adds navigation capabilities to a Buffer.
  *
  * This trait provides cursor-based navigation through buffer elements, allowing forward and backward movement through
  * the stored items. The cursor starts at position 0 (the first element).
  *
  * This trait requires self-type Buffer, meaning it can only be mixed into a Buffer implementation.
  */
trait Navigability:
  self: Buffer =>

  protected var cursor: Int = 0

  /** Returns the current cursor position.
    *
    * @return
    *   the zero-based index of the cursor
    */
  def currentPosition: Int = cursor

  /** Resets the cursor to the beginning of the buffer. */
  def resetCursor(): Unit =
    cursor = 0

  /** Returns the element at the current cursor position.
    *
    * @return
    *   Some(element) if the buffer is not empty, None otherwise
    */
  def currentElement: Option[Element] =
    if isEmpty then None
    else Some(elements(cursor))

  /** Moves the cursor forward by one position.
    *
    * @return
    *   true if the cursor was moved, false if already at the last element
    */
  def moveForward(): Boolean =
    if cursor < size - 1 then
      cursor += 1
      true
    else
      false

  /** Moves the cursor backward by one position.
    *
    * @return
    *   true if the cursor was moved, false if already at the first element
    */
  def moveBackward(): Boolean =
    if cursor > 0 then
      cursor -= 1
      true
    else
      false

/** Provides inverse (backwards) navigation through a buffer.
  *
  * This trait reverses the navigation direction so that position 0 refers to the most recent (last) element, and moving
  * forward goes towards older elements. This is useful for undo/redo functionality where recent actions are more
  * relevant.
  *
  * This trait requires self-type Buffer, meaning it can only be mixed into a Buffer implementation.
  */
trait InverseNavigability extends Navigability:
  self: Buffer =>

  override def currentElement: Option[Element] =
    if isEmpty then None
    else Some(elements(size - 1 - cursor))

  override def moveForward(): Boolean =
    super.moveBackward()

  override def moveBackward(): Boolean =
    super.moveForward()

/** A circular buffer with inverse navigation capabilities.
  *
  * This buffer combines circular buffer behavior (overwriting oldest elements when full) with inverse navigation
  * (starting from the most recent element). When an element is pushed while the cursor is not at position 0, the buffer
  * discards all elements "after" the cursor and adds the new element.
  *
  * This is particularly useful for implementing undo/redo functionality where new actions should invalidate any
  * "future" states.
  *
  * @tparam E
  *   the type of elements stored in the buffer (requires ClassTag for array creation)
  * @param capacity
  *   the maximum number of elements the buffer can hold
  */
abstract class RingNavigableBuffer[E](override val capacity: Int)(using
    reflect.ClassTag[E]
) extends BaseBuffer[E](capacity) with CircularBuffer
    with InverseNavigability:

  override def push(element: Element): Unit =
    if currentPosition == 0 then
      super.push(element)
    else
      val currentLogicalIndex = size - 1 - currentPosition
      val newSize = currentLogicalIndex + 1
      val insertIndex = (head + newSize) % capacity
      set(insertIndex, element)

      head = (head + newSize + 1) % capacity
      _size = Math.min(newSize + 1, capacity)
      cursor = 0

/** Factory object for creating BaseBuffer instances. */
object BaseBuffer:
  /** Creates a simple base buffer with the specified capacity.
    *
    * @tparam T
    *   the type of elements to store (requires ClassTag)
    * @param capacity
    *   the maximum number of elements
    * @return
    *   a new BaseBuffer instance
    */
  def apply[T: reflect.ClassTag](capacity: Int): BaseBuffer[T] =
    new BaseBuffer[T](capacity) {}

/** Factory object for creating CircularBuffer instances. */
object CircularBuffer:
  /** Creates a circular buffer with the specified capacity.
    *
    * @tparam T
    *   the type of elements to store (requires ClassTag)
    * @param capacity
    *   the maximum number of elements
    * @return
    *   a new circular buffer instance
    */
  def apply[T: reflect.ClassTag](capacity: Int)
      : BaseBuffer[T] with CircularBuffer =
    new BaseBuffer[T](capacity) with CircularBuffer {}

/** Factory object for creating navigable buffer instances. */
object NavigableBuffer:
  /** Creates a navigable buffer with the specified capacity.
    *
    * @tparam T
    *   the type of elements to store (requires ClassTag)
    * @param capacity
    *   the maximum number of elements
    * @return
    *   a new navigable buffer instance
    */
  def apply[T: reflect.ClassTag](capacity: Int)
      : BaseBuffer[T] with Navigability =
    new BaseBuffer[T](capacity) with Navigability {}

/** Factory object for creating inverse navigable buffer instances. */
object InverseNavigableBuffer:
  /** Creates an inverse navigable buffer with the specified capacity.
    *
    * @tparam T
    *   the type of elements to store (requires ClassTag)
    * @param capacity
    *   the maximum number of elements
    * @return
    *   a new inverse navigable buffer instance
    */
  def apply[T: reflect.ClassTag](capacity: Int)
      : BaseBuffer[T] with InverseNavigability =
    new BaseBuffer[T](capacity) with InverseNavigability {}

/** Factory object for creating ring navigable buffer instances. */
object RingNavigableBuffer:
  /** Creates a ring navigable buffer with the specified capacity.
    *
    * This buffer combines circular overwriting with inverse navigation, making it ideal for undo/redo functionality.
    *
    * @tparam T
    *   the type of elements to store (requires ClassTag)
    * @param capacity
    *   the maximum number of elements
    * @return
    *   a new ring navigable buffer instance
    */
  def apply[T: reflect.ClassTag](capacity: Int)
      : RingNavigableBuffer[T] with CircularBuffer with InverseNavigability =
    new RingNavigableBuffer[T](capacity) with CircularBuffer
      with InverseNavigability {}
