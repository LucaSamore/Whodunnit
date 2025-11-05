package model.versioning

import java.time.LocalDateTime

trait TimeMachine[S]:
  def save(state: S): Unit
  def restore(): Option[S]
  def hasSnapshot: Boolean
  def clear(): Unit
  def snapshotTime: Option[LocalDateTime]

case class GameTimeMachine[S: Snapshottable](
    private var currentSnapshot: Option[Snapshot[S]] = None
) extends TimeMachine[S]:

  def save(state: S): Unit =
    currentSnapshot = Some(Snapshot(state))

  def restore(): Option[S] =
    currentSnapshot.map(Snapshot.restore)

  def hasSnapshot: Boolean = currentSnapshot.isDefined

  def clear(): Unit =
    currentSnapshot = None

  def snapshotTime: Option[LocalDateTime] =
    currentSnapshot.flatMap(snapshot => Some(snapshot.timestamp))

object GameTimeMachine:
  def apply[S: Snapshottable](): TimeMachine[S] =
    GameTimeMachine[S](None)
