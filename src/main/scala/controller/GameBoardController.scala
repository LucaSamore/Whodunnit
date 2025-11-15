package controller

import model.ModelModule
import model.game.ValidationResult.{CorrectSolution, IncorrectSolution}
import model.game.{CaseKnowledgeGraph, CaseRole, Character, SolutionConfig, SubmissionState, ValidationResult}
import model.hint.Metric.coverage

/** Controller for managing game board operations and player actions.
  *
  * This controller handles:
  *   - Undo/redo functionality for player actions
  *   - Time machine (snapshot) save and restore operations
  *   - Accusation submission and validation
  *   - Game state queries (can accuse, available suspects, etc.)
  */
trait GameBoardController extends ControllerModule.Controller:
  /** Undoes the last action, moving back one state in the history.
    *
    * @return
    *   Some(graph) if undo was successful, None if no previous state exists
    */
  def undo(): Option[CaseKnowledgeGraph]

  /** Redoes the previously undone action, moving forward one state in the history.
    *
    * @return
    *   Some(graph) if redo was successful, None if no next state exists
    */
  def redo(): Option[CaseKnowledgeGraph]

  /** Checks if an undo operation is currently possible.
    *
    * @return
    *   true if there is a previous state to undo to, false otherwise
    */
  def canUndo: Boolean

  /** Checks if a redo operation is currently possible.
    *
    * @return
    *   true if there is a next state to redo to, false otherwise
    */
  def canRedo: Boolean

  /** Saves a snapshot of the current history state.
    *
    * This allows the player to create a save point they can return to later.
    */
  def saveSnapshot(): Unit

  /** Restores the history from the saved snapshot.
    *
    * After restoration, the snapshot is automatically cleared.
    *
    * @return
    *   Some(graph) if a snapshot existed and was restored, None otherwise
    */
  def restoreSnapshot(): Option[CaseKnowledgeGraph]

  /** Checks if a snapshot has been saved.
    *
    * @return
    *   true if a snapshot exists, false otherwise
    */
  def hasSnapshot: Boolean

  /** Clears the saved snapshot without restoring it. */
  def clearSnapshot(): Unit

  /** Checks if the player can currently make an accusation.
    *
    * An accusation is allowed if either:
    *   - The prerequisite coverage threshold has been met, OR
    *   - The minimum time elapsed threshold has been met
    *
    * @return
    *   true if the player can accuse, false otherwise
    */
  def canAccuse: Boolean

  /** Returns the set of characters that can be accused.
    *
    * This includes all characters in the case except the victim.
    *
    * @return
    *   set of characters available for accusation
    */
  def getAvailableSuspects: Set[Character]

  /** Submits an accusation for the specified character.
    *
    * This validates the accusation, updates the submission state, and stops the game timer. The result indicates
    * whether the accusation was correct or incorrect, along with the actual solution.
    *
    * @param character
    *   the character being accused
    * @return
    *   the validation result of the accusation
    */
  def submitAccusation(character: Character): ValidationResult

  /** Checks if there are unread notifications.
   *
   * @return
   * true if there are unread notifications, false otherwise
   */
  def hasUnreadNotifications: Boolean

  /** Marks all notifications as read.
   *
   * This is called when the user opens the notifications panel.
   */
  def markNotificationsAsRead(): Unit

/** Companion object providing factory methods for GameBoardController. */
object GameBoardController:
  /** Creates a new GameBoardController instance.
    *
    * @param model
    *   the model instance to use for state management
    * @return
    *   a new GameBoardController
    */
  def apply(model: ModelModule.Model): GameBoardController =
    new GameBoardControllerImpl(model)

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
      val result = validateSubmission(character)
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

    private var lastSeenHintCount: Int = 0
  
    override def hasUnreadNotifications: Boolean =
      val currentHintCount = model.state.hints.map(_.size).getOrElse(0)
      currentHintCount > lastSeenHintCount
  
    override def markNotificationsAsRead(): Unit =
      lastSeenHintCount = model.state.hints.map(_.size).getOrElse(0)