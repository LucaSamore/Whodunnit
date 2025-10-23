package model.case_generation

trait CaseGenerator:
  def generate(constraints: Constraint*): Either[Error, Case]


sealed trait GenerationError:
  def message: String

object GenerationError:
  case class LLMError(message: String) extends GenerationError

  case class InvalidPromptError(message: String) extends GenerationError

  case class ParseFailureError(parseError: ParseError) extends GenerationError:
    def message: String = s"Failed to parse LLM response: ${parseError.message}"