package view

import controller.ControllerModule
import model.ModelModule
import model.game.State

class WhodunnitLauncher
  extends ModelModule.Interface[State]
  with ControllerModule.Interface[State]
  with ViewModule.Interface[State]:

  val model: ModelModule.Model[State] = Model()
