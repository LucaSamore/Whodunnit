package model.game

import java.time.LocalDateTime

sealed trait Entity

final case class Character(name: String, role: CaseRole) extends Entity

final case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[LocalDateTime]
) extends Entity

final case class CustomEntity(entityType: String, content: Option[String]) extends Entity

enum CaseRole:
  case Suspect
  case Victim
  case Witness
  case Investigator
  case Accomplice
  case Informant

enum CaseFileType:
  case Message
  case Email
  case Interview
  case Diary
  case TextDocument
  case Notes
