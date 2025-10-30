package model.versioning

import java.time.LocalDateTime

trait Snapshot[+A]:
  def subject: A

trait Snapshottable[S]:
  def snap(s: S, timestamp: LocalDateTime): Snapshot[S]
  def restore(snapshot: Snapshot[S]): S

object Snapshot:
  def apply[S: Snapshottable](
      s: S,
      timestamp: LocalDateTime = LocalDateTime.now
  ): Snapshot[S] =
    summon[Snapshottable[S]].snap(s, timestamp)

  def restore[S: Snapshottable](snapshot: Snapshot[S]): S =
    summon[Snapshottable[S]].restore(snapshot)

  private case class SnapshotImpl[A](
      override val subject: A,
      timestamp: LocalDateTime
  ) extends Snapshot[A]

  object Snapshotters:
    given Snapshottable[Int] with
      def snap(value: Int, timestamp: LocalDateTime): Snapshot[Int] =
        SnapshotImpl(value, timestamp)

      def restore(snapshot: Snapshot[Int]): Int =
        snapshot.subject

    given Snapshottable[String] with
      def snap(value: String, timestamp: LocalDateTime): Snapshot[String] =
        SnapshotImpl(value, timestamp)

      def restore(snapshot: Snapshot[String]): String =
        snapshot.subject

    given Snapshottable[History] with
      def snap(history: History, timestamp: LocalDateTime): Snapshot[History] =
        SnapshotImpl(history.deepCopy(), timestamp)

      def restore(snapshot: Snapshot[History]): History =
        snapshot.subject.deepCopy()

    given Snapshottable[MutableHistory] with
      def snap(
          history: MutableHistory,
          timestamp: LocalDateTime
      ): Snapshot[MutableHistory] =
        SnapshotImpl(history.deepCopy(), timestamp)

      def restore(snapshot: Snapshot[MutableHistory]): MutableHistory =
        snapshot.subject.deepCopy()

case class History(elements: List[Int]) {
  def add(element: Int): History = {
    History(elements :+ element)
  }

  def deepCopy(): History = {
    History(elements.map(identity))
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
