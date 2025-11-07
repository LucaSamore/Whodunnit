package model.versioning

import model.versioning.Snapshot.SnapshotImpl

import java.time.LocalDateTime

case class ImmutableHistory(elements: List[Int]):
  def add(element: Int): ImmutableHistory =
    ImmutableHistory(elements :+ element)

  def deepCopy(): ImmutableHistory =
    ImmutableHistory(elements.map(identity))

case class MutableHistory(elements: Array[Int], var size: Int = 0):
  def add(element: Int): Unit =
    elements(size) = element
    size += 1

  def deepCopy(): MutableHistory =
    MutableHistory(elements.slice(0, size), size)

object TestSnapshotters:
  given Snapshottable[Int] with
    override def snap[T <: Int](value: T): Snapshot[T] =
      SnapshotImpl(value, LocalDateTime.now())

    override def restore[T <: Int](snapshot: Snapshot[T]): T =
      snapshot.subject

  given Snapshottable[String] with
    override def snap[T <: String](value: T): Snapshot[T] =
      SnapshotImpl(value, LocalDateTime.now())

    override def restore[T <: String](snapshot: Snapshot[T]): T =
      snapshot.subject

  given Snapshottable[ImmutableHistory] with
    override def snap[T <: ImmutableHistory](history: T): Snapshot[T] =
      SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

    override def restore[T <: ImmutableHistory](snapshot: Snapshot[T]): T =
      snapshot.subject.deepCopy().asInstanceOf[T]

  given Snapshottable[MutableHistory] with
    override def snap[T <: MutableHistory](history: T): Snapshot[T] =
      SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

    override def restore[T <: MutableHistory](snapshot: Snapshot[T]): T =
      snapshot.subject.deepCopy().asInstanceOf[T]
