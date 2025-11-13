package model.generation

import model.game.{Case, CaseFile, CaseFileType, CaseImpl, CaseKnowledgeGraph, CaseRole, Character, Plot, Solution}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GroqLLMProducerTest extends AnyWordSpec with Matchers with EitherValues with OptionValues:

  class MockPrompt(content: String) extends Prompt("/test/mock.md"):
    override def build(params: Seq[Prompt.Parameter] = Seq.empty): Either[ProductionError, String] =
      Right(content)

  class FailingPrompt extends Prompt("/test/failing.md"):
    override def build(params: Seq[Prompt.Parameter] = Seq.empty): Either[ProductionError, String] =
      Left(ProductionError.ConfigurationError("Template error"))

  class TestableGroqLLMProducer[T](
      apiKey: String,
      mockResponse: Either[ProductionError, String],
      testSystemPrompt: Prompt,
      testUserPrompt: Prompt
  )(using parser: ResponseParser[T])
      extends BaseLLMClient(apiKey) with GroqProvider with Producer[T]:

    import GroqProvider.model

    var lastRequest: Option[GroqRequest] = None
    var lastConstraints: Seq[Constraint] = Seq.empty

    override protected def makeCall(req: GroqRequest): Either[ProductionError, String] =
      lastRequest = Some(req)
      mockResponse

    override def produce(constraints: Constraint*): Either[ProductionError, T] =
      lastConstraints = constraints
      val params = Seq(Prompt.Parameter(Prompt.Placeholder.Constraints, constraints.map(_.toPromptDescription)))
      for
        systemPrompt <- testSystemPrompt.build()
        userPrompt <- testUserPrompt.build(params)
        request = GroqRequest(
          model = model,
          messages = List(
            GroqMessage("system", systemPrompt),
            GroqMessage("user", userPrompt)
          )
        )
        result <- invoke[T](request)(using parser)
      yield result

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
        mockResponse = Right("valid response"),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = MockPrompt("Test user prompt")
      )

      val result = producer.produce(Theme("noir"))

      result shouldBe a[Right[_, _]]
      result.value.plot.title shouldBe "Test Mystery"

    "create request with system and user prompts" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid response"),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = MockPrompt("Test user prompt")
      )

      producer.produce(Theme("test"))

      producer.lastRequest shouldBe defined
      val messages = producer.lastRequest.value.messages

      messages should have size 2
      messages.exists(_.role == "system") shouldBe true
      messages.exists(_.role == "user") shouldBe true

    "pass constraints to prompt builder" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid response"),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = MockPrompt("Test user prompt")
      )

      val inputConstraints = Seq(Theme("noir"), Difficulty.Easy)
      producer.produce(inputConstraints*)

      producer.lastConstraints should contain theSameElementsAs inputConstraints

    "fail when prompt building fails" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("valid"),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = FailingPrompt()
      )

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.ConfigurationError]

    "fail when API call fails" in:
      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Left(ProductionError.NetworkError("Timeout")),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = MockPrompt("Test user prompt")
      )

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.NetworkError]

    "fail when parsing fails" in:
      given failingParser: ResponseParser[Case] with
        def parse(jsonString: String): Either[ProductionError, Case] =
          Left(ProductionError.ParseError("Parse failed"))

      val producer = new TestableGroqLLMProducer[Case](
        apiKey = "test-key",
        mockResponse = Right("some response"),
        testSystemPrompt = MockPrompt("Test system prompt"),
        testUserPrompt = MockPrompt("Test user prompt")
      )(using failingParser)

      val result = producer.produce()

      result.left.value shouldBe a[ProductionError.ParseError]
