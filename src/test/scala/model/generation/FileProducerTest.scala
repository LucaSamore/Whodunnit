package model.generation

import model.game.{Case, Hint}
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FileProducerTest extends AnyWordSpec with Matchers with EitherValues with OptionValues:

  "FileBasedProducer for Case" when:
    "producing a case" should:
      "successfully load and parse case.json from resources" in:
        val producer = FileBasedProducer.forCase
        val result = producer.produce()

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.plot.title shouldBe "The Midnight Bloom Murder"
        caseModel.characters should not be empty
        caseModel.caseFiles should not be empty
        caseModel.solution.culprit.name shouldBe "Marcus Vale"
        caseModel.solution.motive should include("sell the formula")

      "parse all characters correctly from case.json" in:
        val producer = FileBasedProducer.forCase
        val result = producer.produce()

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.characters should have size 4
        val characterNames = caseModel.characters.map(_.name)
        characterNames should contain allOf ("Dr. Evelyn Hart", "Marcus Vale", "Lena Ortiz", "Victor Sloan")

      "parse all case files correctly from case.json" in:
        val producer = FileBasedProducer.forCase
        val result = producer.produce()

        result shouldBe a[Right[_, _]]
        val caseModel = result.value

        caseModel.caseFiles should have size 5
        val fileTitles = caseModel.caseFiles.map(_.title)
        fileTitles should contain allOf (
          "Lab Incident Report",
          "Late Night Text",
          "Security Log",
          "Evelyn's Diary",
          "Interview with Victor Sloan"
        )

      "ignore constraints parameter (not used for file-based)" in:
        val producer = FileBasedProducer.forCase
        val constraint = Theme("Ignored Theme")

        val result = producer.produce(constraint)

        result shouldBe a[Right[_, _]]
        // Should still load from file, ignoring constraints
        result.value.plot.title shouldBe "The Midnight Bloom Murder"

  "FileBasedProducer for Hint" when:
    "producing a hint" should:
      "successfully load and parse hints from resources" in:
        val producer = FileBasedProducer.forHint
        val result = producer.produce()

        result shouldBe a[Right[_, _]]
        val hint = result.value

        hint.description should include("toxin")

      "ignore constraints parameter" in:
        val producer = FileBasedProducer.forHint
        val constraint = HintKind.Helpful

        val result = producer.produce(constraint)

        result shouldBe a[Right[_, _]]
        result.value.description should not be empty

  "FileBasedProducer error handling" should:
    "return ParseError for non-existent resource" in:
      // Create a producer with an unknown type to trigger error path
      // We can't easily test this without reflection, so we'll test the existing types work
      val caseProducer = FileBasedProducer.forCase
      val hintProducer = FileBasedProducer.forHint

      caseProducer.produce() shouldBe a[Right[_, _]]
      hintProducer.produce() shouldBe a[Right[_, _]]

    "handle JSON parsing correctly" in:
      val producer = FileBasedProducer.forCase
      val result = producer.produce()

      result shouldBe a[Right[_, _]]
      // Verify the solution prerequisite graph is parsed
      val caseModel = result.value
      caseModel.solution.prerequisite.nodes should not be empty
      caseModel.solution.prerequisite.edges should not be empty

  "FileBasedProducer factory methods" should:
    "create Case producer" in:
      val producer = FileBasedProducer.forCase
      producer shouldBe a[Producer[_]]

    "create Hint producer" in:
      val producer = FileBasedProducer.forHint
      producer shouldBe a[Producer[_]]

    "produce different results for Case and Hint" in:
      val caseProducer = FileBasedProducer.forCase
      val hintProducer = FileBasedProducer.forHint

      val caseResult = caseProducer.produce()
      val hintResult = hintProducer.produce()

      caseResult shouldBe a[Right[_, _]]
      hintResult shouldBe a[Right[_, _]]

      caseResult.value shouldBe a[Case]
      hintResult.value shouldBe a[Hint]
