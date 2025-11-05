package model.casegeneration

import cats.effect.unsafe.implicits.global
import model.casegeneration.TestUtils.mockCase
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime

class CaseGenerationTest extends AnyWordSpec with Matchers:

  "CaseGenerationModel" when:
    "generateCase is called" should:
      "return a Right with a valid Case when generation succeeds" in:
        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] =
            Right(mockCase)
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] =
            ???
        val model = CaseGenerationModel(mockGenerator, mockParser)
        val constraints = Seq(Constraint.Difficulty.Easy)
        val result = model.generateCase(constraints).unsafeRunSync()
        result shouldBe Right(mockCase)

      "return a Left with GenerationError when generation fails" in:
        val error = GenerationError.LLMError("LLM service failed")
        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] =
            Left(error)
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] = ???
        val model = CaseGenerationModel(mockGenerator, mockParser)
        val constraints = Seq(Constraint.Difficulty.Easy)
        val result = model.generateCase(constraints).unsafeRunSync()
        result shouldBe Left(error)

      "pass all constraints to the generator" in:
        var capturedConstraints: Seq[Constraint] = Seq.empty
        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] =
            capturedConstraints = constraints.toSeq
            Right(mockCase)
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] = ???
        val model = CaseGenerationModel(mockGenerator, mockParser)
        val constraints = Seq(
          Constraint.Difficulty.Medium,
          Constraint.Theme("Cybercrime"),
          Constraint.CaseFilesRange(2, 5)
        )
        model.generateCase(constraints).unsafeRunSync()
        capturedConstraints should contain theSameElementsAs constraints

      "handle empty constraints sequence" in:
        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] =
            Right(mockCase)
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] = ???
        val model = CaseGenerationModel(mockGenerator, mockParser)
        val result = model.generateCase(Seq.empty).unsafeRunSync()
        result shouldBe Right(mockCase)

    "parseCaseFromJson is called" should:
      "return a Right with a valid Case when parsing succeeds" in:
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] =
            Right(mockCase)

        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] = ???

        val model = CaseGenerationModel(mockGenerator, mockParser)
        val json =
          """mock json string representing a Case"""

        val result = model.parseCaseFromJson(json)

        result shouldBe Right(mockCase)

      "return a Left with ParseError when parsing fails" in:
        val error = JsonSyntaxError("Invalid JSON format")
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] =
            Left(error)

        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] = ???

        val model = CaseGenerationModel(mockGenerator, mockParser)
        val json = """invalid json"""

        val result = model.parseCaseFromJson(json)

        result shouldBe Left(error)

      "pass the json string to the parser" in:
        var capturedJson: String = ""
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] =
            capturedJson = json
            Right(mockCase)

        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] = ???

        val model = CaseGenerationModel(mockGenerator, mockParser)
        val json = """{"test": "data"}"""

        model.parseCaseFromJson(json)

        capturedJson shouldBe json

      "handle empty json string" in:
        val error = JsonSyntaxError("Invalid JSON format")
        val mockParser = new Parser:
          override def parse(json: String): Either[ParseError, Case] =
            if json.isEmpty then Left(error) else Right(mockCase)

        val mockGenerator = new CaseGenerator:
          override def generate(constraints: Constraint*)
              : Either[GenerationError, Case] = ???

        val model = CaseGenerationModel(mockGenerator, mockParser)

        val result = model.parseCaseFromJson("")

        result shouldBe Left(error)
