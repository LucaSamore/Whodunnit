package model.generation

import model.generation.SystemPrompt.Base

/** Groq-based implementation of Producer using LLM generation.
  *
  * GroqLLMProducer combines prompt building, API communication, and response parsing to generate domain objects (Case,
  * Hint) from natural language specifications. It uses parameterized prompts to guide the LLM.
  *
  * @param apiKey
  *   Groq API authentication key
  * @param systemPrompt
  *   the system-level prompt defining the LLM's role
  * @param userPrompt
  *   the task-specific prompt template
  * @param parser
  *   response parser for the target type (implicit)
  * @tparam T
  *   the type of object to produce
  */
class GroqLLMProducer[T](apiKey: String)(systemPrompt: SystemPrompt = Base, userPrompt: UserPrompt)(using
    parser: ResponseParser[T]
) extends BaseLLMClient(apiKey) with GroqProvider with Producer[T]:

  import GroqProvider.model

  /** Generates content by constructing prompts from constraints and invoking the LLM.
    *
    * @param constraints
    *   generation parameters (difficulty, theme, ranges, etc.)
    * @return
    *   Right(generated object) on success, Left(error) on failure
    */
  override def produce(constraints: Constraint*): Either[ProductionError, T] =
    val params = Seq(Prompt.Parameter(Prompt.Placeholder.Constraints, constraints.map(_.toPromptDescription)))
    for
      systemPrompt <- systemPrompt.build()
      userPrompt <- userPrompt.build(params)
      request = GroqRequest(
        model = model,
        messages = List(
          GroqMessage("system", systemPrompt),
          GroqMessage("user", userPrompt)
        )
      )
      result <- invoke[T](request)(using parser)
    yield result
