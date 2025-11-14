package model.game

import model.versioning.{Snapshot, Snapshottable}

import java.time.LocalDateTime

/** Provides save and restore functionality for game state.
  *
  * TimeMachine allows capturing a complete snapshot of a state at a specific point in time and later restoring that
  * exact state. This enables features like save points, checkpoints, or full game state restoration.
  *
  * @tparam S
  *   the type of state that can be saved and restored
  */
trait TimeMachine[S]:
  /** Saves the current state as a snapshot.
    *
    * If a snapshot already exists, it will be replaced by the new one.
    *
    * @param state
    *   the state to save
    */
  def save(state: S): Unit

  /** Restores the state from the saved snapshot.
    *
    * @return
    *   Some(state) if a snapshot exists, None otherwise
    */
  def restore(): Option[S]

  /** Checks if a snapshot has been saved.
    *
    * @return
    *   true if a snapshot exists, false otherwise
    */
  def hasSnapshot: Boolean

  /** Clears the saved snapshot.
    *
    * After calling this method, hasSnapshot will return false and restore will return None.
    */
  def clear(): Unit

  /** Returns the timestamp of when the current snapshot was created.
    *
    * @return
    *   Some(timestamp) if a snapshot exists, None otherwise
    */
  def snapshotTime: Option[LocalDateTime]

/** Implementation of TimeMachine for game state management.
  *
  * GameTimeMachine stores a single snapshot at a time. Saving a new state replaces any existing snapshot.
  *
  * @tparam S
  *   the type of state to manage (must have a Snapshottable instance)
  * @param currentSnapshot
  *   the current saved snapshot, if any
  */
case class GameTimeMachine[S: Snapshottable](
    private var currentSnapshot: Option[Snapshot[S]] = None
) extends TimeMachine[S]:

  override def save(state: S): Unit =
    currentSnapshot = Some(Snapshot(state))

  override def restore(): Option[S] =
    currentSnapshot.map(Snapshot.restore)

  override def hasSnapshot: Boolean = currentSnapshot.isDefined

  override def clear(): Unit =
    currentSnapshot = None

  override def snapshotTime: Option[LocalDateTime] =
    currentSnapshot.flatMap(snapshot => Some(snapshot.createdAt))

/** Companion object providing factory methods for creating GameTimeMachine instances. */
object GameTimeMachine:
  /** Creates a new empty GameTimeMachine with no saved snapshot.
    *
    * @tparam S
    *   the type of state to manage (must have a Snapshottable instance)
    * @return
    *   a new GameTimeMachine instance
    */
  def apply[S: Snapshottable](): TimeMachine[S] =
    GameTimeMachine[S](None)
