package model.case_generation

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues
import org.scalamock.scalatest.MockFactory
import io.circe.syntax._
import io.circe.parser._

class LLMCaseGeneratorTest extends AnyWordSpec with Matchers with EitherValues
    with MockFactory:

  "LLMCaseGenerator" when:
    "generating with valid LLM response" should:
      "return Case successfully" in:
        val mockLLMService = mock[LLMService]
        val validJson =
          """{
          "plot": {"title": "Test Case", "content": "Test content"},
          "characters": [{"name": "Alice", "role": "Suspect"}],
          "files": [{"title": "Evidence", "kind": "Email", "content": "Test", "sender": "Alice", "receiver": null, "date": null}],
          "solution": {"prerequisite": [], "culprit": "Alice", "motive": "Test"}
        }"""

        (mockLLMService.generateCase _)
          .expects(*)
          .returning(Right(validJson))
          .once()

        val generator = LLMCaseGenerator(mockLLMService, JsonParser)
        val result = generator.generate(Constraint.Theme("Test"))

        result shouldBe a[Right[_, _]]
        result.value.plot.title shouldBe "Test Case"

    "handling LLM errors" should:
      "propagate LLMError from service" in:
        val mockLLMService = mock[LLMService]

        (mockLLMService.generateCase _)
          .expects(*)
          .returning(Left(GenerationError.LLMError("Connection timeout")))
          .once()

        val generator = LLMCaseGenerator(mockLLMService, JsonParser)
        val result = generator.generate(Constraint.Theme("Test"))

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[GenerationError.LLMError]
        result.left.value.message shouldBe "Connection timeout"

  "GroqRequest" should :
    "serialize to valid JSON" in :
      val request = GroqRequest(
        messages = List(
          Message("system", "You are helpful"),
          Message("user", "Hello")
        ),
        model = "llama-3.1-8b-instant"
      )

      val json = request.asJson.noSpaces

      json should include("messages")
      json should include("model")
      json should include("llama-3.1-8b-instant")
      json should include("system")
      json should include("user")

  "GroqResponse" should :
    "deserialize from valid JSON" in :
      val jsonString =
        """
        {
          "choices": [
            {
              "message": {
                "role": "assistant",
                "content": "Generated case content"
              }
            }
          ]
        }
      """

      val result = decode[GroqResponse](jsonString)

      result shouldBe a[Right[_, _]]
      result.map { response =>
        response.choices should have size 1
        response.choices.head.message.content shouldBe "Generated case content"
      }
