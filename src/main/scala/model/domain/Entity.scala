package model.domain

import model.domain.types.CaseRole
import model.domain.types.CaseFileType

import java.util.Date

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
    date: Option[Date]
) extends Entity

case class CustomEntity(
    entityType: String,
    content: Option[String]
) extends Entity
