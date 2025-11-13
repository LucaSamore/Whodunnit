package model.generation

import model.game.{Case, Hint}
import upickle.default.*

import scala.io.Source
import scala.util.{Try, Using}
import scala.reflect.ClassTag

final class FileBasedProducer[T]()(using parser: ResponseParser[T], ct: ClassTag[T]) extends Producer[T]:

  override def produce(constraints: Constraint*): Either[ProductionError, T] =
    val filename = determineFilename[T]
    val resourcePath = s"cases/$filename"

    loadResourceAsString(resourcePath) match
      case Some(jsonContent) =>
        parser.parse(jsonContent)
      case None =>
        Left(ProductionError.ParseError(s"Resource file not found: $resourcePath"))

  private def determineFilename[A](using ct: ClassTag[A]): String =
    ct.runtimeClass.getSimpleName match
      case "Case" => "case.json"
      case "Hint" => "hints.json"
      case other  => throw new IllegalArgumentException(s"Unknown type: $other")

  private def loadResourceAsString(resourcePath: String): Option[String] =
    Try {
      Using.resource(getClass.getClassLoader.getResourceAsStream(resourcePath)) { stream =>
        if (stream != null) Some(Source.fromInputStream(stream).mkString)
        else None
      }
    }.toOption.flatten

object FileBasedProducer:
  import ResponseParser.given

  def forCase: Producer[Case] = new FileBasedProducer[Case]()
  def forHint: Producer[Hint] = new FileBasedProducer[Hint]()
