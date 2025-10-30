package model.versioning

case class Buffer[T](capacity: Int, elements: Array[T]):
  private var size: Int = 0

  def currentSize: Int = size

  def isEmpty: Boolean = size == 0

  def add(element: T): Unit = {
    elements(size) = element
    size += 1
  }

  def contains(element: T): Boolean = elements.contains(element)
