package model.casegeneration

trait Producer[T]:
  def produce(constraints: Constraint*): Either[ProductionError, T]

//TODO check if works
//extension [T: Producer](companion: T.type)
//  def apply(constraints: Constraint*): T =
//    summon[Producer[T]].produce(constraints *)

object Producers:
  import ResponseParser.given
  import PromptBuilder.given

  given Producer[Case] = new GroqLLMProducer[Case](
    apiKey = GroqProvider.apiKey
  )
