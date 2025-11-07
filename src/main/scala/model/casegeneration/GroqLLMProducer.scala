package model.casegeneration

class GroqLLMProducer[T](
    apiKey: String
)(using parser: ResponseParser[T], promptBuilder: PromptBuilder[T])
    extends BaseLLMClient(apiKey)
    with GroqProvider
    with Producer[T]:

  import GroqProvider.model

  def produce(constraints: Constraint*): Either[ProductionError, T] =
    for
      userPrompt <- promptBuilder.build(constraints*)
      request = GroqRequest(
        messages = List(
          GroqMessage("system", promptBuilder.systemPrompt),
          GroqMessage("user", userPrompt)
        ),
        model = model
      )
      result <- invoke[T](request)(using parser)
    yield result
