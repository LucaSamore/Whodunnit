package model

import model.game.GameState

import scala.concurrent.duration.Duration

/** Module providing the Model layer of the application architecture.
  *
  * This module follows the Cake Pattern for dependency injection, providing a thread-safe implementation of the game
  * state management. The Model is responsible for maintaining and updating the application's state.
  */
object ModelModule:

  /** Interface for the application's model layer.
    *
    * Provides access to the game state and operations for updating it. All implementations should be thread-safe.
    */
  trait Model:
    /** Returns the current game state.
      *
      * @return
      *   the current GameState
      */
    def state: GameState

    /** Updates the game state using the provided updater function.
      *
      * This operation is thread-safe and atomic.
      *
      * @param updater
      *   function that transforms the current state to a new state
      * @return
      *   the updated GameState
      */
    def updateState(updater: GameState => GameState): GameState

    /** Returns the remaining time in the current game.
      *
      * @return
      *   Some(duration) if a timer exists and is active, None otherwise
      */
    def getRemainingTime: Option[Duration]

  /** Provides access to the Model instance.
    *
    * Part of the Cake Pattern for dependency injection.
    */
  trait Provider:
    def model: Model

  /** Component providing the Model implementation. */
  trait Component:

    /** Thread-safe implementation of the Model interface.
      *
      * Uses a volatile variable and synchronization to ensure thread-safe state updates.
      */
    class ModelImpl extends Model:

      @volatile private var currentState: GameState = GameState.empty()

      override def state: GameState = currentState

      override def updateState(updater: GameState => GameState): GameState =
        synchronized {
          currentState = updater(currentState)
          currentState
        }

      override def getRemainingTime: Option[Duration] =
        state.timer.flatMap { timer =>
          model.game.TimerLogic.getRemainingTime(timer.state)
        }

  /** Complete module interface combining Provider and Component.
    *
    * This trait provides a fully configured Model instance ready to use.
    */
  trait Interface extends Provider with Component:
    override lazy val model: Model = new ModelImpl()
