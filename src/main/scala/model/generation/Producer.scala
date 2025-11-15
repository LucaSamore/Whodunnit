package model.generation

import model.game.{Case, Hint}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/** Strategy for generating domain objects from constraints.
  *
  * Producer implementations can use LLM APIs, file-based templates, or any other generation mechanism. The trait
  * provides both synchronous and asynchronous generation methods.
  *
  * @tparam T
  *   the type of object to produce
  */
trait Producer[T]:

  given ExecutionContext = ExecutionContext.global

  /** Generates content synchronously based on constraints.
    *
    * @param constraints
    *   generation parameters
    * @return
    *   Right(generated object) on success, Left(error) on failure
    */
  def produce(constraints: Constraint*): Either[ProductionError, T]

  /** Generates content asynchronously.
    *
    * This method wraps the synchronous produce call in a Future for non-blocking execution.
    *
    * @param constraints
    *   generation parameters
    * @return
    *   a Future containing the generation result
    */
  def produceAsync(constraints: Constraint*): Future[Either[ProductionError, T]] = Future {
    produce(constraints*)
  }

/** Companion object providing default Producer instances. */
object Producers:
  import ResponseParser.given

  /** Default Case producer.
    *
    * Uses GroqLLMProducer if GROQ_API_KEY is configured, otherwise back to FileBasedProducer otherwise.
    */
  given Producer[Case] = GroqProvider.apiKey match
    case Some(key) => new GroqLLMProducer[Case](apiKey = key)(userPrompt = UserPrompt.Case)
    case None      => FileBasedProducer.forCase

  /** Default Hint producer.
    *
    * Uses GroqLLMProducer if GROQ_API_KEY is configured, otherwise back to FileBasedProducer otherwise.
    */
  given Producer[Hint] = GroqProvider.apiKey match
    case Some(key) => new GroqLLMProducer[Hint](apiKey = key)(userPrompt = UserPrompt.Hint)
    case None      => FileBasedProducer.forHint
