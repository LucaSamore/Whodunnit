package model.case_generation

object Generators:
  given groqCaseGenerator: CaseGenerator = defaultGenerator
  
  private lazy val defaultGenerator: CaseGenerator =
    GroqLLMService.fromEnv() match
      case Right(service) => LLMCaseGenerator(service, JsonParser)
      case Left(error) => throw IllegalStateException(error.message)

