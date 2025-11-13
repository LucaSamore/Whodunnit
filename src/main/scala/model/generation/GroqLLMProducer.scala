package model.generation

import model.generation.SystemPrompt.Base

final class GroqLLMProducer[T](apiKey: String)(systemPrompt: SystemPrompt = Base, userPrompt: UserPrompt)(using
    parser: ResponseParser[T]
) extends BaseLLMClient(apiKey) with GroqProvider with Producer[T]:

  import GroqProvider.model

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
