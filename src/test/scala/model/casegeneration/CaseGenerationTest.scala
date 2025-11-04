package model.casegeneration

import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDateTime

class CaseGenerationTest extends AnyWordSpec with Matchers:

  private val mockPlot = Plot("Plot Title", "Plot Content")
  private val mockCaseFiles = Set(CaseFile(
    "title",
    "content",
    CaseFileType.Message,
    None,
    None,
    Some(LocalDateTime.now())
  ))
  private val mockCharacters = Set(
    Character("John Doe", CaseRole.Suspect),
    Character("Jane Smith", CaseRole.Victim)
  )
  private val mockSolution = CaseSolution(
    Set(),
    Character("John Doe", CaseRole.Suspect),
    "Motive description"
  )
  private val mockCase =
    Case(mockPlot, mockCaseFiles, mockCharacters, mockSolution)

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
