package controller

import utils.TestUtils.mockCase
import cats.effect.unsafe.implicits.global
import model.casegeneration.*
import model.ModelModule
import view.ViewModule
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.EitherValues

class CaseGenerationControllerTest extends AnyWordSpec with Matchers
    with EitherValues:

  class MockProducer(
      response: Seq[Constraint] => Either[ProductionError, Case]
  ) extends Producer[Case]:
    var lastConstraints: Seq[Constraint] = Seq.empty

    override def produce(constraints: Constraint*)
        : Either[ProductionError, Case] =
      lastConstraints = constraints.toSeq
      response(lastConstraints)

  def createTestEnvironment(mockProducer: MockProducer)
      : (ModelModule.Model[Unit], ControllerModule.Controller[Unit]) =
    val env = new ModelModule.Component[Unit]
      with ControllerModule.Component[Unit]
      with ViewModule.Component[Unit]
      with ModelModule.Provider[Unit]
      with ControllerModule.Provider[Unit]
      with ViewModule.Provider[Unit]:

      val model: ModelModule.Model[Unit] = new ModelImpl:
        override val producer: Producer[Case] = mockProducer
      val view: ViewModule.View[Unit] = new ViewImpl
      val controller: ControllerModule.Controller[Unit] = new ControllerImpl
    (env.model, env.controller)

  "ModelModule case generation (accessed through Controller's Cake Pattern)" should:
    "include difficulty constraint in generateNewCase" in:
      val mockProducer = new MockProducer(_ => Right(mockCase))
      val (model, _) = createTestEnvironment(mockProducer)

      val result = model.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Medium
      ).unsafeRunSync()

      result.isRight shouldBe true
      mockProducer.lastConstraints should contain(Constraint.Difficulty.Medium)
      mockProducer.lastConstraints should have size 1

    "include theme when provided in generateNewCase" in:
      val mockProducer = new MockProducer(_ => Right(mockCase))
      val (model, _) = createTestEnvironment(mockProducer)

      val result = model.generateNewCase(
        theme = Some("Mystery"),
        difficulty = Constraint.Difficulty.Easy
      ).unsafeRunSync()

      result.isRight shouldBe true
      mockProducer.lastConstraints should contain(Constraint.Difficulty.Easy)
      mockProducer.lastConstraints should contain(Constraint.Theme("Mystery"))
      mockProducer.lastConstraints should have size 2

    "include custom constraints in generateNewCase" in:
      val mockProducer = new MockProducer(_ => Right(mockCase))
      val (model, _) = createTestEnvironment(mockProducer)
      val customConstraint = Constraint.CaseFilesRange(5, 10)

      val result = model.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Hard,
        customConstraints = Seq(customConstraint)
      ).unsafeRunSync()

      result.isRight shouldBe true
      mockProducer.lastConstraints should contain(Constraint.Difficulty.Hard)
      mockProducer.lastConstraints should contain(customConstraint)
      mockProducer.lastConstraints should have size 2

    "pass all constraints correctly when using difficulty and theme" in:
      val mockProducer = new MockProducer(_ => Right(mockCase))
      val (model, _) = createTestEnvironment(mockProducer)

      val result = model.generateNewCase(
        theme = Some("Noir"),
        difficulty = Constraint.Difficulty.Hard
      ).unsafeRunSync()

      result.isRight shouldBe true
      mockProducer.lastConstraints should contain(Constraint.Difficulty.Hard)
      mockProducer.lastConstraints should contain(Constraint.Theme("Noir"))
      mockProducer.lastConstraints should have size 2

    "work with only difficulty constraint when no theme or custom constraints provided" in:
      val mockProducer = new MockProducer(_ => Right(mockCase))
      val (model, _) = createTestEnvironment(mockProducer)

      val result = model.generateNewCase(
        theme = None,
        difficulty = Constraint.Difficulty.Easy
      ).unsafeRunSync()

      result.isRight shouldBe true
      mockProducer.lastConstraints should contain only Constraint.Difficulty.Easy
