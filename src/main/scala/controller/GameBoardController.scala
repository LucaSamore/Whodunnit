package controller

import model.ModelModule
import model.game.ValidationResult.{CorrectSolution, IncorrectSolution}
import model.game.{CaseKnowledgeGraph, CaseRole, Character, SolutionConfig, SubmissionState, ValidationResult}
import model.hint.Metric.coverage

trait GameBoardController extends ControllerModule.Controller:
  def undo(): Option[CaseKnowledgeGraph]
  def redo(): Option[CaseKnowledgeGraph]
  def canUndo: Boolean
  def canRedo: Boolean
  def saveSnapshot(): Unit
  def restoreSnapshot(): Option[CaseKnowledgeGraph]
  def hasSnapshot: Boolean
  def clearSnapshot(): Unit
  def canAccuse: Boolean
  def getAvailableSuspects: Set[Character]
  def submitAccusation(character: Character): ValidationResult

object GameBoardController:
  def apply(model: ModelModule.Model): GameBoardController = new GameBoardControllerImpl(model)

  private final class GameBoardControllerImpl(model: ModelModule.Model)
      extends ControllerModule.AbstractController(model) with GameBoardController:

    override def undo(): Option[CaseKnowledgeGraph] = model.state.history.flatMap { history =>
      val (newHistory, previousGraph) = history.undo()
      previousGraph.map { graph =>
        model.updateState(_.withHistory(newHistory))
        graph
      }
    }

    override def redo(): Option[CaseKnowledgeGraph] = model.state.history.flatMap { history =>
      val (newHistory, nextGraph) = history.redo()
      nextGraph.map { graph =>
        model.updateState(_.withHistory(newHistory))
        graph
      }
    }

    override def canUndo: Boolean = model.state.history.exists(_.canUndo)

    override def canRedo: Boolean = model.state.history.exists(_.canRedo)

    def saveSnapshot(): Unit = model.state.history.foreach { history =>
      model.state.timeMachine.foreach { tm =>
        tm.save(history)
      }
    }

    def restoreSnapshot(): Option[CaseKnowledgeGraph] = model.state.timeMachine.flatMap { tm =>
      tm.restore().map { restoredHistory =>
        model.updateState(_.withHistory(restoredHistory))
        tm.clear()
        restoredHistory.currentState.getOrElse(new CaseKnowledgeGraph())
      }
    }

    def hasSnapshot: Boolean = model.state.timeMachine.exists(_.hasSnapshot)

    def clearSnapshot(): Unit = model.state.timeMachine.foreach(_.clear())

    override def canAccuse: Boolean = (model.state.investigativeCase, model.state.currentGraph, model.state.timer) match
      case (Some(currentCase), Some(graph), Some(timer)) =>
        val prerequisitesMet =
          graph.coverage(currentCase.solution.prerequisite) >= SolutionConfig.PrerequisiteCoverageThreshold
        val timeThresholdMet = model.getRemainingTime match
          case Some(remaining) =>
            val elapsed = timer.totalDuration - remaining
            val elapsedPercentage = elapsed.toMillis.toDouble / timer.totalDuration.toMillis.toDouble
            elapsedPercentage >= SolutionConfig.TimeElapsedThreshold
          case None => false

        prerequisitesMet || timeThresholdMet
      case _ => false

    override def getAvailableSuspects: Set[Character] =
      model.state.investigativeCase.map(_.characters).getOrElse(Set.empty).filter(character =>
        !character.role.equals(CaseRole.Victim)
      )

    override def submitAccusation(character: Character): ValidationResult =
      model.updateState(_.withSubmissionState(SubmissionState.Submitting(character)))
      val result = if canAccuse then validateSubmission(character) else ValidationResult.PrerequisitesNotMet
      model.updateState(_.withSubmissionState(SubmissionState.Submitted(result)))
      model.state.timer.foreach(_.stop())
      result

    private def validateSubmission(accusedCharacter: Character): ValidationResult = model.state.investigativeCase match
      case Some(caseData) =>
        val solution = caseData.solution
        if accusedCharacter == solution.culprit then
          CorrectSolution(solution.culprit, solution.motive)
        else
          IncorrectSolution(accusedCharacter, solution.culprit, solution.motive)
      case _ => ValidationResult.PrerequisitesNotMet
