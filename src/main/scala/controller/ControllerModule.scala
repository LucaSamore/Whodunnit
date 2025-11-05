package controller

object ControllerModule:

  trait Controller[S]:
    def onPlayNowClicked(): Unit

  trait Provider[S]:
    val controller: Controller[S]

  type Requirements[S] =
    view.ViewModule.Provider[S] with model.ModelModule.Provider[S]

  trait Component[S]:
    context: Requirements[S] =>

    class ControllerImpl extends Controller[S]:
      def onPlayNowClicked(): Unit =
        println("Play Now button clicked!")

  trait Interface[S] extends Provider[S] with Component[S]:
    self: Requirements[S] =>
    def Controller(): Controller[S] = new ControllerImpl()
