package controller

import utils.TestUtils.mockCase
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import model.casegeneration.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues

class CaseGenerationControllerTest extends AnyWordSpec with Matchers
    with EitherValues:

  class MockCaseGenerationModel(
      response: Seq[Constraint] => IO[Either[GenerationError, Case]]
  ) extends CaseGenerationModel:
    var lastConstraints: Seq[Constraint] = Seq.empty

    override def generateCase(
        constraints: Seq[Constraint]
    ): IO[Either[GenerationError, Case]] =
      lastConstraints = constraints
      response(constraints)

    override def parseCaseFromJson(json: String): Either[ParseError, Case] = ???

  "CaseGenerationController" should:
    "include difficulty constraint in generateNewCase" in:
      val model = MockCaseGenerationModel(_ => IO.pure(Right(mockCase)))
      val controller = CaseGenerationController(model)

      val result = controller.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Medium
      ).unsafeRunSync()

      result.isRight shouldBe true
      model.lastConstraints should contain(Constraint.Difficulty.Medium)
      model.lastConstraints should have size 1

    "include theme when provided in generateNewCase" in:
      val model = MockCaseGenerationModel(_ => IO.pure(Right(mockCase)))
      val controller = CaseGenerationController(model)

      val result = controller.generateNewCase(
        theme = Some("Mystery"),
        difficulty = Constraint.Difficulty.Easy
      ).unsafeRunSync()

      result.isRight shouldBe true
      model.lastConstraints should contain(Constraint.Difficulty.Easy)
      model.lastConstraints should contain(Constraint.Theme("Mystery"))

    "include custom constraints in generateNewCase" in:
      val model = MockCaseGenerationModel(_ => IO.pure(Right(mockCase)))
      val controller = CaseGenerationController(model)
      val customConstraint = Constraint.CaseFilesRange(5, 10)

      val result = controller.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Hard,
        customConstraints = Seq(customConstraint)
      ).unsafeRunSync()

      result.isRight shouldBe true
      model.lastConstraints should contain allOf (
        Constraint.Difficulty.Hard,
        customConstraint
      )

    "return error when model fails in generateNewCase" in:
      val error = GenerationError.LLMError("LLM service failed")
      val model = MockCaseGenerationModel(_ => IO.pure(Left(error)))
      val controller = CaseGenerationController(model)

      val result = controller.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Easy
      ).unsafeRunSync()

      result.left.value shouldBe error

    "pass constraints correctly in generateCaseWithConstraints" in:
      val model = MockCaseGenerationModel(_ => IO.pure(Right(mockCase)))
      val controller = CaseGenerationController(model)
      val constraints = Seq(
        Constraint.Difficulty.Hard,
        Constraint.Theme("Noir")
      )

      val result =
        controller.generateCaseWithConstraints(constraints).unsafeRunSync()

      result.isRight shouldBe true
      model.lastConstraints shouldBe constraints

    "handle empty constraints in generateCaseWithConstraints" in:
      val model = MockCaseGenerationModel(_ => IO.pure(Right(mockCase)))
      val controller = CaseGenerationController(model)

      val result =
        controller.generateCaseWithConstraints(Seq.empty).unsafeRunSync()

      result.isRight shouldBe true
      model.lastConstraints shouldBe empty
