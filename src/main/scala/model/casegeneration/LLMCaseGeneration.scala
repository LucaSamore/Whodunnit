package model.casegeneration

import scala.io.Source
import scala.util.{Try, Using}

trait LLMService:
  def generateCase(prompt: String): Either[GenerationError.LLMError, String]

class LLMCaseGenerator(
    llmService: LLMService,
    parser: Parser
) extends CaseGenerator:

  def generate(constraints: Constraint*): Either[GenerationError, Case] =
    for
      prompt <- buildPrompt(constraints)
      jsonResponse <- llmService.generateCase(prompt)
      parsedCase <- parseResponse(jsonResponse)
    yield parsedCase

  private def buildPrompt(constraints: Seq[Constraint])
      : Either[GenerationError, String] =
    loadPromptTemplate().map { template =>
      val constraintsText =
        constraints.map(_.toPromptDescription).mkString("\n- ", "\n- ", "")
      val finalPrompt = template.replace("{{CONSTRAINTS}}", constraintsText)
      finalPrompt
    }

  private def parseResponse(jsonResponse: String)
      : Either[GenerationError, Case] = {
    println(s"Generated case: $jsonResponse")
    val cleanedJson = cleanJson(jsonResponse)
    parser.parse(cleanedJson).left.map(GenerationError.ParseFailureError.apply)
  }

  private def cleanJson(input: String): String =
    input
      .replaceAll("```json", "")
      .replaceAll("```", "")
      .trim

  private def loadPromptTemplate(): Either[GenerationError, String] =
    Try {
      Using.resource(
        getClass.getResourceAsStream("/prompts/caseGenerationPrompt.txt")
      ) { stream =>
        Source.fromInputStream(stream).mkString
      }
    }.toEither.left.map(e =>
      GenerationError.InvalidPromptError(
        s"Failed to load prompt template: ${e.getMessage}"
      )
    )

object LLMCaseGenerator:
  def apply(llmService: LLMService, parser: Parser): LLMCaseGenerator =
    new LLMCaseGenerator(llmService, parser)
