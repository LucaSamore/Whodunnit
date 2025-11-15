package model.generation

/** Mixin trait providing Groq API integration for LLM clients.
  *
  * GroqProvider implements the HTTP communication layer for the Groq API. It requires the mixing class to extend
  * BaseLLMClient.
  */
trait GroqProvider:
  self: BaseLLMClient =>

  import sttp.client3.*
  import io.circe.generic.auto.*
  import sttp.client3.circe.*
  import io.circe.syntax.*

  type Request = GroqRequest

  private val baseURL = uri"https://api.groq.com/openai/v1/chat/completions"
  private val backend = HttpClientSyncBackend()

  /** Makes a HTTP call to the Groq API.
    *
    * @param req
    *   the Groq-specific request containing messages and model parameters
    * @return
    *   Right(response content) on success, Left(error) on API or network failure
    */
  protected def makeCall(req: Request): Either[ProductionError, String] =
    val sttpRequest = basicRequest
      .post(baseURL)
      .header("Authorization", s"Bearer $apiKey")
      .header("Content-Type", "application/json")
      .body(req.asJson.noSpaces)
      .response(asJson[GroqResponse])

    sttpRequest.send(backend).body match
      case Right(groqResponse) =>
        groqResponse.choices.headOption
          .map(_.message.content)
          .toRight(ProductionError.LLMError("No response content from Groq"))
      case Left(error) =>
        Left(
          ProductionError.NetworkError(s"Groq API error: ${error.getMessage}")
        )

  /** Request structure for Groq API calls.
    *
    * @param messages
    *   conversation history (system + user messages)
    * @param model
    *   Groq model identifier
    * @param temperature
    *   sampling temperature (0.0 = deterministic, 1.0 = creative)
    * @param max_tokens
    *   maximum response length
    */
  protected case class GroqRequest(
      messages: List[GroqMessage],
      model: String,
      temperature: Double = 0.7,
      max_tokens: Int = 4000
  )

  /** A single message in the conversation.
    *
    * @param role
    *   message role ("system", "user", or "assistant")
    * @param content
    *   message text
    */
  protected case class GroqMessage(role: String, content: String)

  private case class GroqResponse(choices: List[GroqChoice])

  private case class GroqChoice(message: GroqMessage)

/** Companion object providing environment-based configuration. */
object GroqProvider:
  import io.github.cdimascio.dotenv.Dotenv

  private val dotenv = Dotenv.configure().ignoreIfMissing().load()

  private def getEnvVar(key: String): Option[String] = Option(dotenv.get(key)).orElse(sys.env.get(key))

  /** Retrieves the Groq API key from environment variables.
    *
    * Checks both .env file (via dotenv) and system environment variables.
    *
    * @return
    *   Some(api key) if found and non-empty, None otherwise
    */
  def apiKey: Option[String] = getEnvVar("GROQ_API_KEY").filter(_.trim.nonEmpty)

  /** Retrieves the Groq model name from environment variables.
    *
    * @return
    *   the configured model name, or "openai/gpt-oss-120b" as default
    */
  def model: String = getEnvVar("GROQ_MODEL").getOrElse("openai/gpt-oss-120b")
