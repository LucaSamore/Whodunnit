package model.versioning

case class Buffer[T](capacity: Int, elements: List[T]):

  def size: Int = elements.size

  def isEmpty: Boolean = elements.isEmpty
