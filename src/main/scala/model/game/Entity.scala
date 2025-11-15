package model.game

import upickle.default._

/** Base trait for all entities that can appear in a case or knowledge graph.
  *
  * Entities can be case-provided (Character, CaseFile) or player-created (CustomEntity). They serve as nodes in the
  * player's knowledge graph.
  */
sealed trait Entity derives ReadWriter

/** An individual involved in the case.
  *
  * @param name
  *   the character's name
  * @param role
  *   their function in the narrative
  */
final case class Character(name: String, role: CaseRole) extends Entity

/** A piece of evidence or information document.
  *
  * Case files represent various types of documents (emails, interviews, diaries, etc.) that contain clues necessary to
  * solve the mystery. Metadata such as sender, receiver, and date may be present depending on the document type.
  *
  * @param title
  *   the document heading
  * @param content
  *   the full text of the document
  * @param kind
  *   the type of document
  * @param sender
  *   the character who created or sent the document, if exists
  * @param receiver
  *   the character who received the document, if exists
  * @param date
  *   temporal information about when the document was created, if exists
  */
final case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[String]
) extends Entity

/** A player-created entity for the knowledge graph.
  *
  * Custom entities allow players to represent locations, objects, or concepts that are not explicitly provided in the
  * case data but are relevant to their investigation and hypotheses.
  *
  * @param entityType
  *   a descriptive label for what this entity represents (e.g., "Crime Scene", "Murder Weapon")
  */
final case class CustomEntity(entityType: String) extends Entity

/** The narrative function of a character in the case. */
enum CaseRole derives ReadWriter:
  case Suspect
  case Victim
  case Witness
  case Investigator
  case Accomplice
  case Informant

/** Classification of evidence documents. */
enum CaseFileType derives ReadWriter:
  case Message
  case Email
  case Interview
  case Diary
  case TextDocument
  case Notes
