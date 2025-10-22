package model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Success

class JsonParserTest extends AnyWordSpec with Matchers:

  "JsonParser" when:
    "parsing plot field" should:
      "extract plot text from valid JSON" in:
        val json = """{"plot": "A mysterious case"}"""

        val jsonParser: Parser = JsonParser
        val result = jsonParser.parse(json)

        result shouldBe a[Success[_]]
        result.get.plot.text shouldBe "A mysterious case"
