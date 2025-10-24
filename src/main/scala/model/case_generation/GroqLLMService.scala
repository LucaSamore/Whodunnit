package model.case_generation

import sttp.client3.*
import io.circe.generic.auto.*

case class GroqRequest(
                        messages: List[Message],
                        model: String,
                        temperature: Double = 0.7,
                        max_tokens: Int = 4000
                      )

case class Message(role: String, content: String)
case class GroqResponse(choices: List[Choice])
case class Choice(message: Message)

class GroqLLMService(
                      apiKey: String,
                      model: String,
                      backend: SttpBackend[Identity, Any]
                    ) extends LLMService:

  import sttp.client3.circe._
  import io.circe.syntax._

  private val groqUri = uri"https://api.groq.com/openai/v1/chat/completions"

  def generateCase(prompt: String): Either[GenerationError.LLMError, String] =
    val request = GroqRequest(
      messages = List(
        Message(
          "system",
          "You are a mystery game master. Generate cases in JSON format."
        ),
        Message("user", prompt)
      ),
      model = model
    )

    val sttpRequest = basicRequest
      .post(groqUri)
      .header("Authorization", s"Bearer $apiKey")
      .header("Content-Type", "application/json")
      .body(request.asJson.noSpaces)
      .response(asJson[GroqResponse])

    sttpRequest.send(backend).body match
      case Right(groqResponse) =>
        groqResponse.choices.headOption
          .map(_.message.content)
          .toRight(GenerationError.LLMError("No response content from Groq"))
      case Left(error) =>
        Left(GenerationError.LLMError(s"Groq API error: ${error.getMessage}"))

object GroqLLMService:
  def fromEnv(): Either[GenerationError, GroqLLMService] =
    import io.github.cdimascio.dotenv.Dotenv

    val dotenv = Dotenv.configure()
      .ignoreIfMissing()
      .load()

    Option(dotenv.get("GROQ_API_KEY"))
      .orElse(sys.env.get("GROQ_API_KEY"))
      .toRight(GenerationError.InvalidPromptError(
        "GROQ_API_KEY not found in .env file or environment variables. " +
          "See .env.example for setup instructions."
      ))
      .map { apiKey =>
        val model = Option(dotenv.get("GROQ_MODEL"))
          .orElse(sys.env.get("GROQ_MODEL"))
          .getOrElse("llama-3.1-8b-instant")

        val backend = HttpClientSyncBackend()
        new GroqLLMService(apiKey, model, backend)
      }
