package model

import model.game.GameState

object ModelModule:

  trait Model:
    def state: GameState
    def updateState(updater: GameState => GameState): GameState
    def startTimer(): Unit
    def resetState(): GameState

    def addHint(hint: game.Hint): GameState =
      updateState(_.addHint(hint))

    def updateHistory(f: game.History => game.History): GameState =
      updateState(_.updateHistory(f))

    def addGraphToHistory(graph: game.CaseKnowledgeGraph): GameState =
      updateState(_.addGraphToHistory(graph))

    def undo(): Option[game.CaseKnowledgeGraph] =
      state.history.flatMap { history =>
        val (newHistory, previousGraph) = history.undo()
        previousGraph.map { graph =>
          updateState(_.withHistory(newHistory))
          graph
        }
      }

    def redo(): Option[game.CaseKnowledgeGraph] =
      state.history.flatMap { history =>
        val (newHistory, nextGraph) = history.redo()
        nextGraph.map { graph =>
          updateState(_.withHistory(newHistory))
          graph
        }
      }

    def saveSnapshot(): Unit =
      state.history.foreach { history =>
        state.timeMachine.foreach { tm =>
          tm.save(history)
        }
      }

    def restoreSnapshot(): Option[game.CaseKnowledgeGraph] =
      state.timeMachine.flatMap { tm =>
        tm.restore().map { restoredHistory =>
          updateState(_.withHistory(restoredHistory))
          tm.clear()
          restoredHistory.currentState.getOrElse(
            new game.CaseKnowledgeGraph()
          )
        }
      }

    def hasSnapshot: Boolean =
      state.timeMachine.exists(_.hasSnapshot)

    def clearSnapshot(): Unit =
      state.timeMachine.foreach(_.clear())

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

      override def resetState(): GameState =
        synchronized {
          currentState = GameState.empty()
          currentState
        }

      override def startTimer(): Unit =
        synchronized {
          currentState.timer.foreach(_.start())
        }

  trait Interface extends Provider with Component:
    override lazy val model: Model = new ModelImpl()
