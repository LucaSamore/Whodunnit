package model.casegeneration

trait ProductionError:
  def message: String

object ProductionError:
  case class LLMError(message: String) extends ProductionError
  case class ParseError(message: String) extends ProductionError
  case class ConfigurationError(message: String) extends ProductionError
  case class NetworkError(message: String) extends ProductionError
