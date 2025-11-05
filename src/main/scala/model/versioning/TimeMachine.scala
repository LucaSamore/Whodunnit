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

  override def save(state: S): Unit =
    currentSnapshot = Some(Snapshot(state))

  override def restore(): Option[S] =
    currentSnapshot.map(Snapshot.restore)

  override def hasSnapshot: Boolean = currentSnapshot.isDefined

  override def clear(): Unit =
    currentSnapshot = None

  override def snapshotTime: Option[LocalDateTime] =
    currentSnapshot.flatMap(snapshot => Some(snapshot.createdAt))

object GameTimeMachine:
  def apply[S: Snapshottable](): TimeMachine[S] =
    GameTimeMachine[S](None)
