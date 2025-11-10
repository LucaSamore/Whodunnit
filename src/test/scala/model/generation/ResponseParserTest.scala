package model.generation

import model.game.*
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResponseParserTest extends AnyWordSpec with Matchers with EitherValues with OptionValues:

  object CaseJsonFixtures:

    val minimalValidCase: String =
      """{
        "plot": {
          "title": "Mystery at Dawn",
          "content": "A mysterious case begins at the old mansion"
        },
        "characters": [
          {
            "$type": "model.game.Character",
            "name": "Alice",
            "role": "Suspect"
          }
        ],
        "caseFiles": [
          {
            "$type": "model.game.CaseFile",
            "title": "Note",
            "content": "Evidence found",
            "kind": "Notes",
            "sender": null,
            "receiver": null,
            "date": null
          }
        ],
        "solution": {
          "prerequisite": {
            "nodes": [
              {
                "$type": "model.game.Character",
                "name": "Alice",
                "role": "Suspect"
              }
            ],
            "edges": []
          },
          "culprit": {
            "$type": "model.game.Character",
            "name": "Alice",
            "role": "Suspect"
          },
          "motive": "Revenge"
        }
      }"""

    val completeCase: String =
      """{
        "plot": {
          "title": "The Midnight Ledger",
          "content": "Elena Marsh, a financial analyst, is found dead in her downtown loft."
        },
        "characters": [
          {
            "$type": "model.game.Character",
            "name": "Elena Marsh",
            "role": "Victim"
          },
          {
            "$type": "model.game.Character",
            "name": "Dylan Hart",
            "role": "Suspect"
          },
          {
            "$type": "model.game.Character",
            "name": "Olivia Reed",
            "role": "Witness"
          }
        ],
        "caseFiles": [
          {
            "$type": "model.game.CaseFile",
            "title": "Contract Discussion",
            "content": "Dylan, I need the payment by Friday.",
            "kind": "Email",
            "sender": {
              "$type": "model.game.Character",
              "name": "Elena Marsh",
              "role": "Victim"
            },
            "receiver": {
              "$type": "model.game.Character",
              "name": "Dylan Hart",
              "role": "Suspect"
            },
            "date": "2025-10-31T09:15:00"
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Meeting Note",
            "content": "Meet at 8pm",
            "kind": "Notes",
            "sender": null,
            "receiver": null,
            "date": null
          }
        ],
        "solution": {
          "prerequisite": {
            "nodes": [
              {
                "$type": "model.game.Character",
                "name": "Dylan Hart",
                "role": "Suspect"
              },
              {
                "$type": "model.game.CaseFile",
                "title": "Contract Discussion",
                "content": "Dylan, I need the payment by Friday.",
                "kind": "Email",
                "sender": {
                  "$type": "model.game.Character",
                  "name": "Elena Marsh",
                  "role": "Victim"
                },
                "receiver": {
                  "$type": "model.game.Character",
                  "name": "Dylan Hart",
                  "role": "Suspect"
                },
                "date": "2025-10-31T09:15:00"
              },
              {
                "$type": "model.game.CustomEntity",
                "entityType": "Motive",
                "content": "Financial debt"
              }
            ],
            "edges": [
              [
                {
                  "$type": "model.game.Character",
                  "name": "Dylan Hart",
                  "role": "Suspect"
                },
                {
                  "semantic": "sent"
                },
                {
                  "$type": "model.game.CaseFile",
                  "title": "Contract Discussion",
                  "content": "Dylan, I need the payment by Friday.",
                  "kind": "Email",
                  "sender": {
                    "$type": "model.game.Character",
                    "name": "Elena Marsh",
                    "role": "Victim"
                  },
                  "receiver": {
                    "$type": "model.game.Character",
                    "name": "Dylan Hart",
                    "role": "Suspect"
                  },
                  "date": "2025-10-31T09:15:00"
                }
              ],
              [
                {
                  "$type": "model.game.Character",
                  "name": "Dylan Hart",
                  "role": "Suspect"
                },
                {
                  "semantic": "has_motive"
                },
                {
                  "$type": "model.game.CustomEntity",
                  "entityType": "Motive",
                  "content": "Financial debt"
                }
              ]
            ]
          },
          "culprit": {
            "$type": "model.game.Character",
            "name": "Dylan Hart",
            "role": "Suspect"
          },
          "motive": "Dylan Hart was deeply indebted and killed to prevent exposure."
        }
      }"""

    val multipleCharactersCase: String =
      """{
        "plot": {
          "title": "The Art Heist",
          "content": "A valuable painting was stolen."
        },
        "characters": [
          {
            "$type": "model.game.Character",
            "name": "Detective Brown",
            "role": "Investigator"
          },
          {
            "$type": "model.game.Character",
            "name": "Museum Owner",
            "role": "Victim"
          },
          {
            "$type": "model.game.Character",
            "name": "Security Guard",
            "role": "Witness"
          },
          {
            "$type": "model.game.Character",
            "name": "Art Dealer",
            "role": "Suspect"
          },
          {
            "$type": "model.game.Character",
            "name": "Assistant",
            "role": "Accomplice"
          }
        ],
        "caseFiles": [
          {
            "$type": "model.game.CaseFile",
            "title": "Security Log",
            "content": "Entry recorded at 2am",
            "kind": "TextDocument",
            "sender": null,
            "receiver": null,
            "date": null
          }
        ],
        "solution": {
          "prerequisite": {
            "nodes": [],
            "edges": []
          },
          "culprit": {
            "$type": "model.game.Character",
            "name": "Art Dealer",
            "role": "Suspect"
          },
          "motive": "Greed"
        }
      }"""

    val allCaseFileTypesCase: String =
      """{
        "plot": {
          "title": "Evidence Collection",
          "content": "Testing all file types"
        },
        "characters": [
          {
            "$type": "model.game.Character",
            "name": "John",
            "role": "Suspect"
          }
        ],
        "caseFiles": [
          {
            "$type": "model.game.CaseFile",
            "title": "Text Message",
            "content": "SMS content",
            "kind": "Message",
            "sender": null,
            "receiver": null,
            "date": null
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Email Message",
            "content": "Email content",
            "kind": "Email",
            "sender": null,
            "receiver": null,
            "date": null
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Interview Transcript",
            "content": "Interview content",
            "kind": "Interview",
            "sender": null,
            "receiver": null,
            "date": null
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Personal Diary",
            "content": "Diary content",
            "kind": "Diary",
            "sender": null,
            "receiver": null,
            "date": null
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Document",
            "content": "Document content",
            "kind": "TextDocument",
            "sender": null,
            "receiver": null,
            "date": null
          },
          {
            "$type": "model.game.CaseFile",
            "title": "Investigation Notes",
            "content": "Notes content",
            "kind": "Notes",
            "sender": null,
            "receiver": null,
            "date": null
          }
        ],
        "solution": {
          "prerequisite": {
            "nodes": [],
            "edges": []
          },
          "culprit": {
            "$type": "model.game.Character",
            "name": "John",
            "role": "Suspect"
          },
          "motive": "Unknown"
        }
      }"""

    val invalidRoleCase: String =
      """{
        "plot": {"title": "Test", "content": "Test"},
        "characters": [
          {
            "$type": "model.game.Character",
            "name": "Alice",
            "role": "InvalidRole"
          }
        ],
        "caseFiles": [],
        "solution": {
          "prerequisite": {"nodes": [], "edges": []},
          "culprit": {"$type": "model.game.Character", "name": "Alice", "role": "Suspect"},
          "motive": "Test"
        }
      }"""

    val invalidCaseFileTypeCase: String =
      """{
        "plot": {"title": "Test", "content": "Test"},
        "characters": [
          {"$type": "model.game.Character", "name": "Alice", "role": "Suspect"}
        ],
        "caseFiles": [
          {
            "$type": "model.game.CaseFile",
            "title": "Test",
            "content": "Test",
            "kind": "InvalidType",
            "sender": null,
            "receiver": null,
            "date": null
          }
        ],
        "solution": {
          "prerequisite": {"nodes": [], "edges": []},
          "culprit": {"$type": "model.game.Character", "name": "Alice", "role": "Suspect"},
          "motive": "Test"
        }
      }"""

    val missingPlotCase: String =
      """{
        "characters": [
          {"$type": "model.game.Character", "name": "Alice", "role": "Suspect"}
        ],
        "caseFiles": [],
        "solution": {
          "prerequisite": {"nodes": [], "edges": []},
          "culprit": {"$type": "model.game.Character", "name": "Alice", "role": "Suspect"},
          "motive": "Test"
        }
      }"""

    val malformedJson: String = """{ this is not valid json }"""

    val emptyJson: String = """{}"""

  import CaseJsonFixtures.*

  "ResponseParser for Case" when:

    "parsing a minimal valid case" should:
      "successfully parse all required fields" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(minimalValidCase)

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.plot.title shouldBe "Mystery at Dawn"
        caseModel.plot.content shouldBe "A mysterious case begins at the old mansion"
        caseModel.characters should have size 1
        caseModel.caseFiles should have size 1
        caseModel.solution.culprit.name shouldBe "Alice"
        caseModel.solution.motive shouldBe "Revenge"

    "parsing a complete case" should:
      "successfully parse all fields including complex structures" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(completeCase)

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.plot.title shouldBe "The Midnight Ledger"
        caseModel.characters should have size 3
        caseModel.caseFiles should have size 2
        caseModel.solution.culprit.name shouldBe "Dylan Hart"

      "correctly parse case files with sender and receiver" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(completeCase)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles
        val emailFile = caseFiles.find(_.title == "Contract Discussion").value

        emailFile.sender.value.name shouldBe "Elena Marsh"
        emailFile.receiver.value.name shouldBe "Dylan Hart"
        emailFile.date.value shouldBe "2025-10-31T09:15:00"

      "correctly parse case files with null optional fields" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(completeCase)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles
        val noteFile = caseFiles.find(_.title == "Meeting Note").value

        noteFile.sender shouldBe None
        noteFile.receiver shouldBe None
        noteFile.date shouldBe None

      "correctly parse prerequisite graph with nodes and edges" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(completeCase)

        result shouldBe a[Right[_, _]]
        val graph = result.value.solution.prerequisite

        graph.nodes should have size 3
        graph.edges should have size 2

        // Verify nodes contain Character
        val characters = graph.nodes.collect { case c: Character => c }
        characters.map(_.name) should contain("Dylan Hart")

        // Verify nodes contain CaseFile
        val caseFiles = graph.nodes.collect { case cf: CaseFile => cf }
        caseFiles.map(_.title) should contain("Contract Discussion")

        // Verify nodes contain CustomEntity
        val customEntities = graph.nodes.collect { case ce: CustomEntity => ce }
        customEntities.map(_.entityType) should contain("Motive")

      "correctly parse edges with semantic relationships" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(completeCase)

        result shouldBe a[Right[_, _]]
        val edges = result.value.solution.prerequisite.edges

        val semantics = edges.map(_._2.semantic)
        semantics should contain allOf ("sent", "has_motive")

    "parsing characters" should:
      "parse all character roles correctly" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(multipleCharactersCase)

        result shouldBe a[Right[_, _]]
        val characters = result.value.characters

        characters should have size 5
        characters.find(_.name == "Detective Brown").value.role shouldBe CaseRole.Investigator
        characters.find(_.name == "Museum Owner").value.role shouldBe CaseRole.Victim
        characters.find(_.name == "Security Guard").value.role shouldBe CaseRole.Witness
        characters.find(_.name == "Art Dealer").value.role shouldBe CaseRole.Suspect
        characters.find(_.name == "Assistant").value.role shouldBe CaseRole.Accomplice

    "parsing case files" should:
      "parse all CaseFileType variants correctly" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(allCaseFileTypesCase)

        result shouldBe a[Right[_, _]]
        val caseFiles = result.value.caseFiles

        caseFiles should have size 6
        caseFiles.find(_.title == "Text Message").value.kind shouldBe CaseFileType.Message
        caseFiles.find(_.title == "Email Message").value.kind shouldBe CaseFileType.Email
        caseFiles.find(_.title == "Interview Transcript").value.kind shouldBe CaseFileType.Interview
        caseFiles.find(_.title == "Personal Diary").value.kind shouldBe CaseFileType.Diary
        caseFiles.find(_.title == "Document").value.kind shouldBe CaseFileType.TextDocument
        caseFiles.find(_.title == "Investigation Notes").value.kind shouldBe CaseFileType.Notes

    "parsing solution" should:
      "correctly extract culprit and motive" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(minimalValidCase)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution

        solution.culprit.name shouldBe "Alice"
        solution.culprit.role shouldBe CaseRole.Suspect
        solution.motive shouldBe "Revenge"

      "correctly parse empty prerequisite graph" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(multipleCharactersCase)

        result shouldBe a[Right[_, _]]
        val graph = result.value.solution.prerequisite

        graph.nodes shouldBe empty
        graph.edges shouldBe empty

    "handling invalid data" should:
      "reject case with invalid character role" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(invalidRoleCase)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("parsing")

      "reject case with invalid case file type" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(invalidCaseFileTypeCase)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
        result.left.value.message should include("parsing")

      "reject case with missing required field" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(missingPlotCase)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]

      "reject completely malformed JSON" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(malformedJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]

      "reject empty JSON object" in:
        val result = ResponseParser.given_ResponseParser_Case.parse(emptyJson)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[ProductionError.ParseError]
