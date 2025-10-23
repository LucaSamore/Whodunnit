package model.case_generation

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues


class CaseGeneratorTest extends AnyWordSpec with Matchers with EitherValues:

  "CaseGenerator" should :
    "have generate method returning Either" in :
      val generator: CaseGenerator = new CaseGenerator:
        def generate(constraints: Constraint*): Either[Error, Case] =
          Left(Error("Not implemented"))

      val result = generator.generate(Constraint.Theme("Test"))
      result shouldBe a[Left[_, _]]


  "GenerationError" should :
    "have LLMError with message" in :
      val error = GenerationError.LLMError("Connection timeout")
      error.message shouldBe "Connection timeout"

    "have InvalidPromptError with message" in :
      val error = GenerationError.InvalidPromptError("No constraints provided")
      error.message shouldBe "No constraints provided"

    "have ParseFailureError wrapping ParseError" in :
      val parseError = MissingFieldError("plot")
      val error = GenerationError.ParseFailureError(parseError)

      error.message should include("plot")