package model.versioning

abstract class Buffer[T]:
  protected val capacity: Int
  protected val elements: Array[T]
  protected var size: Int = 0

  def currentSize: Int = size

  def isEmpty: Boolean = size == 0

  def add(element: T): Unit =
    if size < capacity then
      elements(size) = element
      size += 1
    else
      replaceElement(element)

  protected def replaceElement(element: T): Unit =
    elements(size - 1) = element

  def contains(element: T): Boolean = elements.take(size).contains(element)

  def elementList: List[T] = elements.take(size).toList

case class BaseBuffer[T](
    override val capacity: Int,
    override val elements: Array[T]
) extends Buffer[T]

class CircularBuffer[T](
    override val capacity: Int,
    override val elements: Array[T]
) extends Buffer[T]:
  private var head: Int = 0

  override protected def replaceElement(element: T): Unit =
    elements(head) = element
    head = (head + 1) % capacity

  override def elementList: List[T] =
    if size < capacity then
      elements.take(size).toList
    else
      (0 until capacity).map(i => elements((head + i) % capacity)).toList

object BaseBuffer:
  def apply[T: reflect.ClassTag](capacity: Int): BaseBuffer[T] =
    new BaseBuffer[T](capacity, new Array[T](capacity))

object CircularBuffer:
  def apply[T: reflect.ClassTag](capacity: Int): CircularBuffer[T] =
    new CircularBuffer[T](capacity, new Array[T](capacity))
