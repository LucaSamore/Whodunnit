package controller

import model.game.*

trait HomePageController[S]
    extends ControllerModule.Controller[S]:

  def onPlayNowClicked(): Unit

object HomePageController:
  def apply[S](gameState: GameState): HomePageController[S] =
    new HomePageControllerImpl[S](gameState)

  private class HomePageControllerImpl[S](gameState: GameState)
      extends ControllerModule.AbstractController[S](gameState)
      with HomePageController[S]:

    override def onPlayNowClicked(): Unit =
      println("[Controller] Play Now button clicked!")
