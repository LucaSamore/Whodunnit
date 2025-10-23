package model

sealed trait Constraints

object Constraints:
  final case class Theme(value: String) extends Constraints

  sealed trait ConstraintError:
    def message: String

  case class EmptyTheme(message: String = "Theme cannot be empty") extends ConstraintError

  object validated:
    def theme(value: String): Either[ConstraintError, Theme] =
      if value.trim.isEmpty then Left(EmptyTheme())
      else
        Right(Theme(value))