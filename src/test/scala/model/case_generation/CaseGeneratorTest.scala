package model.case_generation

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues


class CaseGeneratorTest extends AnyWordSpec with Matchers with EitherValues:
  
  "CaseGenerator" should :
    "have generate method returning Either" in :
      val generator: CaseGenerator = new CaseGenerator:
        def generate(constraints: Constraint*): Either[Error, Case] =
          Left(Error.InvalidPromptError("Not implemented"))

      val result = generator.generate(Constraint.Theme("Test"))
      result shouldBe a[Left[_, _]]