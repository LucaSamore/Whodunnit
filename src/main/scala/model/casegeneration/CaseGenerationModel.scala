package model.casegeneration

import cats.effect.IO

trait CaseGenerationModel:
  def generateCase(constraints: Seq[Constraint])
      : IO[Either[ProductionError, Case]]

object CaseGenerationModel:
  def apply(producer: Producer[Case]): CaseGenerationModel =
    new CaseGenerationModel:
      // Wrapped in IO Monad to allow for potential side effects in generation such as:
      // buildPrompt(constraints) -> IO effect for reading prompt from file
      // llmService.generateCase(prompt) -> IO effect for calling external LLM service via HTTP
      def generateCase(constraints: Seq[Constraint])
          : IO[Either[ProductionError, Case]] =
        IO(producer.produce(constraints*))
