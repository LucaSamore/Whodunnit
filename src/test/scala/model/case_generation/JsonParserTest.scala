package model.case_generation

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class JsonParserTest extends AnyWordSpec with Matchers with EitherValues:

  object JsonFixtures:
    val basePlot: String =
      """
      "plot": {
        "title": "Mystery at Dawn",
        "content": "A mysterious case begins"
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

    val singleFile: String =
      """
      "files": [
        {
          "title": "Email",
          "kind": "Email",
          "sender": "Alice",
          "receiver": null,
          "date": "2025-10-20T14:30:00",
          "content": "Threatening message"
        }
      ]
      """

    val fileWithNulls: String =
      """
      "files": [
        {
          "title": "Note",
          "kind": "Notes",
          "sender": null,
          "receiver": null,
          "date": null,
          "content": "Anonymous note"
        }
      ]
      """

    val baseSolution: String =
      """
      "solution": {
        "prerequisite": [
          {
            "firstEntity": "Alice",
            "secondEntity": "Email",
            "semantic": "sent"
          }
        ],
        "culprit": "Alice",
        "motive": "Revenge"
      }
      """

    def json(parts: String*): String =
      parts.mkString("{", ",", "}")

  import JsonFixtures.*

  "JsonParser" when:
    "parsing plot field" should:
      "extract plot text from valid JSON" in:
        val jsonStr = json(basePlot, singleCharacter, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val plot = result.value.plot
        plot.title shouldBe "Mystery at Dawn"
        plot.content shouldBe "A mysterious case begins"

      "handle missing plot field" in:
        val jsonStr = json(singleCharacter)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[MissingFieldError]
        result.left.value.message should include("plot")

      "handle invalid JSON syntax" in:
        val jsonStr = """{plot: "invalid}"""
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[JsonSyntaxError]

      "handle non-string plot value" in:
        val jsonStr = """{"plot": 123}"""
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[JsonSyntaxError]

    "parsing characters" should:
      "extract single character with name and role" in:
        val jsonStr = json(basePlot, singleCharacter, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        result.value.characters should have size 1

      "extract multiple characters" in:
        val jsonStr = json(basePlot, multipleCharacters, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        result.value.characters should have size 3

      "handle missing characters field" in:
        val jsonStr = json(basePlot)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value.message should include("characters")

      "handle empty characters array" in:
        val jsonStr = json(basePlot, """"characters": [], "files": []""", baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value.message should include("must not be empty")

    "parsing files" should:
      "extract casefile with all fields present" in:
        val jsonStr = json(basePlot, multipleCharacters, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        result.value.caseFiles should have size 1

      "extract casefile with null optional fields" in:
        val jsonStr = json(basePlot, singleCharacter, fileWithNulls,
          """"solution": {"prerequisite": [{"firstEntity": "Alice","secondEntity": "Note","semantic": "sent"}],"culprit": "Alice","motive": "Revenge"}"""
          )
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]

      "handle invalid casefile kind" in:
        val jsonStr = json(basePlot, singleCharacter,
          """"files": [{"title": "Doc", "kind": "InvalidType", "content": "text"}]""",
          baseSolution
        )
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Left[_, _]]
        result.left.value.message should include("Unknown type")

    "parsing solution" should:
      "extract culprit and motive" in:
        val jsonStr = json(basePlot, singleCharacter, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution
        solution.culprit.name shouldBe "Alice"
        solution.motive shouldBe "Revenge"

      "extract single prerequisite" in:
        val jsonStr = json(basePlot, singleCharacter, singleFile, baseSolution)
        val result = JsonParser.parse(jsonStr)

        result shouldBe a[Right[_, _]]
        val solution = result.value.solution.asInstanceOf[CaseSolution]
        solution.prerequisite should have size 1
        val prereq = solution.prerequisite.head
        prereq.firstEntity.toString shouldBe "Character(Alice,Suspect)"
        prereq.secondEntity.toString shouldBe "CaseFile(Email,Threatening message,Email,Some(Character(Alice,Suspect)),None,Some(2025-10-20T14:30))"
        prereq.semantic shouldBe "sent"

      "handle missing solution field" in :
        val jsonStr = json(basePlot, singleCharacter, singleFile)
        val result = JsonParser.parse(jsonStr)
        result shouldBe a[Left[_, _]]
        result.left.value.message should include("solution")

      "handle missing culprit in solution" in :
        val jsonStr = json(basePlot, singleCharacter, singleFile,
          """"solution": {"prerequisite": [{"firstEntity": "Alice", "secondEntity": "Email", "semantic": "sent"}],"motive": "Revenge"}"""
          )
        val result = JsonParser.parse(jsonStr)
        result shouldBe a[Left[_, _]]

      "handle missing motive in solution" in :
        val jsonStr = json(basePlot, singleCharacter, singleFile,
          """"solution": {"prerequisite": [{"firstEntity": "Alice", "secondEntity": "Email", "semantic": "sent"}],"culprit": "Alice"}"""
        )
        val result = JsonParser.parse(jsonStr)
        result shouldBe a[Left[_, _]]

      "handle missing prerequisite entities" in :
        val jsonStr = json(basePlot, singleCharacter, singleFile,
            """solution": {"prerequisite": [],"culprit": "Alice","motive": "Revenge"}"""
        )
        val result = JsonParser.parse(jsonStr)
        result shouldBe a[Left[_, _]]
