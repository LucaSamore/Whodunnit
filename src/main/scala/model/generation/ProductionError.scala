package model.generation

/** Base trait for all errors that can occur during content generation.
  *
  * ProductionErrors capture failures at different stages: LLM API communication, response parsing, configuration
  * loading, or network issues.
  */
trait ProductionError:
  /** Human-readable description of the error.
    *
    * @return
    *   the error message
    */
  def message: String

/** Companion object defining specific error types. */
object ProductionError:
  /** Error returned by the LLM API or due to invalid LLM response format.
    *
    * @param message
    *   description of the LLM error
    */
  case class LLMError(message: String) extends ProductionError

  /** Error during parsing of LLM response into domain objects.
    *
    * @param message
    *   description of the parsing failure
    */
  case class ParseError(message: String) extends ProductionError

  /** Error loading configuration files or resources (e.g., prompt templates).
    *
    * @param message
    *   description of the configuration error
    */
  case class ConfigurationError(message: String) extends ProductionError

  /** Error during HTTP communication with the LLM API.
    *
    * @param message
    *   description of the network failure
    */
  case class NetworkError(message: String) extends ProductionError
