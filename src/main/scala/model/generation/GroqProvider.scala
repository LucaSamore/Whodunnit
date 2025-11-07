package model.generation

trait GroqProvider:
  self: BaseLLMClient =>

  import sttp.client3.*
  import io.circe.generic.auto.*
  import sttp.client3.circe.*
  import io.circe.syntax.*

  type Request = GroqRequest

  private val baseURL = uri"https://api.groq.com/openai/v1/chat/completions"
  private val backend = HttpClientSyncBackend()

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

  protected case class GroqRequest(
      messages: List[GroqMessage],
      model: String,
      temperature: Double = 0.7,
      max_tokens: Int = 4000
  )

  protected case class GroqMessage(role: String, content: String)

  private case class GroqResponse(choices: List[GroqChoice])

  private case class GroqChoice(message: GroqMessage)

object GroqProvider:
  import io.github.cdimascio.dotenv.Dotenv

  private val dotenv = Dotenv.configure().ignoreIfMissing().load()

  private def getEnvVar(key: String): Option[String] =
    Option(dotenv.get(key)).orElse(sys.env.get(key))

  def apiKey: String =
    getEnvVar("GROQ_API_KEY")
      .getOrElse(throw IllegalStateException("GROQ_API_KEY not found"))

  def model: String =
    getEnvVar("GROQ_MODEL")
      .getOrElse("llama-3.1-8b-instant")
