package model.versioning

import java.time.LocalDateTime

trait Snapshot[+A]:
  def subject: A
  def timestamp: LocalDateTime

// TODO: Togliere il timestamp da Snaphottable
trait Snapshottable[S]:
  def snap(s: S): Snapshot[S]
  def restore(snapshot: Snapshot[S]): S

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
      def snap(value: Int): Snapshot[Int] =
        SnapshotImpl(value, LocalDateTime.now())

      def restore(snapshot: Snapshot[Int]): Int =
        snapshot.subject

    given Snapshottable[String] with
      def snap(value: String): Snapshot[String] =
        SnapshotImpl(value, LocalDateTime.now())

      def restore(snapshot: Snapshot[String]): String =
        snapshot.subject

    given Snapshottable[ImmutableHistory] with
      def snap(history: ImmutableHistory): Snapshot[ImmutableHistory] =
        SnapshotImpl(history.deepCopy(), LocalDateTime.now())

      def restore(snapshot: Snapshot[ImmutableHistory]): ImmutableHistory =
        snapshot.subject.deepCopy()

    given Snapshottable[MutableHistory] with
      def snap(history: MutableHistory): Snapshot[MutableHistory] =
        SnapshotImpl(history.deepCopy(), LocalDateTime.now())

      def restore(snapshot: Snapshot[MutableHistory]): MutableHistory =
        snapshot.subject.deepCopy()

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
