package model.game

import model.generation.{Constraint, Producer, ProductionError}
import upickle.ReadWriter

/** Represents a hint in the investigative game.
  *
  * A hint provides guidance to the player during their investigation. Hints can be helpful (guiding toward the
  * solution) or misleading (leading players astray), depending on the game's progression and the rules evaluated by the
  * [[model.hint.HintEngine]].
  *
  * Hints are typically generated dynamically by analyzing the player's investigation history and applying predefined
  * rules defined in the [[model.hint.RuleDSL]].
  *
  * @see
  *   [[model.hint.HintEngine]] for hint generation logic
  * @see
  *   [[model.generation.HintKind]] for hint types (Helpful or Misleading)
  */
trait Hint:
  /** The textual description of the hint provided to the player.
    *
    * @return
    *   a string containing the hint message
    */
  def description: String

/** Factory object for creating [[Hint]] instances.
  *
  * Provides a convenient apply method that uses the implicit [[Producer]] to generate hints based on provided
  * constraints. The actual production mechanism is determined by the implicit Producer in scope.
  *
  * @example
  *   {{{
  * import model.generation.{HintKind, Producer}
  * import model.generation.Producers.given
  *
  * // Generate a helpful hint with context
  * val result = Hint(
  *   HintKind.Helpful,
  *   Context("The player is stuck on identifying the motive")
  * )
  *
  * result match
  *   case Right(hint) => println(hint.description)
  *   case Left(error) => println(s"Failed to generate hint: ${error.message}")
  *   }}}
  * @see
  *   [[model.generation.Producer]] for hint generation strategies
  * @see
  *   [[model.generation.Constraint]] for available constraints
  */
object Hint:
  /** Creates a hint using the implicit producer and provided constraints.
    *
    * @param constraints
    *   variable number of constraints to guide hint generation (e.g., [[model.generation.HintKind.Helpful]],
    *   [[model.generation.Context]])
    * @param producer
    *   implicit producer that handles the actual hint generation (LLM-based or file-based)
    * @return
    *   Either a [[model.generation.ProductionError]] if generation fails, or a successfully generated [[Hint]]
    */
  def apply(constraints: Constraint*)(using producer: Producer[Hint]): Either[ProductionError, Hint] =
    producer.produce(constraints*)

private[model] final case class HintImpl(override val description: String) extends Hint derives ReadWriter
