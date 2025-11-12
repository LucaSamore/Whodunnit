# **Testing**
The project's testing approach was fundamental in ensuring the correctness, robustness and maintainability of the software. The strategy adopted focused primarily on the core components of the application domain, following the principles of **Test-Driven Development (TDD)** wherever possible, as described in the development process.

This allowed us to build a solid base of verified code, especially for the business logic and complex data structures that form the core of the system.

## **Testing Technologies Adopted**
Specific tools from the Scala ecosystem were used to implement the automated test suite, chosen for their expressiveness and integration with the SBT build tool.

- **ScalaTest**: This was selected as the main testing framework. Its flexibility allowed us to write clear and readable tests. In particular, the AnyWordSpec style was adopted, which favours a BDD (Behaviour-Driven Development) approach. This style allows the expected behaviour of components to be described in an almost textual format (e.g. “A component” when “performing an action” should “produce a result”), making the tests self-documenting.
- **ScalaMock**: As indicated in the project dependencies, ScalaMock was integrated for the creation of mock objects. This tool was essential for isolating components during unit testing, especially for simulating the behaviour of external or complex dependencies (such as the `Producer`) and verifying that interactions occurred as expected.
- **sbt-scoverage**: The sbt-scoverage plugin was used to measure code coverage. This tool, integrated into the Continuous Integration pipeline, generates detailed reports on which parts of the code were actually executed by the test suite. In addition, using the Coveralls tool, the coverage results were aggregated and made accessible online for easy consultation via [Coveralls](https://coveralls.io/github/LuciaCastellucci/PPS-24-whodunnit).

## **Test Examples**

The following examples illustrate the testing approach adopted in the project, showing how ScalaTest with the AnyWordSpec style was used to verify the behaviour of key components.

### **Testing Domain Constraints**

The `ConstraintTest` suite demonstrates how the application validates game generation constraints. This example shows the testing of the `expandConstraints` method, which is responsible for transforming difficulty presets into concrete constraint values:
```scala
"Constraint.expandConstraints" should:
    "expand Easy difficulty to easy preset constraints without theme" in:
        import Difficulty.Easy
        
            val result = Constraint.expandConstraints(Seq(Theme("Murder"), Easy))
        
            result should contain(Theme("Murder"))
            result should contain(CharactersRange(2, 4))
            result should contain(CaseFilesRange(2, 5))
            result should contain(PrerequisitesRange(1, 2))
            result should have size 4
```    
This test verifies that the system correctly expands a difficulty level into its corresponding constraints while preserving user-defined constraints like the theme. The BDD-style syntax makes the expected behaviour clear and self-documenting.

### **Testing Graph Data Structures**

The `BaseOrientedGraphTest` suite validates the correctness of the graph implementation used to represent relationships between game elements. This example tests the graph's behaviour when removing nodes:

```scala
"removing nodes" should:
    "remove incoming edges from all sources" in:
        val g = graph.withNodes(
            1,
            2,
            3
        ).withEdge(1, "edge1", 2).withEdge(3, "edge2", 2)
        g.removeNode(2)
        g.outEdges(1) should not contain "edge1"
        g.outEdges(3) should not contain "edge2"
```

This test ensures that when a node is removed, all edges pointing to it are also removed, maintaining graph consistency. Such tests were crucial for verifying the integrity of the game's knowledge graph structure.

### **Testing Time Machine Functionality**
The `TimeMachineTest` suite validates the snapshot and restore mechanism used for game state management. This example demonstrates testing the restore functionality:

```scala
"a snapshot is restored" should:
    "preserve equality but be a distinct instance" in:
        restoredHistory.get should not be theSameInstanceAs(gameHistory)
        restoredHistory.get shouldEqual gameHistory
        
    "preserve the state if modified after snapshot" in:
        val modifiedHistory = gameHistory.deepCopy()
        case class MockCaseKnowledgeGraph(id: Int)
                extends game.CaseKnowledgeGraph:
            override def deepCopy(): CaseKnowledgeGraph =
                MockCaseKnowledgeGraph(id)
        val updatedHistory = modifiedHistory.addState(MockCaseKnowledgeGraph(1))
        updatedHistory should not equal gameHistory
        val restoredAfterModification = timeMachine.restore()
        restoredAfterModification shouldBe Some(gameHistory)
```
These tests verify that the time machine correctly restores previous game states through deep copying, ensuring that modifications to the current state don't affect saved snapshots. This was essential for implementing the game's undo functionality reliably.