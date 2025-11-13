package model.generation

sealed trait Prompt(path: String):
  import scala.io.Source
  import scala.util.{Try, Using}

  def build(params: Seq[Prompt.Parameter] = Seq.empty): Either[ProductionError, String] = loadTemplate(path).map {
    template =>
      params.foldLeft(template) { case (acc, parameter) =>
        acc.replace(parameter.placeholder.placeholder, parameter.values.mkString("\n"))
      }
  }

  private def loadTemplate(path: String): Either[ProductionError, String] =
    Try:
      Using.resource(getClass.getResourceAsStream(path)): stream =>
        Source.fromInputStream(stream).mkString
    .toEither
      .left
      .map(e => ProductionError.ConfigurationError(s"Failed to load template: ${e.getMessage}"))

enum SystemPrompt(path: String) extends Prompt(path):
  case Base extends SystemPrompt("/prompts/system/base.md")

enum UserPrompt(path: String) extends Prompt(path):
  case Case extends UserPrompt("/prompts/user/case-generation.md")
  case Hint extends UserPrompt("/prompts/user/hint-generation.md")

object Prompt:
  final case class Parameter(placeholder: Placeholder, values: Seq[String])

  enum Placeholder(val placeholder: String):
    case Constraints extends Placeholder("{{CONSTRAINTS}}")
    case None extends Placeholder("")
