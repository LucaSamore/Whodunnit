package controller

import model.game.*

trait GameBoardController[S]
    extends ControllerModule.Controller[S]

object GameBoardController:
  def apply[S](gameState: GameState): GameBoardController[S] =
    new GameBoardControllerImpl[S](gameState)

  private class GameBoardControllerImpl[S](gameState: GameState)
      extends ControllerModule.AbstractController[S](gameState)
      with GameBoardController[S]
