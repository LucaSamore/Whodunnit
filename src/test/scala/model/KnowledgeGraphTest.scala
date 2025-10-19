package model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KnowledgeGraphTest extends AnyFlatSpec with Matchers:

  "A BaseGraph" must "be initially empty" in:
    val g = new BaseGraph {}
    g.isEmpty shouldBe true
