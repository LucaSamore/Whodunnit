package model.versioning

trait Buffer:
  type Element
  val capacity: Int
  def isEmpty: Boolean
  def elements: List[Element]
  def set(index: Int, element: Element): Unit
  def push(element: Element): Unit
  def replaceOnFull(element: Element): Unit
  def contains(element: Element): Boolean
  def size: Int

abstract class BaseBuffer[E](override val capacity: Int)(using
    reflect.ClassTag[E]
) extends Buffer:
  type Element = E
  private val buffer: Array[Option[E]] = Array.fill(capacity)(None)
  protected var _size: Int = 0

  override def size: Int = _size

  override def isEmpty: Boolean = _size == 0

  override def elements: List[E] = buffer.take(_size).flatten.toList

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

trait CircularBuffer extends Buffer:
  private var head: Int = 0

  abstract override def push(element: Element): Unit =
    super.push(element)

  abstract override def replaceOnFull(element: Element): Unit =
    set(head, element)
    head = (head + 1) % capacity

  abstract override def elements: List[Element] =
    if size < capacity then
      super.elements
    else
      (0 until capacity).map(i => super.elements((head + i) % capacity)).toList

trait Navigability:
  self: Buffer =>

  private var cursor: Int = 0

  def currentPosition: Int = cursor

  def currentElement: Option[Element] =
    if isEmpty then None
    else Some(elements(size - 1 - cursor))

  def resetCursor(): Unit =
    cursor = 0

  def moveForward(): Boolean =
    if cursor > 0 then
      cursor -= 1
      true
    else
      false

  def moveBackward(): Boolean =
    if cursor < size - 1 then
      cursor += 1
      true
    else
      false

object BaseBuffer:
  def apply[T: reflect.ClassTag](capacity: Int): BaseBuffer[T] =
    new BaseBuffer[T](capacity) {}

object CircularBuffer:
  def apply[T: reflect.ClassTag](capacity: Int)
      : BaseBuffer[T] with CircularBuffer =
    new BaseBuffer[T](capacity) with CircularBuffer {}

object NavigableBuffer:
  def apply[T: reflect.ClassTag](capacity: Int)
      : BaseBuffer[T] with Navigability =
    new BaseBuffer[T](capacity) with Navigability {}
