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
      elements(size - 1) = element

  def contains(element: T): Boolean = elements.take(size).contains(element)

  def elementList: List[T] = elements.take(size).toList

case class BaseBuffer[T](
    override val capacity: Int,
    override val elements: Array[T]
) extends Buffer[T]

object BaseBuffer:
  def apply[T: reflect.ClassTag](capacity: Int): BaseBuffer[T] =
    new BaseBuffer[T](capacity, new Array[T](capacity))
