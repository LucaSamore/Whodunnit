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
