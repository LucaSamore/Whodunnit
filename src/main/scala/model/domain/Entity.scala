package model.domain

import model.domain.types.CaseRole
import model.domain.types.CaseFileType

import java.time.LocalDateTime

sealed trait Entity

case class Character(
    name: String,
    role: CaseRole
) extends Entity

case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[LocalDateTime]
) extends Entity

case class CustomEntity(
    entityType: String,
    content: Option[String]
) extends Entity
