package model.generation

import model.game
import model.game.{CaseFile, CaseFileType, CaseRole, Solution}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResponseParserTest extends AnyWordSpec with Matchers with EitherValues
    with OptionValues:

  object CaseJsonFixtures:
    val validPlot: String =
      """
      "plot": {
        "title": "Mystery at Dawn",
        "content": "A mysterious case begins at the old mansion"
      }
      """

    val singleCharacter: String =
      """
      "characters": [
        {"name": "Alice", "role": "Suspect"}
      ]
      """

    val multipleCharacters: String =
      """
      "characters": [
        {"name": "Alice", "role": "Suspect"},
        {"name": "Bob", "role": "Victim"},
        {"name": "Charlie", "role": "Witness"}
      ]
      """

    val invalidRoleCharacter: String =
      """
      "characters": [
        {"name": "Alice", "role": "InvalidRole"}
      ]
      """

    val singleCaseFile: String =
      """
      "caseFiles": [
        {
          "title": "Threatening Email",
          "content": "You will pay for what you did",
          "kind": "Email",
          "sender": "Alice",
          "receiver": "Bob",
          "date": "2025-10-20T14:30:00"
        }
      ]
      """

    val caseFileWithNullFields: String =
      """
      "caseFiles": [
        {
          "title": "Threatening Email",
          "content": "Meet me at midnight",
          "kind": "Notes",
          "sender": null,
          "receiver": null,
          "date": null
        }
      ]
      """

    val caseFileWithInvalidType: String =
      """
      "caseFiles": [
        {
          "title": "Document",
          "content": "Evidence",
          "kind": "InvalidType",
          "sender": null,
          "receiver": null,
          "date": null
        }
      ]
      """

    val caseFileWithInvalidDate: String =
      """
      "caseFiles": [
        {
          "title": "Email",
          "content": "Content",
          "kind": "Email",
          "sender": null,
          "receiver": null,
          "date": "not-a-valid-date"
        }
      ]
      """

    val multipleCaseFiles: String =
      """
      "caseFiles": [
        {
          "title": "Threatening Email",
          "content": "First evidence",
          "kind": "Email",
          "sender": "Alice",
          "receiver": "Bob",
          "date": "2025-10-20T14:30:00"
        },
        {
          "title": "Note",
          "content": "Second evidence",
          "kind": "Notes",
          "sender": null,
          "receiver": null,
          "date": null
        }
      ]
      """

    val validSolution: String =
      """
      "solution": {
        "prerequisite": [
          {
            "firstEntity": "Alice",
            "secondEntity": "Threatening Email",
            "semantic": "sent"
          }
        ],
        "culprit": "Alice",
        "motive": "Revenge for past betrayal"
      }
      """

    val solutionWithMultiplePrerequisites: String =
      """
      "solution": {
        "prerequisite": [
          {
            "firstEntity": "Alice",
            "secondEntity": "Threatening Email",
            "semantic": "sent"
          },
          {
            "firstEntity": "Bob",
            "secondEntity": "Note",
            "semantic": "received"
          }
        ],
        "culprit": "Alice",
        "motive": "Revenge"
      }
      """

    val solutionWithInvalidCulprit: String =
      """
      "solution": {
        "prerequisite": [],
        "culprit": "NonExistentPerson",
        "motive": "Unknown"
      }
      """

    val solutionWithInvalidEntity: String =
      """
      "solution": {
        "prerequisite": [
          {
            "firstEntity": "NonExistent",
            "secondEntity": "Threatening Email",
            "semantic": "sent"
          }
        ],
        "culprit": "Alice",
        "motive": "Revenge"
      }
      """

    def wrapInMarkdownCodeBlock(json: String): String =
      s"```json\n$json\n```"

    def buildJson(parts: String*): String =
      parts.mkString("{", ",", "}")

  import CaseJsonFixtures.*

  "ResponseParser" when:
    "parsing a complete valid case" should:
      "successfully parse all fields" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.plot.title shouldBe "Mystery at Dawn"
        caseModel.plot.content shouldBe "A mysterious case begins at the old mansion"
        caseModel.characters should have size 1
        caseModel.caseFiles should have size 1
        caseModel.solution.culprit.name shouldBe "Alice"
        caseModel.solution.motive shouldBe "Revenge for past betrayal"

    "parsing plot" should:
      "extract title and content correctly" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val plot = result.value.plot
        plot.title shouldBe "Mystery at Dawn"
        plot.content shouldBe "A mysterious case begins at the old mansion"

    "parsing characters" should:
      "extract a single character with valid role" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val characters = result.value.characters
        characters should have size 1
        characters.find(_.name == "Alice") match
          case Some(alice) => alice.role shouldBe CaseRole.Suspect
          case None        => fail("Character Alice not found")

    "extract multiple characters with different roles" in:
      val jsonStr = buildJson(
        validPlot,
        multipleCharacters,
        singleCaseFile,
        validSolution
      )
      val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

      result shouldBe a[Right[_, _]]
      val characters = result.value.characters
      characters should have size 3
      characters.map(_.name) should contain allOf ("Alice", "Bob", "Charlie")
      characters.find(_.name == "Alice").value.role shouldBe CaseRole.Suspect
      characters.find(_.name == "Bob").value.role shouldBe CaseRole.Victim
      characters.find(_.name == "Charlie").value.role shouldBe CaseRole.Witness

    "reject character with invalid role" in:
      val jsonStr = buildJson(
        validPlot,
        invalidRoleCharacter,
        singleCaseFile,
        validSolution
      )
      val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

      result shouldBe a[Left[_, _]]
      result.left.value shouldBe a[ProductionError.ParseError]
      result.left.value.message should include("Invalid CaseRole")
      result.left.value.message should include("InvalidRole")

    "parsing case files" should:
      "extract case file with all fields present" in:
        val jsonStr = buildJson(
          validPlot,
          multipleCharacters,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles
        caseFiles should have size 1
        val email = caseFiles.find(_.title == "Threatening Email").value
        email.content shouldBe "You will pay for what you did"
        email.kind shouldBe CaseFileType.Email
        email.sender.value.name shouldBe "Alice"
        email.receiver.value.name shouldBe "Bob"
        email.date shouldBe defined

      "extract case file with null optional fields" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          caseFileWithNullFields,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles
        caseFiles should have size 1
        val note = caseFiles.find(_.title == "Threatening Email").value
        note.sender shouldBe None
        note.receiver shouldBe None
        note.date shouldBe None

      "extract multiple case files" in:
        val jsonStr = buildJson(
          validPlot,
          multipleCharacters,
          multipleCaseFiles,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles
        caseFiles should have size 2
        caseFiles.map(_.title) should contain allOf (
          "Threatening Email",
          "Note"
        )

      "reject case file with invalid type" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          caseFileWithInvalidType,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("Invalid CaseFileType")
        result.left.value.message should include("InvalidType")

      "reject case file with invalid date format" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          caseFileWithInvalidDate,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("Invalid date format")
        result.left.value.message should include("not-a-valid-date")

      "correctly resolve sender and receiver from characters" in:
        val jsonStr = buildJson(
          validPlot,
          multipleCharacters,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val characters = result.value.characters
        val caseFile = result.value.caseFiles.head
        caseFile.sender should contain(characters.find(_.name == "Alice").value)
        caseFile.receiver should contain(characters.find(_.name == "Bob").value)

    "parsing solution" should:
      "extract culprit and motive correctly" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution.asInstanceOf[Solution]
        solution.culprit.name shouldBe "Alice"
        solution.motive shouldBe "Revenge for past betrayal"

      "extract single prerequisite correctly" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution.asInstanceOf[Solution]
        solution.prerequisite should have size 1
        val prereq = solution.prerequisite.head
        prereq.firstEntity match
          case char: game.Character => char.name shouldBe "Alice"
          case _ => fail("First entity should be a Character")
        prereq.secondEntity match
          case file: CaseFile => file.title shouldBe "Threatening Email"
          case _              => fail("Second entity should be a CaseFile")
        prereq.semantic shouldBe "sent"

      "extract multiple prerequisites correctly" in:
        val jsonStr = buildJson(
          validPlot,
          multipleCharacters,
          multipleCaseFiles,
          solutionWithMultiplePrerequisites
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution.asInstanceOf[Solution]
        solution.prerequisite should have size 2

      "reject solution with non-existent culprit" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          solutionWithInvalidCulprit
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("Culprit not found")
        result.left.value.message should include("NonExistentPerson")

      "reject solution with non-existent entity in prerequisites" in:
        val jsonStr = buildJson(
          validPlot,
          singleCharacter,
          singleCaseFile,
          solutionWithInvalidEntity
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("Entity not found")
        result.left.value.message should include("NonExistent")

      "correctly resolve entities as both characters and case files" in:
        val jsonStr = buildJson(
          validPlot,
          multipleCharacters,
          singleCaseFile,
          validSolution
        )
        val result = ResponseParser.given_ResponseParser_Case.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution.asInstanceOf[Solution]
        val prereq = solution.prerequisite.head
        prereq.firstEntity shouldBe a[game.Character]
        prereq.secondEntity shouldBe a[CaseFile]

    "parsing malformed JSON" should:
      "reject completely invalid JSON" in:
        val invalidJson = """{ this is not valid json }"""
        val result = ResponseParser.given_ResponseParser_Case.parse(invalidJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]

      "reject JSON with missing required fields" in:
        val incompleteJson = buildJson(validPlot)
        val result =
          ResponseParser.given_ResponseParser_Case.parse(incompleteJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]

      "reject empty JSON object" in:
        val emptyJson = "{}"
        val result = ResponseParser.given_ResponseParser_Case.parse(emptyJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]

      "reject JSON with wrong field types" in:
        val wrongTypeJson = buildJson(
          """"plot": "should be an object not a string"""",
          singleCharacter,
          singleCaseFile,
          validSolution
        )
        val result =
          ResponseParser.given_ResponseParser_Case.parse(wrongTypeJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
