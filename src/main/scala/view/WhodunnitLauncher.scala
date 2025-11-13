package view

import controller.ControllerModule
import model.ModelModule

/** Application launcher that assembles all modules using the Cake Pattern.
  *
  * This class wires together the Model, Controller, and View modules to create a fully configured application. It
  * serves as the composition root where all dependencies are resolved and connected.
  */
class WhodunnitLauncher
    extends ModelModule.Interface
    with ControllerModule.Interface
    with ViewModule.Interface
