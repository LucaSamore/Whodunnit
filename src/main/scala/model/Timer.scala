package model

import scala.concurrent.duration.{Duration, DurationLong}

case class TriggerEvent(triggerAtRemaining: Duration, message: String)

enum TimerState:
  case Ready
  case Running(startedAt: Long, totalDuration: Duration, remaining: Duration)
  case Paused(totalDuration: Duration, remaining: Duration)
  case Finished

object TimerLogic:
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

  def getRemainingTime(state: TimerState): Option[Duration] = state match
    case Running(startedAt, totalDuration, remaining) =>
      Some(remaining)
    case Paused(totalDuration, remaining) => Some(remaining)
    case Ready                            => None
    case Finished                         => Some(Duration.Zero)

  def checkTriggers(
      currentTimeRemaining: Duration,
      previousTimeRemaining: Duration,
      triggers: List[TriggerEvent]
  ): List[TriggerEvent] =
    triggers.filter: trigger =>
      currentTimeRemaining <= trigger.triggerAtRemaining &&
        previousTimeRemaining > trigger.triggerAtRemaining

class Timer(
    val totalDuration: Duration,
    val triggers: List[TriggerEvent] = List.empty
):

  private var _state: TimerState = TimerState.Ready
  private var tickerThread: Option[Thread] = None

  def state: TimerState = _state

  def start(): Unit =
    _state = TimerLogic.start(totalDuration, System.currentTimeMillis())
    startTicker()

  private def onTick(): Unit =
    val oldRemaining = TimerLogic.getRemainingTime(_state)
    val (newState, newRemaining) =
      TimerLogic.updateTimer(_state, System.currentTimeMillis())
    _state = newState

    (oldRemaining, newRemaining) match
      case (Some(previousTimeRemaining), Some(currentTimeRemaining)) =>
        val activatedTriggers = TimerLogic.checkTriggers(
          currentTimeRemaining,
          previousTimeRemaining,
          triggers
        )
        activatedTriggers.foreach { trigger =>
          println(s"\n ${trigger.message}")
        }

        displayCurrentTime()

        if _state == TimerState.Finished then
          println("\n Time over!")
          stopTicker()

      case _ => ()

  private def displayCurrentTime(): Unit =
    val remaining = TimerLogic.getRemainingTime(_state) match
      case Some(remaining) =>
        val formatted = TimerLogic.formatDuration(remaining)
        println(s"️$formatted")
      case None => None

  private def startTicker(): Unit =
    val thread = new Thread(() => {
      while _state != TimerState.Finished do
        onTick()
        Thread.sleep(1000)
    })
    thread.setDaemon(true)
    thread.start()
    tickerThread = Some(thread)

  private def stopTicker(): Unit =
    tickerThread.foreach(_.interrupt())
    tickerThread = None
