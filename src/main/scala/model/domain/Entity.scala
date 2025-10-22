package model.domain

import model.domain.types.CaseRole

sealed trait Entity

case class Character(
    name: String,
    role: CaseRole
) extends Entity
