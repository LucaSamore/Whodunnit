package model.casegeneration

abstract class BaseLLMClient(protected val apiKey: String):
  type Request

  final def invoke[T](req: Request)(using
      parser: ResponseParser[T]
  ): Either[ProductionError, T] =
    for
      rawResponse <- makeCall(req)
      res <- parser.parse(rawResponse)
    yield res

  protected def makeCall(req: Request): Either[ProductionError, String]
