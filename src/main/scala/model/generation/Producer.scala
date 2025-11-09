package model.generation

import model.game.{Case, Hint}

trait Producer[T]:
  def produce(constraints: Constraint*): Either[ProductionError, T]

object Producers:
  import ResponseParser.given
  import PromptBuilder.given

  given Producer[Case] = new GroqLLMProducer[Case](apiKey = GroqProvider.apiKey)
  given Producer[Hint] = new GroqLLMProducer[Hint](apiKey = GroqProvider.apiKey)
