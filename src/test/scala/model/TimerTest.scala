package model

import model.TimerState.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration.*

class TimerTest extends AnyWordSpec with Matchers:

  "Timer" when:
    "starting a timer" should:
      "create a Running state with the given current time" in:
        val currentTime = 1000L
        val duration = 10.seconds

        val state =
          Timer.start(totalDuration = duration, currentTime = currentTime)

        state shouldBe Running(
          startedAt = currentTime,
          totalDuration = duration,
          remaining = duration
        )

    "updating a Running timer" should:
      "calculate the correct remaining time based on elapsed time" in:
        val startTime = 1000L
        val currentTime = 4000L // 3 seconds elapsed
        val initialState = Running(
          startedAt = startTime,
          totalDuration = 10.seconds,
          remaining = 10.seconds
        )

        val (newState, remainingTime) =
          Timer.updateTimer(initialState, currentTime)

        remainingTime.get shouldBe 7.seconds
        newState shouldBe Running(
          startedAt = startTime,
          totalDuration = 10.seconds,
          remaining = 7.seconds
        )

      "transition to Finished when time expires" in:
        val startTime = 1000L
        val currentTime = 11000L // 10 seconds elapsed
        val initialState = Running(
          startedAt = startTime,
          totalDuration = 10.seconds,
          remaining = 10.seconds
        )

        val (newState, remainingTime) =
          Timer.updateTimer(initialState, currentTime)

        newState shouldBe Finished
        remainingTime shouldBe Some(Duration.Zero)

      "not go below zero for remaining time" in:
        val startTime = 1000L
        val currentTime = 15000L // 14 seconds elapsed, more than duration
        val initialState = Running(
          startedAt = startTime,
          totalDuration = 10.seconds,
          remaining = 10.seconds
        )

        val (newState, remainingTime) =
          Timer.updateTimer(initialState, currentTime)

        newState shouldBe Finished
        remainingTime shouldBe Some(Duration.Zero)

    "formatting duration" should:
      "format zero seconds correctly" in:
        val duration = 0.seconds
        Timer.formatDuration(duration) shouldBe "00:00"

      "format seconds correctly" in:
        val duration = 45.seconds
        Timer.formatDuration(duration) shouldBe "00:45"

      "format minutes and seconds correctly" in:
        val duration = 125.seconds
        Timer.formatDuration(duration) shouldBe "02:05"

      "format exact minutes correctly" in:
        val duration = 3.minutes
        Timer.formatDuration(duration) shouldBe "03:00"

    "getting remaining time" should:
      "return None for Ready state" in:
        val state = Ready
        Timer.getRemainingTime(state) shouldBe None

      "return Some(Duration.Zero) for Finished state" in:
        val state = Finished
        Timer.getRemainingTime(state) shouldBe Some(Duration.Zero)

      "return remaining time for Running state" in:
        val state = Running(
          startedAt = 1000L,
          totalDuration = 10.seconds,
          remaining = 7.seconds
        )
        Timer.getRemainingTime(state) shouldBe Some(7.seconds)

      "return remaining time for Paused state" in:
        val state = Paused(
          totalDuration = 10.seconds,
          remaining = 5.seconds
        )
        Timer.getRemainingTime(state) shouldBe Some(5.seconds)

    "checking triggers" should:
      "return empty list when no triggers are defined" in:
        val triggers = List.empty[TriggerEvent]
        val current = 5.seconds
        val previous = 8.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated shouldBe empty

      "activate trigger when crossing the threshold" in:
        val trigger = TriggerEvent(6.seconds, "6 seconds remaining!")
        val triggers = List(trigger)
        val current = 5.seconds
        val previous = 7.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated should contain only trigger

      "not activate trigger when not crossing threshold" in:
        val trigger = TriggerEvent(6.seconds, "6 seconds remaining!")
        val triggers = List(trigger)
        val current = 7.seconds
        val previous = 8.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated shouldBe empty

      "activate trigger when current time equals trigger time" in:
        val trigger = TriggerEvent(5.seconds, "5 seconds remaining!")
        val triggers = List(trigger)
        val current = 5.seconds
        val previous = 6.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated should contain only trigger

      "not activate trigger if already past it" in:
        val trigger = TriggerEvent(6.seconds, "6 seconds remaining!")
        val triggers = List(trigger)
        val current = 4.seconds
        val previous = 5.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated shouldBe empty

      "activate only crossed triggers when multiple are defined" in:
        val trigger1 = TriggerEvent(6.seconds, "6 seconds!")
        val trigger2 = TriggerEvent(3.seconds, "3 seconds!")
        val triggers = List(trigger1, trigger2)
        val current = 5.seconds
        val previous = 7.seconds

        val activated = Timer.checkTriggers(current, previous, triggers)

        activated should contain only trigger1