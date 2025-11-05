package model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import model.casegeneration.TestUtils.mockCase

class GameStateTest extends AnyWordSpec with Matchers:

  val emptyGameState = GameState(None, None, None, None, None, None)
  val initializedGameState: GameState = GameState.initialize(
    mockCase,
    timer = 60
  )

  "A GameState" when:
    "empty" should:
      "have all fields as None" in:
        val gameState = GameState.empty()
        gameState shouldEqual emptyGameState

      "initialized" should:
        "have all fields properly set" in:
          val gameState = GameState.initialize(mockCase, timer = 60)
          gameState shouldEqual initializedGameState

      "reset" should:
        "return an empty GameState" in:
          val resetState = initializedGameState.reset
          resetState shouldEqual emptyGameState
