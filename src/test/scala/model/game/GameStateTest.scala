package model.game

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.TestUtils.mockCase

import scala.concurrent.duration.*

class GameStateTest extends AnyWordSpec with Matchers:

  val emptyGameState = GameState()
  val mockTimer = new Timer(3600.seconds, List.empty)
  val mockGraph = new CaseKnowledgeGraph()
  val initializedGameState: GameState = GameState.initialize(
    mockCase,
    timer = mockTimer,
    graph = mockGraph
  )

  "A GameState" when:
    "empty" should:
      "have all fields as None" in:
        val gameState = GameState.empty()
        gameState shouldEqual emptyGameState

    "initialized" should:
      "have all fields properly set" in:
        val gameState = GameState.initialize(mockCase, mockTimer, mockGraph)
        gameState shouldEqual initializedGameState

    "using functional updates" should:
      "preserve immutability with withCase" in:
        val initial = GameState.empty()
        val updated = initial.withCase(mockCase)

        initial.investigativeCase shouldBe None
        updated.investigativeCase shouldBe Some(mockCase)

      "preserve immutability with withGraph" in:
        val initial = GameState.empty()
        val updated = initial.withGraph(mockGraph)

        initial.graph shouldBe None
        updated.graph shouldBe Some(mockGraph)

      "chain updates functionally" in:
        val state = GameState.empty()
          .withCase(mockCase)
          .withTimer(mockTimer)
          .withGraph(mockGraph)

        state.investigativeCase shouldBe Some(mockCase)
        state.timer shouldBe Some(mockTimer)
        state.graph shouldBe Some(mockGraph)

      "add hints immutably" in:
        import model.hint.HintKind
        val hint1 = Hint(HintKind.Helpful)
        val hint2 = Hint(HintKind.Misleading)

        val initial = GameState.empty()
        val withHint1 = initial.addHint(hint1)
        val withHint2 = withHint1.addHint(hint2)

        initial.hints shouldBe None
        withHint1.hints shouldBe Some(Seq(hint1))
        withHint2.hints shouldBe Some(Seq(hint1, hint2))

      "replace graph instance with updateGraph" in:
        val graph1 = new CaseKnowledgeGraph()
        val entity1 = Character("Char1", CaseRole.Suspect)
        graph1.addNode(entity1)

        val graph2 = new CaseKnowledgeGraph()
        val entity2 = Character("Char2", CaseRole.Victim)
        graph2.addNode(entity2)

        val initial = GameState.empty().withGraph(graph1)
        val updated = initial.updateGraph(_ => graph2)

        initial.graph shouldBe Some(graph1)
        updated.graph shouldBe Some(graph2)
        initial.graph should not be updated.graph
