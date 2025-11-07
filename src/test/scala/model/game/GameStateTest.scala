package model.game

import model.game.{GameState, Timer}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.TestUtils.mockCase

import scala.concurrent.duration.*

class GameStateTest extends AnyWordSpec with Matchers:

  val emptyGameState = GameState(None, None, None, None, None, None)
  val mockTimer = new Timer(3600.seconds, List.empty)
  val initializedGameState: GameState = GameState.initialize(
    mockCase,
    timer = mockTimer
  )

  "A GameState" when:
    "empty" should:
      "have all fields as None" in:
        val gameState = GameState.empty()
        gameState shouldEqual emptyGameState

    "initialized" should:
      "have all fields properly set" in:
        val gameState = GameState.initialize(mockCase, mockTimer)
        gameState shouldEqual initializedGameState

    "reset" should:
      "return an empty GameState" in:
        val resetState = initializedGameState.reset
        resetState shouldEqual emptyGameState
