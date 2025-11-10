package controller

import model.ModelModule

trait CluesManagementController
    extends ControllerModule.Controller

object CluesManagementController:
  def apply(model: ModelModule.Model): CluesManagementController =
    new CluesManagementControllerImpl(model)

  private class CluesManagementControllerImpl(
      model: ModelModule.Model
  ) extends ControllerModule.AbstractController(model)
      with CluesManagementController
