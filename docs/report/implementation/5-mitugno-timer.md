# Timer

The timer system manages countdown functionality for each investigative case, providing temporal updates and triggering scheduled events at specific intervals. The implementation rigorously follows the functional core, separating pure logic from side effects.

## Architecture
The timer design separates into two distinct components with clearly defined responsibilities:
- `TimerLogic` (object): Contains all pure functional logic with no side effects. Every function is referentially transparent - given the same inputs, they always return the same outputs with no observable side effects.
- `TimerExecutor` (class): Manages mutable state and orchestrates effects (threading, callbacks, I/O).

## TimerLogic
The `TimerLogic` object exposes all timer state cases and provides pure functions for timer operations:

```scala
object TimerLogic:
  export TimerState.*
```
Each function in `TimerLogic` is a pure transformation with no side effects. The export `TimerState.*` statement brings enum cases into scope without prefixes, improving code readability in pattern matches. This is a modern approach to namespace management, selective exports for cleaner client code.

The `TimerLogic` is composed of the following functions:

`start`: Creates the initial running state from a total duration and current timestamp. This function doesn't start any timer; it computes what the initial state should be when a timer begins.

```scala
def start(totalDuration: Duration, currentTime: Long): TimerState =
  Running(
    startedAt = currentTime,
    totalDuration = totalDuration,
    remaining = totalDuration
  )
```

`updateTimer`: The core computational logic. Given a current state and timestamp, it calculates elapsed time, computes remaining duration, and determines whether the timer should transition to finished state. Returns both the new state and the updated remaining time.

```scala  
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
      (Running(startedAt, totalDuration, newRemaining), Some(newRemaining))
  case other => (other, None)
```

`formatDuration`: Transforms a duration into MM:SS format for display. Pure string transformation with no dependencies on external state.

```scala  
def formatDuration(duration: Duration): String =
  val totalSeconds = duration.toSeconds.max(0)
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  f"$minutes%02d:$seconds%02d"
```

`getRemainingTime`: Extracts remaining time from any timer state, handling all cases through pattern matching. Returns `None` for states without remaining time (Ready), `Some(Duration.Zero)` for finished timers, and the actual remaining duration for running or paused timers.

```scala 
def getRemainingTime(state: TimerState): Option[Duration] = state match
  case Running(_, _, remaining) => Some(remaining)
  case Paused(_, remaining) => Some(remaining)
  case Ready => None
  case Finished => Some(Duration.Zero)
```

`checkTriggers`: Identifies which triggers should fire based on time transition. This function doesn't execute triggers, but it only computes which ones crossed their activation threshold between two time points. Execution is delegated to the caller, keeping this function pure.

```scala 
def checkTriggers(
  currentTimeRemaining: Duration,
  previousTimeRemaining: Duration,
  triggers: List[Trigger]
): List[Trigger] =
  triggers.filter { trigger =>
    currentTimeRemaining <= trigger.firesAtRemaining &&
    previousTimeRemaining > trigger.firesAtRemaining
  }
```

## TimerState

The timer state is represented as an enum:

```scala
enum TimerState:
  case Ready
  case Running(startedAt: Long, totalDuration: Duration, remaining: Duration)
  case Paused(totalDuration: Duration, remaining: Duration)
  case Finished
```
This design makes illegal states unrepresentable through the type system. Each state has exactly the data it needs: `Ready` has no data (timer not started), `Running` has start timestamp and durations (actively counting down), `Paused` has durations but no timestamp (paused timers don't track elapsed time), `Finished` has no data (timer completed).  
The type system enforces these invariants automatically: if it compiles, the state transitions are valid. The rejected alternative was a single case class with optional fields and a string state discriminator. This would allow illegal combinations like `state = "running"` with `startedAt = None`, requiring runtime validation scattered throughout the code. With the ADT, the compiler guarantees correctness.


## TimerExecutor

The TimerExecutor class wraps the pure logic with controlled mutation and effects. The mutable state is encapsulated and private, exposed only through read-only accessors. This is a pragmatic compromise: we manage mutable state locally (necessary for a timer that evolves over time), but expose it immutably to the outside world.

```scala
class TimerExecutor(
  val totalDuration: Duration,
  val triggers: List[Trigger] = List.empty,
  var onTimeUpdate: String => Unit = _ => (),
  var onTimeExpired: () => Unit = () => ()
):
  private var _state: TimerState = TimerState.Ready
  private var tickerThread: Option[Thread] = None
  
  def state: TimerState = _state
  
  def start(): Unit =
    _state = TimerLogic.start(totalDuration, System.currentTimeMillis())
    startTicker()
  
  def stop(): Unit =
    _state = TimerState.Finished
    stopTicker()
```

The threading implementation uses Java's native threads for simplicity:

```scala
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
```

This choice prioritizes simplicity over sophistication (KISS principle). The alternative would be using effect systems (cats-effect, ZIO, Akka Streams), but that would add complexity for a straightforward use case. The timer executor doesn't need sophisticated scheduling, backpressure, or parallel execution. It's just a simple ticker every second.

The `onTick` method is the functional core of the timer ticking behavior::

```scala
private def onTick(): Unit =
  val oldRemaining = TimerLogic.getRemainingTime(_state)
  val (newState, newRemaining) = TimerLogic.updateTimer(_state, System.currentTimeMillis())
  _state = newState

  (oldRemaining, newRemaining) match
    case (Some(previousTimeRemaining), Some(currentTimeRemaining)) =>
      val activatedTriggers = TimerLogic.checkTriggers(
        currentTimeRemaining,
        previousTimeRemaining,
        triggers
      )
      activatedTriggers.foreach { trigger => trigger.body() }

      val formattedTime = TimerLogic.formatDuration(currentTimeRemaining)
      onTimeUpdate(formattedTime)

      if _state == TimerState.Finished then
        onTimeExpired()
        stopTicker()
    case _ => ()
```

This structure follows a pattern: 
- pure computation (call `TimerLogic.updateTimer` to calculate new state), 
- controlled mutation (update `_state` as a localized effect), 
- effect execution (invoke callbacks and triggers based on pure computation).  

This makes the code traceable: pure logic is tested in `TimerLogic`.

## Trigger

```scala
case class Trigger(firesAtRemaining: Duration, body: () => Unit)
```

The body field is a function value: a () => Unit (function taking no parameters, returning nothing).  
This makes triggers extremely flexible. The timer doesn't know what a trigger does, it just knows when to invoke it. 

The action is encapsulated in the function value, for example:

```scala
TimerExecutor(
  totalDuration = 30.minutes,
  triggers = List(
    Trigger(20.minutes, () => generateHint(HintKind.Helpful)),
    Trigger(10.minutes, () => generateHint(HintKind.Misleading)),
    Trigger(5.minutes, () => showWarning("Time running out!"))
  )
)
```