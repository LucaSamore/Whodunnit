package model.versioning

import java.time.LocalDateTime

trait Snapshot[+A]:
  def subject: A
  def createdAt: LocalDateTime

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

  private[versioning] case class SnapshotImpl[A](
      subject: A,
      createdAt: LocalDateTime
  ) extends Snapshot[A]

  object Snapshotters:
    given Snapshottable[History] with
      override def snap[T <: History](history: T): Snapshot[T] =
        SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

      override def restore[T <: History](snapshot: Snapshot[T]): T =
        snapshot.subject.deepCopy().asInstanceOf[T]
