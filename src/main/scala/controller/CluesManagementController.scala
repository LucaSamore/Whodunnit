package controller

import model.game.*

trait CluesManagementController[S]
  extends ControllerModule.Controller[S]

object CluesManagementController:
  def apply[S](gameState: GameState): CluesManagementController[S] =
    new CluesManagementControllerImpl[S](gameState)

  private class CluesManagementControllerImpl[S](gameState: GameState)
    extends ControllerModule.AbstractController[S](gameState)
      with CluesManagementController[S]
