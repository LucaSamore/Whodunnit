package model.versioning

import java.time.LocalDateTime

trait Snapshot[+A]:
  def subject: A
  def timestamp: LocalDateTime

trait Snapshottable[-S]:
  def snap[T <: S](s: T): Snapshot[T]
  def restore[T <: S](snapshot: Snapshot[T]): T

object Snapshot:
  def apply[S: Snapshottable](
      s: S,
      timestamp: LocalDateTime = LocalDateTime.now
  ): Snapshot[S] =
    summon[Snapshottable[S]].snap(s)

  def restore[S: Snapshottable](snapshot: Snapshot[S]): S =
    summon[Snapshottable[S]].restore(snapshot)

  private case class SnapshotImpl[A](subject: A, timestamp: LocalDateTime)
      extends Snapshot[A]

  object Snapshotters:
    given Snapshottable[Int] with
      def snap[T <: Int](value: T): Snapshot[T] =
        SnapshotImpl(value, LocalDateTime.now())

      def restore[T <: Int](snapshot: Snapshot[T]): T =
        snapshot.subject

    given Snapshottable[String] with
      def snap[T <: String](value: T): Snapshot[T] =
        SnapshotImpl(value, LocalDateTime.now())

      def restore[T <: String](snapshot: Snapshot[T]): T =
        snapshot.subject

    given Snapshottable[ImmutableHistory] with
      def snap[T <: ImmutableHistory](history: T): Snapshot[T] =
        SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

      def restore[T <: ImmutableHistory](snapshot: Snapshot[T]): T =
        snapshot.subject.deepCopy().asInstanceOf[T]

    given Snapshottable[MutableHistory] with
      def snap[T <: MutableHistory](history: T): Snapshot[T] =
        SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

      def restore[T <: MutableHistory](snapshot: Snapshot[T]): T =
        snapshot.subject.deepCopy().asInstanceOf[T]

    given Snapshottable[History] with
      def snap[T <: History](history: T): Snapshot[T] =
        SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

      def restore[T <: History](snapshot: Snapshot[T]): T =
        snapshot.subject.deepCopy().asInstanceOf[T]

case class ImmutableHistory(elements: List[Int]) {
  def add(element: Int): ImmutableHistory = {
    ImmutableHistory(elements :+ element)
  }

  def deepCopy(): ImmutableHistory = {
    ImmutableHistory(elements.map(identity))
  }
}

case class MutableHistory(elements: Array[Int], var size: Int = 0) {
  def add(element: Int): Unit = {
    elements(size) = element
    size += 1
  }

  def deepCopy(): MutableHistory = {
    MutableHistory(elements.slice(0, size), size)
  }
}
