package controller

import model.ModelModule
import model.game.CaseKnowledgeGraph

trait GameBoardController extends ControllerModule.Controller:
  def undo(): Option[CaseKnowledgeGraph]
  def redo(): Option[CaseKnowledgeGraph]
  def canUndo: Boolean
  def canRedo: Boolean
  def saveSnapshot(): Unit
  def restoreSnapshot(): Option[CaseKnowledgeGraph]
  def hasSnapshot: Boolean
  def clearSnapshot(): Unit

object GameBoardController:
  def apply(model: ModelModule.Model): GameBoardController =
    new GameBoardControllerImpl(model)

  private class GameBoardControllerImpl(
      model: ModelModule.Model
  ) extends ControllerModule.AbstractController(model)
      with GameBoardController:

    override def undo(): Option[CaseKnowledgeGraph] =
      model.state.history.flatMap { history =>
        val (newHistory, previousGraph) = history.undo()
        previousGraph.map { graph =>
          model.updateState(_.withHistory(newHistory))
          graph
        }
      }

    override def redo(): Option[CaseKnowledgeGraph] =
      model.state.history.flatMap { history =>
        val (newHistory, nextGraph) = history.redo()
        nextGraph.map { graph =>
          model.updateState(_.withHistory(newHistory))
          graph
        }
      }

    override def canUndo: Boolean =
      model.state.history.exists(_.canUndo)

    override def canRedo: Boolean =
      model.state.history.exists(_.canRedo)

    def saveSnapshot(): Unit =
      model.state.history.foreach { history =>
        model.state.timeMachine.foreach { tm =>
          tm.save(history)
        }
      }

    def restoreSnapshot(): Option[CaseKnowledgeGraph] =
      model.state.timeMachine.flatMap { tm =>
        tm.restore().map { restoredHistory =>
          model.updateState(_.withHistory(restoredHistory))
          tm.clear()
          restoredHistory.currentState.getOrElse(
            new CaseKnowledgeGraph()
          )
        }
      }

    def hasSnapshot: Boolean =
      model.state.timeMachine.exists(_.hasSnapshot)

    def clearSnapshot(): Unit =
      model.state.timeMachine.foreach(_.clear())
