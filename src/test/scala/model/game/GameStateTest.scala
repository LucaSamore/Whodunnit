package model.game

import model.generation.{Constraint, Producer, ProductionError}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.TestUtils.mockCase

import scala.concurrent.duration.*

class GameStateTest extends AnyWordSpec with Matchers:

  private case class MockHint(override val description: String) extends Hint

  private given Producer[Hint] with
    override def produce(constraints: Constraint*): Either[ProductionError, Hint] =
      Right(MockHint("Test Hint"))

  val emptyGameState: GameState = GameState()
  val mockTimer = new Timer(3600.seconds, List.empty)
  val mockGraph = new CaseKnowledgeGraph()
  val initializedGameState: GameState = GameState.initialize(
    mockCase,
    timer = mockTimer,
    initialGraph = mockGraph
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

      "preserve immutability with addGraphToHistory" in:
        val initial = GameState.empty().withHistory(GameHistory(5))
        val updated = initial.addGraphToHistory(mockGraph)

        initial.currentGraph shouldBe None
        updated.currentGraph shouldBe Some(mockGraph)

      "chain updates functionally" in:
        val state = GameState.empty()
          .withCase(mockCase)
          .withTimer(mockTimer)
          .withHistory(GameHistory(5))
          .addGraphToHistory(mockGraph)

        state.investigativeCase shouldBe Some(mockCase)
        state.timer shouldBe Some(mockTimer)
        state.currentGraph shouldBe Some(mockGraph)

      "add hints immutably" in:
        import model.generation.HintKind
        val hint1 = Hint(HintKind.Helpful).toOption.get
        val hint2 = Hint(HintKind.Misleading).toOption.get

        val initial = GameState.empty()
        val withHint1 = initial.addHint(hint1)
        val withHint2 = withHint1.addHint(hint2)

        initial.hints shouldBe None
        withHint1.hints shouldBe Some(Seq(hint1))
        withHint2.hints shouldBe Some(Seq(hint1, hint2))

      "add multiple graphs to history" in:
        val graph1 = new CaseKnowledgeGraph()
        val entity1 = Character("Char1", CaseRole.Suspect)
        graph1.addNode(entity1)

        val graph2 = new CaseKnowledgeGraph()
        val entity2 = Character("Char2", CaseRole.Victim)
        graph2.addNode(entity2)

        val initial = GameState.empty().withHistory(GameHistory(5))
        val withGraph1 = initial.addGraphToHistory(graph1)
        val withGraph2 = withGraph1.addGraphToHistory(graph2)

        initial.currentGraph shouldBe None
        withGraph1.currentGraph.map(_.nodes) shouldBe Some(Set(entity1))
        withGraph2.currentGraph.map(_.nodes) shouldBe Some(Set(entity2))
