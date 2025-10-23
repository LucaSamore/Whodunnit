package model.case_generation

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues
import org.scalamock.scalatest.MockFactory

class LLMCaseGeneratorTest extends AnyWordSpec with Matchers with EitherValues with MockFactory:

  "LLMCaseGenerator" when:
    "generating with valid LLM response" should:
      "return Case successfully" in:
        val mockLLMService = mock[LLMService]
        val validJson = """{
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
        println(generator)
        val result = generator.generate(Constraint.Theme("Test"))
        println(result)

        result shouldBe a[Right[_, _]]
        result.value.plot.title shouldBe "Test Case"