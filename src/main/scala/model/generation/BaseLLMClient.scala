package model.generation

/** Base class for LLM API clients.
  *
  * Provides a template for making API calls to language models and parsing responses. Subclasses define the specific
  * request format and implement the actual HTTP communication.
  *
  * @param apiKey
  *   the API authentication key
  */
abstract class BaseLLMClient(protected val apiKey: String):
  /** The request type specific to this client implementation. */
  type Request

  /** Invokes the LLM with a request and parses the response.
    *
    * This method orchestrates the full request-response cycle: makes the API call and parses the result using the
    * provided parser.
    *
    * @param req
    *   the request to send
    * @param parser
    *   the parser to decode the response (implicit)
    * @tparam T
    *   the expected response type
    * @return
    *   Right(parsed result) on success, Left(error) on failure
    */
  final def invoke[T](req: Request)(using parser: ResponseParser[T]): Either[ProductionError, T] =
    for
      rawResponse <- makeCall(req)
      res <- parser.parse(rawResponse)
    yield res

  /** Executes the HTTP call to the LLM API.
    *
    * Subclasses must implement this method to handle the specific API protocol.
    *
    * @param req
    *   the request to send
    * @return
    *   Right(raw JSON string) on success, Left(error) on failure
    */
  protected def makeCall(req: Request): Either[ProductionError, String]
