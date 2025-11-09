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
      model.undo()

    override def redo(): Option[CaseKnowledgeGraph] =
      model.redo()

    override def canUndo: Boolean =
      model.state.history.exists(_.canUndo)

    override def canRedo: Boolean =
      model.state.history.exists(_.canRedo)

    override def saveSnapshot(): Unit =
      model.saveSnapshot()

    override def restoreSnapshot(): Option[CaseKnowledgeGraph] =
      model.restoreSnapshot()

    override def hasSnapshot: Boolean =
      model.hasSnapshot

    override def clearSnapshot(): Unit =
      model.clearSnapshot()
