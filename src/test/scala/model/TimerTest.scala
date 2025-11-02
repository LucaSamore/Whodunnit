package model

import model.TimerState.Running
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

        val (newState, remainingOpt) =
          Timer.updateTimer(initialState, currentTime)

        remainingOpt shouldBe defined
        remainingOpt.get shouldBe 7.seconds
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

        val (newState, remainingOpt) =
          Timer.updateTimer(initialState, currentTime)

        newState shouldBe Finished
        remainingOpt shouldBe Some(Duration.Zero)

      "not go below zero for remaining time" in:
        val startTime = 1000L
        val currentTime = 15000L // 14 seconds elapsed, more than duration
        val initialState = Running(
          startedAt = startTime,
          totalDuration = 10.seconds,
          remaining = 10.seconds
        )

        val (newState, remainingOpt) =
          Timer.updateTimer(initialState, currentTime)

        remainingOpt shouldBe Some(Duration.Zero)
