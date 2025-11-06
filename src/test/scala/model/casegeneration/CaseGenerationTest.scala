package model.casegeneration

import utils.TestUtils.mockCase
import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CaseGenerationTest extends AnyWordSpec with Matchers:

  "CaseGenerationModel" when:
    "generateCase is called" should:
      "return a Right with a valid Case when production succeeds" in:
        val mockProducer = new Producer[Case]:
          override def produce(constraints: Constraint*)
              : Either[ProductionError, Case] =
            Right(mockCase)

        val model = CaseGenerationModel(mockProducer)
        val constraints = Seq(Constraint.Difficulty.Easy)
        val result = model.generateCase(constraints).unsafeRunSync()

        result shouldBe Right(mockCase)

      "return a Left with GenerationError when production fails" in:
        val error = ProductionError.LLMError("LLM service failed")
        val mockProducer = new Producer[Case]:
          override def produce(constraints: Constraint*)
              : Either[ProductionError, Case] =
            Left(error)

        val model = CaseGenerationModel(mockProducer)
        val constraints = Seq(Constraint.Difficulty.Easy)
        val result = model.generateCase(constraints).unsafeRunSync()

        result shouldBe Left(error)

      "pass all constraints to the production" in:
        var capturedConstraints: Seq[Constraint] = Seq.empty
        val mockGenerator = new Producer[Case]:
          override def produce(constraints: Constraint*)
              : Either[ProductionError, Case] =
            capturedConstraints = constraints.toSeq
            Right(mockCase)

        val model = CaseGenerationModel(mockGenerator)
        val constraints = Seq(
          Constraint.Difficulty.Medium,
          Constraint.Theme("Cybercrime"),
          Constraint.CaseFilesRange(2, 5)
        )
        model.generateCase(constraints).unsafeRunSync()

        capturedConstraints should contain theSameElementsAs constraints

      "handle empty constraints sequence" in:
        val mockProducer = new Producer[Case]:
          override def produce(constraints: Constraint*)
              : Either[ProductionError, Case] =
            Right(mockCase)

        val model = CaseGenerationModel(mockProducer)
        val result = model.generateCase(Seq.empty).unsafeRunSync()

        result shouldBe Right(mockCase)
