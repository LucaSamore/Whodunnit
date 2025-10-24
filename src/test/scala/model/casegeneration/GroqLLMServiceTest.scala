package model.casegeneration

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues
import io.circe.parser._
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3.testing.SttpBackendStub

class GroqLLMServiceTest extends AnyWordSpec with Matchers with EitherValues:

  "GroqRequest" should:
    "serialize to valid JSON" in:
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

  "GroqResponse" should:
    "deserialize from valid JSON" in:
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

  "GroqLLMService" when:
    "making successful API call" should:
      "return generated content from Groq response" in:
        val mockJsonResponse =
          """
          {
            "choices": [
              {
                "message": {
                  "role": "assistant",
                  "content": "Generated JSON content"
                }
              }
            ]
          }
        """

        val backend = SttpBackendStub.synchronous
          .whenRequestMatches(_ => true)
          .thenRespond(mockJsonResponse)

        val service = new GroqLLMService(
          apiKey = "test_key",
          model = "llama-3.1-8b-instant",
          backend = backend
        )

        val result = service.generateCase("test prompt")

        result shouldBe a[Right[_, _]]
        result.value shouldBe "Generated JSON content"

    "handling empty response" should:
      "return LLMError when choices list is empty" in:
        val emptyJsonResponse = """
          {
            "choices": []
          }
        """

        val backend = SttpBackendStub.synchronous
          .whenRequestMatches(_ => true)
          .thenRespond(emptyJsonResponse)

        val service = new GroqLLMService("test_key", "test_model", backend)

        val result = service.generateCase("test prompt")

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[GenerationError.LLMError]
        result.left.value.message should include("No response content")

    "handling HTTP errors" should:
      "return LLMError when backend returns error" in:
        val backend = SttpBackendStub.synchronous
          .whenRequestMatches(_ => true)
          .thenRespondServerError()

        val service = new GroqLLMService("test_key", "test_model", backend)

        val result = service.generateCase("test prompt")

        result shouldBe a[Left[_, _]]
        result.left.value.message should include("Groq API error")