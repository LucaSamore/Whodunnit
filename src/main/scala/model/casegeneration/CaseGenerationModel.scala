package model.casegeneration

import cats.effect.IO
import cats.syntax.all.*

trait CaseGenerationModel:
  def generateCase(constraints: Seq[Constraint])
      : IO[Either[GenerationError, Case]]
  def parseCaseFromJson(json: String): Either[ParseError, Case]

object CaseGenerationModel:
  def apply(generator: CaseGenerator, parser: Parser): CaseGenerationModel =
    new CaseGenerationModel:
      // Wrapped in IO Monad to allow for potential side effects in generation such as:
      // buildPrompt(constraints) -> IO effect for reading prompt from file
      // llmService.generateCase(prompt) -> IO effect for calling external LLM service via HTTP
      def generateCase(constraints: Seq[Constraint])
          : IO[Either[GenerationError, Case]] =
        IO(generator.generate(constraints*))

      def parseCaseFromJson(json: String): Either[ParseError, Case] =
        ???
