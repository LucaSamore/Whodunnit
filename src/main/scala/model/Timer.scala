package model

import scala.concurrent.duration.{Duration, DurationLong}

enum TimerState:
  case Ready
  case Running(startedAt: Long, totalDuration: Duration, remaining: Duration)
  case Paused(totalDuration: Duration, remaining: Duration)
  case Finished

object Timer:
  export TimerState.*

  def start(totalDuration: Duration, currentTime: Long): TimerState =
    Running(
      startedAt = currentTime,
      totalDuration = totalDuration,
      remaining = totalDuration
    )

  def updateTimer(
      state: TimerState,
      currentTime: Long
  ): (TimerState, Option[Duration]) = state match
    case Running(startedAt, totalDuration, remaining) =>
      val elapsed = (currentTime - startedAt).millis
      val newRemaining = (totalDuration - elapsed).max(Duration.Zero)

      if newRemaining <= Duration.Zero then
        (Finished, Some(Duration.Zero))
      else
        val newState = Running(startedAt, totalDuration, newRemaining)
        (newState, Some(newRemaining))
    case other => (other, None)

  def formatDuration(duration: Duration): String =
    val totalSeconds = duration.toSeconds.max(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    f"$minutes%02d:$seconds%02d"
