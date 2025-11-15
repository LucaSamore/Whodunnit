package model.game

import scala.concurrent.duration.{Duration, DurationLong}

/** Represents a timed action that executes when a specific time threshold is reached.
  *
  * Triggers fire once when the remaining time crosses their threshold. They are commonly used for timed notifications,
  * hints, or other time-based game events.
  *
  * @param firesAtRemaining
  *   the time remaining when this trigger should activate
  * @param body
  *   the action to execute when the trigger fires
  */
case class Trigger(firesAtRemaining: Duration, body: () => Unit)

/** Represents the current state of a game timer. */
enum TimerState:
  /** Timer is initialized but not yet started. */
  case Ready

  /** Timer is actively counting down.
    *
    * @param startedAt
    *   timestamp (in milliseconds) when the timer started
    * @param totalDuration
    *   the original duration set for the timer
    * @param remaining
    *   the time left before expiration
    */
  case Running(startedAt: Long, totalDuration: Duration, remaining: Duration)

  /** Timer is paused and can be resumed.
    *
    * @param totalDuration
    *   the original duration set for the timer
    * @param remaining
    *   the time left before expiration
    */
  case Paused(totalDuration: Duration, remaining: Duration)

  /** Timer has reached zero and expired. */
  case Finished

/** Pure functional logic for timer state management.
  *
  * TimerLogic provides stateless functions for timer operations, keeping the business logic separate from the stateful
  * Timer class. All functions are side-effect free.
  */
object TimerLogic:
  export TimerState.*

  /** Starts a timer with the given duration.
    *
    * @param totalDuration
    *   how long the timer should run
    * @param currentTime
    *   the current timestamp in milliseconds
    * @return
    *   a new Running state
    */
  def start(totalDuration: Duration, currentTime: Long): TimerState =
    Running(
      startedAt = currentTime,
      totalDuration = totalDuration,
      remaining = totalDuration
    )

  /** Updates the timer state based on elapsed time.
    *
    * If the timer is Running, calculates the new remaining time. If time has expired, transitions to Finished state.
    *
    * @param state
    *   the current timer state
    * @param currentTime
    *   the current timestamp in milliseconds
    * @return
    *   a tuple containing the new state and the updated remaining time (if applicable)
    */
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

  /** Formats a duration as MM:SS string.
    *
    * @param duration
    *   the duration to format
    * @return
    *   a formatted string like "05:30" (5 minutes, 30 seconds)
    */
  def formatDuration(duration: Duration): String =
    val totalSeconds = duration.toSeconds.max(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    f"$minutes%02d:$seconds%02d"

  /** Extracts the remaining time from a timer state.
    *
    * @param state
    *   the timer state to query
    * @return
    *   Some(duration) if the state has a remaining time, None for Ready state
    */
  def getRemainingTime(state: TimerState): Option[Duration] = state match
    case Running(startedAt, totalDuration, remaining) =>
      Some(remaining)
    case Paused(totalDuration, remaining) => Some(remaining)
    case Ready                            => None
    case Finished                         => Some(Duration.Zero)

  /** Identifies triggers that should fire based on time change.
    *
    * A trigger fires when the remaining time crosses its threshold from above (was greater, now is less than or equal).
    *
    * @param currentTimeRemaining
    *   the current remaining time
    * @param previousTimeRemaining
    *   the previous remaining time
    * @param triggers
    *   all available triggers
    * @return
    *   the list of triggers that should activate
    */
  def checkTriggers(
      currentTimeRemaining: Duration,
      previousTimeRemaining: Duration,
      triggers: List[Trigger]
  ): List[Trigger] =
    triggers.filter: trigger =>
      currentTimeRemaining <= trigger.firesAtRemaining &&
        previousTimeRemaining > trigger.firesAtRemaining

/** A countdown timer with trigger support for game sessions.
  *
  * Timer runs in a background thread, updating every second. It fires registered triggers when time thresholds are
  * crossed and invokes callbacks for time updates and expiration.
  *
  * @param totalDuration
  *   how long the timer should run
  * @param triggers
  *   time-based actions to execute during countdown
  * @param onTimeUpdate
  *   callback invoked every second with formatted remaining time
  * @param onTimeExpired
  *   callback invoked when timer reaches zero
  */
class TimerExecutor(
    val totalDuration: Duration,
    val triggers: List[Trigger] = List.empty,
    var onTimeUpdate: String => Unit = _ => (),
    var onTimeExpired: () => Unit = () => ()
):

  private var _state: TimerState = TimerState.Ready
  private var tickerThread: Option[Thread] = None

  /** Returns the current timer state.
    *
    * @return
    *   the current state (Ready, Running, Paused, or Finished)
    */
  def state: TimerState = _state

  /** Starts the timer countdown.
    *
    * Transitions from Ready to Running state and begins background tick processing.
    */
  def start(): Unit =
    _state = TimerLogic.start(totalDuration, System.currentTimeMillis())
    startTicker()

  /** Stops the timer immediately.
    *
    * Transitions to Finished state and terminates the background thread.
    */
  def stop(): Unit =
    _state = TimerState.Finished
    stopTicker()

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
          trigger.body()
        }

        val formattedTime = TimerLogic.formatDuration(currentTimeRemaining)
        onTimeUpdate(formattedTime)

        if _state == TimerState.Finished then
          println("[Model] Time over!")
          onTimeExpired()
          stopTicker()

      case _ => ()

  private def startTicker(): Unit =
    val thread = new Thread(() =>
      try
        while _state != TimerState.Finished do
          onTick()
          Thread.sleep(1000)
      catch
        case _: InterruptedException => ()
    )
    thread.setDaemon(true)
    thread.start()
    tickerThread = Some(thread)

  private def stopTicker(): Unit =
    tickerThread.foreach(_.interrupt())
    tickerThread = None
