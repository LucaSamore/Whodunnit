package model

import model.game.GameState

import scala.concurrent.duration.Duration

object ModelModule:

  trait Model:
    def state: GameState
    def updateState(updater: GameState => GameState): GameState
    def startTimer(): Unit
    def getRemainingTime: Option[Duration]

  trait Provider:
    def model: Model

  trait Component:

    class ModelImpl extends Model:

      @volatile private var currentState: GameState = GameState.empty()

      override def state: GameState = currentState

      override def updateState(updater: GameState => GameState): GameState =
        synchronized {
          currentState = updater(currentState)
          currentState
        }

      override def startTimer(): Unit =
        synchronized {
          currentState.timer.foreach(_.start())
        }

      override def getRemainingTime: Option[Duration] =
        state.timer.flatMap { timer =>
          model.game.TimerLogic.getRemainingTime(timer.state)
        }

  trait Interface extends Provider with Component:
    override lazy val model: Model = new ModelImpl()
