package model.game

import model.generation.{Constraint, Producer, ProductionError}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}

class CaseTest extends AnyWordSpec with Matchers with EitherValues
    with OptionValues:

  class MockProducer(response: Either[ProductionError, Case])
      extends Producer[Case]:
    var capturedConstraints: Option[Seq[Constraint]] = None

    def produce(constraints: Constraint*): Either[ProductionError, Case] =
      capturedConstraints = Some(constraints)
      response

  private def createTestCase(): Case =
    val plot = Plot("Test Mystery", "Test content")
    val character = Character("Suspect", CaseRole.Suspect)
    val caseFile =
      CaseFile("Evidence", "Content", CaseFileType.Email, None, None, None)
    val solution = CaseSolution(Set.empty, character, "Test motive")
    CaseImpl(plot, Set(caseFile), Set(character), solution)

  "Case.apply" when:

    "producer succeeds" should:
      "return the produced case" in:
        val expectedCase = createTestCase()
        given testProducer: Producer[Case] =
          new MockProducer(Right(expectedCase))

        val result = Case.apply(Constraint.Theme("noir"))

        result shouldBe a[Right[_, _]]
        result.value shouldBe expectedCase

      "pass constraints to producer" in:
        val mockProducer = new MockProducer(Right(createTestCase()))
        given Producer[Case] = mockProducer

        val constraints = Seq(
          Constraint.Theme("detective"),
          Constraint.CharactersRange(2, 4),
          Constraint.Difficulty.Medium
        )

        Case.apply(constraints*)

        mockProducer.capturedConstraints shouldBe defined

      "work with varargs constraints" in:
        val mockProducer = new MockProducer(Right(createTestCase()))
        given Producer[Case] = mockProducer

        Case.apply(
          Constraint.Theme("noir"),
          Constraint.CharactersRange(3, 5),
          Constraint.CaseFilesRange(4, 8)
        )

        mockProducer.capturedConstraints shouldBe defined
        mockProducer.capturedConstraints.value should have size 3

    "producer fails" should:
      "return LLMError when producer returns LLMError" in:
        val error = ProductionError.LLMError("API failed")
        given testProducer: Producer[Case] = new MockProducer(Left(error))

        val result = Case.apply(Constraint.Theme("test"))

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.LLMError]
        result.left.value.message should include("API failed")

      "return NetworkError when producer returns NetworkError" in:
        val error = ProductionError.NetworkError("Connection timeout")
        given testProducer: Producer[Case] = new MockProducer(Left(error))

        val result = Case.apply()

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.NetworkError]
        result.left.value.message should include("Connection timeout")

      "return ParseError when producer returns ParseError" in:
        val error = ProductionError.ParseError("Invalid JSON")
        given testProducer: Producer[Case] = new MockProducer(Left(error))

        val result = Case.apply(Constraint.Difficulty.Easy)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("Invalid JSON")

      "return ConfigurationError when producer returns ConfigurationError" in:
        val error = ProductionError.ConfigurationError("Missing API key")
        given testProducer: Producer[Case] = new MockProducer(Left(error))

        val result = Case.apply()

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ConfigurationError]
        result.left.value.message should include("Missing API key")
