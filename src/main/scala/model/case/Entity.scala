package model.domain

import model.domain.types.CaseRole
import model.domain.types.CaseFileType

import java.time.LocalDateTime

sealed trait Entity

final case class Character(
    name: String,
    role: CaseRole
) extends Entity

final case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[LocalDateTime]
) extends Entity

final case class CustomEntity(
    entityType: String,
    content: Option[String]
) extends Entity
