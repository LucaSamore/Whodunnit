package model.versioning

import model.game.History

import java.time.LocalDateTime

/** Represents an immutable snapshot of an object's state at a specific point in time.
  *
  * A snapshot captures the complete state of an object along with the timestamp of when the snapshot was created. This
  * enables time-travel functionality and state restoration.
  *
  * @tparam A
  *   the type of the object being snapshotted (covariant)
  */
trait Snapshot[+A]:
  /** The object whose state has been captured in this snapshot.
    *
    * @return
    *   the snapshotted object
    */
  def subject: A

  /** The timestamp indicating when this snapshot was created.
    *
    * @return
    *   the creation time of this snapshot
    */
  def createdAt: LocalDateTime

/** Type class that defines how to create and restore snapshots for a type.
  *
  * Implementations of this trait define the snapshot and restore logic for specific types. The type parameter is
  * contravariant, allowing a single implementation to work for a type and all its subtypes.
  *
  * @tparam S
  *   the type that can be snapshotted (contravariant)
  */
trait Snapshottable[-S]:
  /** Creates a snapshot of the given object.
    *
    * @tparam T
    *   the specific type being snapshotted (must be a subtype of S)
    * @param s
    *   the object to snapshot
    * @return
    *   a snapshot containing the state of the object
    */
  def snap[T <: S](s: T): Snapshot[T]

  /** Restores an object from a snapshot.
    *
    * @tparam T
    *   the specific type being restored (must be a subtype of S)
    * @param snapshot
    *   the snapshot to restore from
    * @return
    *   the restored object
    */
  def restore[T <: S](snapshot: Snapshot[T]): T

/** Companion object providing factory methods and utilities for creating and managing snapshots. */
object Snapshot:
  /** Creates a snapshot of the given object using its implicit Snapshottable instance.
    *
    * @tparam S
    *   the type of the object to snapshot (must have a Snapshottable instance available)
    * @param s
    *   the object to snapshot
    * @param timestamp
    *   the timestamp for the snapshot (defaults to current time)
    * @return
    *   a snapshot of the object
    */
  def apply[S: Snapshottable](
      s: S,
      timestamp: LocalDateTime = LocalDateTime.now
  ): Snapshot[S] =
    summon[Snapshottable[S]].snap(s)

  /** Restores an object from a snapshot using its implicit Snapshottable instance.
    *
    * @tparam S
    *   the type of the object to restore (must have a Snapshottable instance available)
    * @param snapshot
    *   the snapshot to restore from
    * @return
    *   the restored object
    */
  def restore[S: Snapshottable](snapshot: Snapshot[S]): S =
    summon[Snapshottable[S]].restore(snapshot)

  /** Internal implementation of the Snapshot trait.
    *
    * @tparam A
    *   the type of the subject
    * @param subject
    *   the object being snapshotted
    * @param createdAt
    *   the creation timestamp
    */
  private[versioning] case class SnapshotImpl[A](
      subject: A,
      createdAt: LocalDateTime
  ) extends Snapshot[A]

  /** Contains given instances of Snapshottable for common types. */
  object Snapshotters:
    /** Provides a Snapshottable instance for History objects.
      *
      * This implementation performs deep copies during both snapshot creation and restoration to ensure complete
      * isolation between the snapshot and the original object.
      */
    given Snapshottable[History] with
      override def snap[T <: History](history: T): Snapshot[T] =
        SnapshotImpl(history.deepCopy().asInstanceOf[T], LocalDateTime.now())

      override def restore[T <: History](snapshot: Snapshot[T]): T =
        snapshot.subject.deepCopy().asInstanceOf[T]
