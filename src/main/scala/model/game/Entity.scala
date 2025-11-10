package model.game

import upickle.default._

sealed trait Entity derives ReadWriter

final case class Character(name: String, role: CaseRole) extends Entity

final case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[String]
) extends Entity

final case class CustomEntity(entityType: String, content: Option[String]) extends Entity

enum CaseRole derives ReadWriter:
  case Suspect
  case Victim
  case Witness
  case Investigator
  case Accomplice
  case Informant

enum CaseFileType derives ReadWriter:
  case Message
  case Email
  case Interview
  case Diary
  case TextDocument
  case Notes
