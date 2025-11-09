package controller

import model.ModelModule

trait GameBoardController extends ControllerModule.Controller

object GameBoardController:
  def apply(model: ModelModule.Model): GameBoardController =
    new GameBoardControllerImpl(model)

  private class GameBoardControllerImpl(
      model: ModelModule.Model
  ) extends ControllerModule.AbstractController(model)
      with GameBoardController
