package model

import org.scalatest.EitherValues
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class JsonParserTest extends AnyWordSpec with Matchers with EitherValues:

  "JsonParser" when:
    "parsing plot field" should:
      "extract plot text from valid JSON" in:
        val json = """
          {
            "plot": {
              "title": "Mystery at Dawn",
              "content": "A mysterious case begins"
            },
            "characters": []
          }
        """

        val parser: Parser = JsonParser
        val result = parser.parse(json)

        result shouldBe a[Right[_, _]]
        val plot = result.value.plot
        plot.title shouldBe "Mystery at Dawn"
        plot.content shouldBe "A mysterious case begins"

      "handle missing plot field" in:
        val invalidJson = """{"characters": []}"""

        val jsonParser: Parser = JsonParser
        val result = jsonParser.parse(invalidJson)

        result shouldBe a[Left[_, _]]
        println(result)
        result.left.value shouldBe a[MissingFieldError]
        result.left.value.message should include("plot")

      "handle invalid JSON syntax" in:
        val json = """{plot: "invalid}"""

        val result = JsonParser.parse(json)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[JsonSyntaxError]

      "handle non-string plot value" in:
        val json = """{"plot": 123}"""

        val result = JsonParser.parse(json)

        result shouldBe a[Left[_, _]]
        result.left.value shouldBe a[MissingFieldError]

    "parsing characters" should:
      "extract single character with name and role" in:
        val json =
          """
          {
            "plot": {
              "title": "Mystery at Dawn",
              "content": "A mysterious case begins"
            },
            "characters": [
              {"name": "Alice", "role": "Suspect"}
            ]
          }
        """

        val result = JsonParser.parse(json)

        result shouldBe a[Right[_, _]]
        val case1 = result.value

        case1.characters should have size 1

      "extract multiple characters" in:
        val json =
          """
          {
            "plot": {
              "title": "Mystery at Dawn",
              "content": "A mysterious case begins"
            },
            "characters": [
              {"name": "Alice", "role": "Suspect"},
              {"name": "Bob", "role": "Victim"},
              {"name": "Charlie", "role": "Witness"}
            ]
          }
        """

        val result = JsonParser.parse(json)

        result shouldBe a[Right[_, _]]
        result.value.characters should have size 3
