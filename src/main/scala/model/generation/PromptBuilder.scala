package model.generation

/** Template-based prompt builder that loads and parameterizes prompts from resources.
  *
  * Prompts are stored as template files with placeholder tags (e.g., {{CONSTRAINTS}}) that are replaced with actual
  * values at runtime. This allows for flexible, maintainable prompt engineering without hardcoding text in code.
  *
  * @param path
  *   the resource path to the template file
  */
trait Prompt(path: String):
  import scala.io.Source
  import scala.util.{Try, Using}

  /** Builds a complete prompt by loading the template and substituting parameters.
    *
    * @param params
    *   key-value pairs where placeholders will be replaced with their corresponding values
    * @return
    *   Right(completed prompt string) on success, Left(error) if template loading fails
    */
  def build(params: Seq[Prompt.Parameter] = Seq.empty): Either[ProductionError, String] = loadTemplate(path).map {
    template =>
      params.foldLeft(template) { case (acc, parameter) =>
        acc.replace(parameter.placeholder.tag, parameter.values.mkString("\n"))
      }
  }

  private def loadTemplate(path: String): Either[ProductionError, String] =
    Try:
      Using.resource(getClass.getResourceAsStream(path)): stream =>
        Source.fromInputStream(stream).mkString
    .toEither
      .left
      .map(e => ProductionError.ConfigurationError(s"Failed to load template: ${e.getMessage}"))

/** System-level prompts that define the LLM's role and behavior. */
enum SystemPrompt(path: String) extends Prompt(path):
  /** Base system prompt defining the LLM's role as a mystery case generator. */
  case Base extends SystemPrompt("/prompts/system/base.md")

/** User-facing prompts for specific generation tasks. */
enum UserPrompt(path: String) extends Prompt(path):
  /** Prompt template for generating complete investigative cases. */
  case Case extends UserPrompt("/prompts/user/case-generation.md")

  /** Prompt template for generating hints during gameplay. */
  case Hint extends UserPrompt("/prompts/user/hint-generation.md")

/** Companion object defining prompt parameter types. */
object Prompt:
  /** A parameter that replaces a placeholder in a template with actual values.
    *
    * @param placeholder
    *   the placeholder tag to replace
    * @param values
    *   the strings to substitute (joined with newlines)
    */
  final case class Parameter(placeholder: Placeholder, values: Seq[String])

  /** Predefined placeholder tags used in prompt templates. */
  enum Placeholder(val tag: String):
    /** Placeholder for constraint descriptions in generation prompts. */
    case Constraints extends Placeholder("{{CONSTRAINTS}}")

    /** Empty placeholder for prompts without parameters. */
    case None extends Placeholder("")
