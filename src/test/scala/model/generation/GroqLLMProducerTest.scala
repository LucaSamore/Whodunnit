package model.generation

import model.game.{Case, CaseFile, CaseFileType, CaseImpl, CaseKnowledgeGraph, CaseRole, Character, Plot, Solution}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GroqLLMProducerTest extends AnyWordSpec with Matchers with EitherValues
    with OptionValues:

  class TestableGroqLLMProducer[T](
      apiKey: String,
      mockResponse: Either[ProductionError, String]
  )(using parser: ResponseParser[T], promptBuilder: PromptBuilder[T])
      extends GroqLLMProducer[T](apiKey):

    var lastRequest: Option[GroqRequest] = None

    override protected def makeCall(req: GroqRequest)
        : Either[ProductionError, String] =
      lastRequest = Some(req)
      mockResponse

  given testPromptBuilder: PromptBuilder[Case] with
    def systemPrompt: String = "Test system prompt"
    def build(constraints: Constraint*): Either[ProductionError, String] =
      Right(s"User prompt with ${constraints.length} constraints")

  given testParser: ResponseParser[Case] with
    def parse(jsonString: String): Either[ProductionError, Case] =
      if jsonString.contains("valid") then
        Right(createValidCase())
      else
        Left(ProductionError.ParseError("Invalid response"))

  private def createValidCase(): Case =
    val plot = Plot("Test Mystery", "Test content")
    val character = Character("TestSuspect", CaseRole.Suspect)
    val caseFile = CaseFile("Evidence", "Content", CaseFileType.Email, None, None, None)
    val solution = Solution(new CaseKnowledgeGraph(), character, "Motive")
    CaseImpl(
      plot = plot,
      characters = Set(character),
      caseFiles = Set(caseFile),
      solution = solution
    )

  "GroqLLMProducer" should:
    "successfully produce case when all components work" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid response")
      )

      val result = producer.produce(Theme("noir"))

      result shouldBe a[Right[_, _]]
      result.value.plot.title shouldBe "Test Mystery"

    "create request with system and user prompts" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid response")
      )

      producer.produce(Theme("test"))

      producer.lastRequest shouldBe defined
      val messages = producer.lastRequest.value.messages

      messages should have size 2
      messages.exists(_.role == "system") shouldBe true
      messages.exists(_.role == "user") shouldBe true

    "pass constraints to prompt builder" in:
      var receivedConstraints: Seq[Constraint] = Seq.empty

      given capturingBuilder: PromptBuilder[Case] with
        def systemPrompt: String = "Test"
        def build(constraints: Constraint*): Either[ProductionError, String] =
          receivedConstraints = constraints
          Right("prompt")

      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid response")
      )(using testParser, capturingBuilder)

      val inputConstraints =
        Seq(Theme("noir"), Difficulty.Easy)
      producer.produce(
        inputConstraints*
      ) // use capturingBuilder and populate receivedConstraints

      receivedConstraints should contain theSameElementsAs inputConstraints

    "fail when prompt building fails" in:
      given failingBuilder: PromptBuilder[Case] with
        def systemPrompt: String = "Test"
        def build(constraints: Constraint*): Either[ProductionError, String] =
          Left(ProductionError.ConfigurationError("Template error"))

      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid")
      )(using testParser, failingBuilder)

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.ConfigurationError]

    "fail when API call fails" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Left(ProductionError.NetworkError("Timeout"))
      )

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.NetworkError]

    "fail when parsing fails" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Left(ProductionError.ParseError("invalid response"))
      )

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.ParseError]
