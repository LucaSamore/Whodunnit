package controller

import model.ModelModule

trait HomePageController extends ControllerModule.Controller:

  def onPlayNowClicked(): Unit

object HomePageController:
  def apply(model: ModelModule.Model): HomePageController =
    new HomePageControllerImpl(model)

  private class HomePageControllerImpl(
      model: ModelModule.Model
  ) extends ControllerModule.AbstractController(model)
      with HomePageController:

    override def onPlayNowClicked(): Unit =
      println("[Controller] Play Now button clicked!")
