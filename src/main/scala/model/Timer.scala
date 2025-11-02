package model

import scala.concurrent.duration.Duration

enum TimerState:
  case Ready
  case Running(startedAt: Long, remaining: Duration, totalDuration: Duration)
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