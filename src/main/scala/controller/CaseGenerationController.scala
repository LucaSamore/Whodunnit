package controller

import cats.effect.IO
import model.casegeneration.*

trait CaseGenerationController:
  def generateNewCase(
      theme: Option[String],
      difficulty: Constraint.Difficulty,
      customConstraints: Seq[Constraint] = Seq.empty
  ): IO[Either[ProductionError, Case]]

  def generateCaseWithConstraints(
      constraints: Seq[Constraint]
  ): IO[Either[ProductionError, Case]]

object CaseGenerationController:
  def apply(caseGenModel: CaseGenerationModel): CaseGenerationController =
    new CaseGenerationController:

      def generateNewCase(
          theme: Option[String],
          difficulty: Constraint.Difficulty,
          customConstraints: Seq[Constraint] = Seq.empty
      ): IO[Either[ProductionError, Case]] =
        val baseConstraints = Seq(difficulty) ++ customConstraints
        val allConstraints = theme match
          case Some(t) => baseConstraints :+ Constraint.Theme(t)
          case None    => baseConstraints

        caseGenModel.generateCase(allConstraints)

      def generateCaseWithConstraints(
          constraints: Seq[Constraint]
      ): IO[Either[ProductionError, Case]] =
        caseGenModel.generateCase(constraints)
