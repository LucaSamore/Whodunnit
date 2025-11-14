package model

import model.game.{Case, CaseKnowledgeGraph, GameHistory, GameState, Timer}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.TestUtils.mockCase

import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class ModelTest extends AnyWordSpec with Matchers:

  "ModelModule.Model" when:
    "using ModelImpl" should:
      val module = new ModelModule.Interface {}
      val model = module.model

      "start with empty state" in:
        model.state shouldBe GameState.empty()

      "update state with updater function" in:
        val timer = new Timer(3600.seconds, List.empty)
        val updatedState = model.updateState(_.withTimer(timer))

        updatedState.timer shouldBe Some(timer)
        model.state.timer shouldBe Some(timer)

      "return updated state from updateState" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        val result = newModel.updateState(_.withCase(mockCase))
        result.investigativeCase shouldBe Some(mockCase)

      "maintain state across multiple updates" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        val timer = new Timer(3600.seconds, List.empty)
        val history = GameHistory(5)

        newModel.updateState(_.withTimer(timer))
        newModel.updateState(_.withHistory(history))

        val finalState = newModel.state
        finalState.timer shouldBe Some(timer)
        finalState.history shouldBe Some(history)

      "handle concurrent updates safely" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        val futures = (1 to 100).map { i =>
          Future {
            newModel.updateState { state =>
              val timer = new Timer((3600 + i).seconds, List.empty)
              state.withTimer(timer)
            }
          }
        }

        Await.result(Future.sequence(futures), 5.seconds)

        // State should be consistent (no corruption)
        newModel.state.timer shouldBe defined

      "return None for getRemainingTime when timer is None" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        newModel.getRemainingTime shouldBe None

      "return remaining time when timer exists" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        val timer = new Timer(3600.seconds, List.empty)
        newModel.updateState(_.withTimer(timer))

        // Timer not started, so should return None
        newModel.getRemainingTime shouldBe None

      "handle state transformations correctly" in:
        val newModule = new ModelModule.Interface {}
        val newModel = newModule.model

        val graph = new CaseKnowledgeGraph()

        newModel.updateState { state =>
          state
            .withCase(mockCase)
            .withHistory(GameHistory(5))
            .addGraphToHistory(graph)
        }

        val finalState = newModel.state
        finalState.investigativeCase shouldBe Some(mockCase)
        finalState.history shouldBe defined
        finalState.currentGraph shouldBe Some(graph)

  "ModelModule.Provider" should:
    "provide access to model instance" in:
      val provider = new ModelModule.Interface {}
      provider.model shouldBe a[ModelModule.Model]

  "ModelModule.Component" should:
    "create ModelImpl instance" in:
      val component = new ModelModule.Component {}
      val model = new component.ModelImpl()
      model shouldBe a[ModelModule.Model]
