package model.generation

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PromptBuilderTest extends AnyWordSpec with Matchers with EitherValues
    with OptionValues:

  "PromptBuilder for Case" when:

    "accessing system prompt" should:
      "return predefined system prompt" in:
        val systemPrompt = SystemPrompt.Base.build().toOption

        systemPrompt should not be None

    "building prompt" should:
      "successfully load template and return Right" in:
        val result = UserPrompt.Case.build()

        result shouldBe a[Right[_, _]]

      "replace constraints placeholder with formatted constraint descriptions" in:
        val result = UserPrompt.Case.build(
          Seq(
            Prompt.Parameter(
              Prompt.Placeholder.Constraints,
              Seq(
                Theme("test").toPromptDescription,
                CharactersRange(2, 4).toPromptDescription
              )
            )
          )
        )

        result shouldBe a[Right[_, _]]
        val prompt = result.value

        prompt should not include ("{{CONSTRAINTS}}")

      "preserve template structure after substitution" in:
        val result = UserPrompt.Case.build(
          Seq(
            Prompt.Parameter(
              Prompt.Placeholder.Constraints,
              Seq(
                Theme("test").toPromptDescription
              )
            )
          )
        )

        result shouldBe a[Right[_, _]]
        val prompt = result.value

        prompt should include("You are a procedural story generation engine for an investigative game.")
        prompt should include("Contains all relevant entities in the case. Each node must have a `$type` field")
        prompt should include("## Constraints")
        prompt should include("Before returning your JSON, verify:")
        prompt should include("## JSON Template")

      "handle empty constraints without errors" in:
        val result = UserPrompt.Case.build(
          Seq(
            Prompt.Parameter(
              Prompt.Placeholder.Constraints,
              Seq.empty
            )
          )
        )

        result shouldBe a[Right[_, _]]
        result.value should not be empty
