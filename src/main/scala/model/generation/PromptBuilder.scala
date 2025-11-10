package model.generation

import model.game.{Case, Hint}

trait PromptBuilder[T]:
  def systemPrompt: String
  def build(constraints: Constraint*): Either[ProductionError, String]

object PromptBuilder:
  import scala.io.Source
  import scala.util.{Try, Using}

  given PromptBuilder[Case] with
    override val systemPrompt: String =
      "You are a mystery game master. Generate cases in JSON format."

    override def build(constraints: Constraint*): Either[ProductionError, String] =
      loadTemplate("/prompts/case-generation.md").map { template =>
        val expandedConstraints = Constraint.expandConstraints(constraints)
        val constraintsText = expandedConstraints
          .map(_.toPromptDescription)
          .mkString("\n- ", "\n- ", "")
        template.replace("{{CONSTRAINTS}}", constraintsText)
      }

  given PromptBuilder[Hint] with
    override val systemPrompt: String =
      "You are a hint generator for an investigative game. Hints can be helpful or misleading"

    // TODO: refactor duplicate code
    override def build(constraints: Constraint*): Either[ProductionError, String] =
      loadTemplate("/prompts/hintGenerationPrompt.txt").map { template =>
        val expandedConstraints = Constraint.expandConstraints(constraints)
        val constraintsText = expandedConstraints
          .map(_.toPromptDescription)
          .mkString("\n- ", "\n- ", "")
        template.replace("{{CONSTRAINTS}}", constraintsText)

      }

  private def loadTemplate(path: String): Either[ProductionError, String] =
    Try:
      Using.resource(getClass.getResourceAsStream(path)): stream =>
        Source.fromInputStream(stream).mkString
    .toEither
      .left
      .map(e => ProductionError.ConfigurationError(s"Failed to load template: ${e.getMessage}"))
