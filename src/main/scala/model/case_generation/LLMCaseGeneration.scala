package model.case_generation

import scala.io.Source
import scala.util.{Try, Using}

trait LLMService:
  def generateCase(prompt: String): Either[GenerationError.LLMError, String]

class LLMCaseGenerator(
    llmService: LLMService,
    parser: Parser
) extends CaseGenerator:

  def generate(constraints: Constraint*): Either[GenerationError, Case] =
    for
      prompt <- buildPrompt(constraints)
      jsonResponse <- llmService.generateCase(prompt)
      parsedCase <- {
        println(s"Generated case: $jsonResponse")
        val cleanedJson = cleanJson(jsonResponse)
        parser.parse(cleanedJson).left.map(GenerationError.ParseFailureError.apply)
      }
    yield parsedCase

  private def buildPrompt(constraints: Seq[Constraint])
      : Either[GenerationError, String] =
    loadPromptTemplate() match
      case Right(template) =>
        val constraintsText =
          constraints.map(_.toPromptDescription).mkString("\n- ", "\n- ", "")
        Right(template.replace("{{CONSTRAINTS}}", constraintsText))
      case Left(error) => Left(error)

  private def cleanJson(response: String): String =
    response
      .replaceAll("(?s).*?\\{", "{")
      .replaceAll("```", "")
      .trim

  private def loadPromptTemplate(): Either[GenerationError, String] =
    Try {
      Using.resource(
        getClass.getResourceAsStream("/prompts/case_generation_prompt.txt")
      ) { stream =>
        Source.fromInputStream(stream).mkString
      }
    }.toEither.left.map(e =>
      GenerationError.InvalidPromptError(
        s"Failed to load prompt template: ${e.getMessage}"
      )
    )

object LLMCaseGenerator:
  def apply(llmService: LLMService, parser: Parser): LLMCaseGenerator =
    new LLMCaseGenerator(llmService, parser)

case class GroqRequest(
    messages: List[Message],
    model: String,
    temperature: Double = 0.7,
    max_tokens: Int = 4000
)

import io.circe.generic.auto._

case class Message(role: String, content: String)
case class GroqResponse(choices: List[Choice])
case class Choice(message: Message)

import sttp.client3._

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
