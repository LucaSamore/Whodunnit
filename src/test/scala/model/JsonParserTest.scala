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
            }
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