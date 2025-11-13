package model.generation

import model.game.{Case, Hint}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait Producer[T]:

  given ExecutionContext = ExecutionContext.global

  def produce(constraints: Constraint*): Either[ProductionError, T]

  def produceAsync(constraints: Constraint*): Future[Either[ProductionError, T]] = Future {
    produce(constraints*)
  }

object Producers:
  import ResponseParser.given
  import PromptBuilder.given

  given Producer[Case] = GroqProvider.apiKey match
    case Some(key) => new GroqLLMProducer[Case](apiKey = key)
    case None      => FileBasedProducer.forCase

  given Producer[Hint] = GroqProvider.apiKey match
    case Some(key) => new GroqLLMProducer[Hint](apiKey = key)
    case None      => FileBasedProducer.forHint
