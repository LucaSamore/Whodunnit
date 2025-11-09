package model.game

import model.game.TimerState.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration.*

class TimerTest extends AnyWordSpec with Matchers:

  "TimerLogic" when:
    "starting a timer" should:
      "create a Running state with the given current time" in:
        val currentTime = 1000L
        val duration = 10.seconds

        val state =
          TimerLogic.start(totalDuration = duration, currentTime = currentTime)

        state shouldBe Running(
          startedAt = currentTime,
          totalDuration = duration,
          remaining = duration
        )

    "updating a Running timer" should:
      val startTime = 1000L
      val total = 10.seconds
      val initial = Running(
        startedAt = startTime,
        totalDuration = total,
        remaining = total
      )

      "calculate the correct remaining time based on elapsed time" in:
        val currentTime = 4000L
        val (newState, remaining) = TimerLogic.updateTimer(initial, currentTime)

        remaining.get shouldBe 7.seconds
        newState shouldBe Running(startTime, total, 7.seconds)

      "transition to Finished when time expires" in:
        val currentTime = 11000L
        val (newState, remaining) = TimerLogic.updateTimer(initial, currentTime)

        newState shouldBe Finished
        remaining shouldBe Some(Duration.Zero)

      "not go below zero for remaining time" in:
        val currentTime = 15000L
        val (newState, remaining) = TimerLogic.updateTimer(initial, currentTime)

        newState shouldBe Finished
        remaining shouldBe Some(Duration.Zero)

    "formatting duration" should:
      "format zero seconds correctly" in:
        val duration = 0.seconds
        TimerLogic.formatDuration(duration) shouldBe "00:00"

      "format seconds correctly" in:
        val duration = 45.seconds
        TimerLogic.formatDuration(duration) shouldBe "00:45"

      "format minutes and seconds correctly" in:
        val duration = 125.seconds
        TimerLogic.formatDuration(duration) shouldBe "02:05"

      "format exact minutes correctly" in:
        val duration = 3.minutes
        TimerLogic.formatDuration(duration) shouldBe "03:00"

    "getting remaining time" should:
      "return None for Ready state" in:
        val state = Ready
        TimerLogic.getRemainingTime(state) shouldBe None

      "return Some(Duration.Zero) for Finished state" in:
        val state = Finished
        TimerLogic.getRemainingTime(state) shouldBe Some(Duration.Zero)

      "return remaining time for Running state" in:
        val state = Running(
          startedAt = 1000L,
          totalDuration = 10.seconds,
          remaining = 7.seconds
        )
        TimerLogic.getRemainingTime(state) shouldBe Some(7.seconds)

      "return remaining time for Paused state" in:
        val state = Paused(
          totalDuration = 10.seconds,
          remaining = 5.seconds
        )
        TimerLogic.getRemainingTime(state) shouldBe Some(5.seconds)

    "checking triggers" should:
      val trigger6 = TriggerEvent(6.seconds, () => println("6 seconds remaining!"))
      val trigger5 = TriggerEvent(5.seconds, () => println("5 seconds remaining!"))
      val trigger3 = TriggerEvent(3.seconds, () => println("3 seconds remaining!"))
      val triggers = List(trigger6, trigger3)

      "return empty list when no triggers are defined" in:
        val current = 5.seconds
        val previous = 8.seconds

        val activated = TimerLogic.checkTriggers(current, previous, Nil)
        activated shouldBe empty

      "activate trigger when crossing the threshold" in:
        val current = 5.seconds
        val previous = 7.seconds

        val activated =
          TimerLogic.checkTriggers(current, previous, List(trigger6))
        activated should contain only trigger6

      "not activate trigger when not crossing threshold" in:
        val current = 7.seconds
        val previous = 8.seconds

        val activated =
          TimerLogic.checkTriggers(current, previous, List(trigger6))
        activated shouldBe empty

      "activate trigger when current time equals trigger time" in:
        val current = 5.seconds
        val previous = 6.seconds

        val activated =
          TimerLogic.checkTriggers(current, previous, List(trigger5))

        activated should contain only trigger5

      "not activate trigger if already past it" in:
        val current = 4.seconds
        val previous = 5.seconds

        val activated =
          TimerLogic.checkTriggers(current, previous, List(trigger6))

        activated shouldBe empty

      "activate only crossed triggers when multiple are defined" in:
        val current = 5.seconds
        val previous = 7.seconds

        val activated = TimerLogic.checkTriggers(current, previous, triggers)

        activated should contain only trigger6

  "Timer" when:
    val timer = Timer(totalDuration = 10.seconds)
    timer.start()

    "starting" should:
      "transition from Ready to Running state" in:
        val timer1 = Timer(totalDuration = 10.seconds)

        timer1.state shouldBe TimerState.Ready

        timer1.start()

        timer1.state match
          case TimerState.Running(startedAt, totalDuration, remaining) =>
            totalDuration shouldBe 10.seconds
            startedAt should be > 0L
          case other => fail(s"Expected Running state, got $other")

      "maintain Running state immediately after start" in:
        timer.state shouldBe a[TimerState.Running]

    "ticking" should:
      "decrease remaining time progressively" in:
        val state1 = timer.state
        Thread.sleep(2000)
        val state2 = timer.state
        Thread.sleep(2000)
        val state3 = timer.state

        (state1, state2, state3) match
          case (
                TimerState.Running(_, _, remainingTime1),
                TimerState.Running(_, _, remainingTime2),
                TimerState.Running(_, _, remainingTime3)
              ) =>
            remainingTime1 should be > remainingTime2
            remainingTime2 should be > remainingTime3

          case _ => fail("Timer should remain in Running state")

      "maintain Running state while time remains" in:
        Thread.sleep(1000)
        timer.state shouldBe a[TimerState.Running]
        Thread.sleep(1000)
        timer.state shouldBe a[TimerState.Running]

    "completing" should:
      "transition to Finished state when time expires" in:
        val timer3 = Timer(totalDuration = 5.seconds)
        timer3.start()
        timer3.state shouldBe a[TimerState.Running]
        Thread.sleep(6000)
        timer3.state shouldBe TimerState.Finished

      "have zero remaining time when Finished" in:
        val timer4 = Timer(totalDuration = 5.seconds)
        timer4.start()
        Thread.sleep(6000)
        timer4.state shouldBe TimerState.Finished
        TimerLogic.getRemainingTime(timer4.state) shouldBe Some(Duration.Zero)
