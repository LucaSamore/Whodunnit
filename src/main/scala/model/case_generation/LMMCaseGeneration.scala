package model.case_generation

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
      parsedCase <- parser.parse(
        jsonResponse
      ).left.map(GenerationError.ParseFailureError.apply)
    yield parsedCase

  private def buildPrompt(constraints: Seq[Constraint])
      : Either[GenerationError, String] =
    loadPromptTemplate() match
      case Right(template) =>
        val constraintsText =
          constraints.map(_.toPromptDescription).mkString("\n- ", "\n- ", "")
        Right(template.replace("{{CONSTRAINTS}}", constraintsText))
      case Left(error) => Left(error)

  private def loadPromptTemplate(): Either[GenerationError, String] =
    Try {
      Using.resource(
        getClass.getResourceAsStream("/prompts/case_generation_prompt.txt")
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
