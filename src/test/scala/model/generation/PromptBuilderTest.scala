package model.generation

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PromptBuilderTest extends AnyWordSpec with Matchers with EitherValues
    with OptionValues:

  "PromptBuilder for Case" when:

    "accessing system prompt" should:
      "return predefined system prompt" in:
        val systemPrompt = PromptBuilder.given_PromptBuilder_Case.systemPrompt

        systemPrompt shouldBe "You are a mystery game master. Generate cases in JSON format."

    "building prompt" should:
      "successfully load template and return Right" in:
        val result = PromptBuilder.given_PromptBuilder_Case.build()

        result shouldBe a[Right[_, _]]

      "replace constraints placeholder with formatted constraint descriptions" in:
        val result = PromptBuilder.given_PromptBuilder_Case.build(
          Constraint.Theme("test"),
          Constraint.CharactersRange(2, 4)
        )

        result shouldBe a[Right[_, _]]
        val prompt = result.value

        prompt should not include ("{{CONSTRAINTS}}")

      "preserve template structure after substitution" in:
        val result =
          PromptBuilder.given_PromptBuilder_Case.build(Constraint.Theme("test"))

        result shouldBe a[Right[_, _]]
        val prompt = result.value

        prompt should include("gioco investigativo")
        prompt should include("La trama")
        prompt should include("Vincoli da rispettare:")
        prompt should include("Important rules:")
        prompt should include("```json")

      "handle empty constraints without errors" in:
        val result = PromptBuilder.given_PromptBuilder_Case.build()

        result shouldBe a[Right[_, _]]
        result.value should not be empty
