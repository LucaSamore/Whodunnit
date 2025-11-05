package view

import controller.ControllerModule
import model.ModelModule
import model.State

class WhodunnitLauncher
    extends ModelModule.Interface[State]
    with ControllerModule.Interface[State]
    with ViewModule.Interface[State]:

  val model: ModelModule.Model[State] = Model()
  val controller: ControllerModule.Controller[State] = Controller()
  val view: ViewModule.View[State] = View()
