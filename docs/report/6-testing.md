# **Testing**
The project's testing approach was fundamental in ensuring the correctness and maintainability of the software. The strategy adopted focused primarily on the core components of the application domain, following the principles of **Test-Driven Development (TDD)** wherever possible, as described in the development process.

This allowed us to build a solid base of verified code, especially for the business logic and complex data structures that form the core of the system.

## **Testing Technologies Adopted**
Specific tools from the Scala ecosystem were used to implement the automated test suite, chosen for their expressiveness and integration with the SBT build tool.

- **ScalaTest**: This was selected as the main testing framework due to its flexibility, which allowed us to write clear and readable tests tailored to different contexts. Specifically, we utilized a hybrid approach by employing two distinct styles: **`AnyWordSpec`** was used for its BDD (Behaviour-Driven Development) capabilities, enabling tests to be written in a descriptive, almost textual format (e.g., “A component” should “produce a result” when “performing an action”), making the tests highly readable and self-documenting. **`AnyFlatSpec`** was adopted for its concise, which is ideal for more straightforward, example-based test cases.
- **ScalaMock**: As indicated in the project dependencies, ScalaMock was integrated for the creation of mock objects. This tool was essential for isolating components during unit testing, especially for simulating the behaviour of external or complex dependencies (such as the `Producer`) and verifying that interactions occurred as expected.
- **sbt-scoverage**: The sbt-scoverage plugin was used to measure code coverage. This tool, integrated into the Continuous Integration pipeline, generates detailed reports on which parts of the code were actually executed by the test suite. In addition, using the Coveralls tool, the coverage results were aggregated and made accessible online for easy consultation via [Coveralls](https://coveralls.io/github/LuciaCastellucci/PPS-24-whodunnit).

## **Test Examples**

The following examples illustrate the testing approach adopted in the project, showing how ScalaTest with the `AnyWordSpec` and `AnyFlatSpec` styles were used to verify the behaviour of key components.

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
This test verifies that the system correctly expands a difficulty level into its corresponding constraints while preserving user-defined constraints like the theme.

### **Testing Trend Analysis**

The `TrendAnalyzerTest` suite validate trend detection logic (in this case detecting increasing, worsening or stable trends in sequences of values):

```scala
"TrendAnalyzer" should "detect increasing trend" in:
    Given("values with clearly increasing trend")
    val values = List(0.1, 0.2, 0.3, 0.4, 0.5)

    When("analyzing the trend")
    val trend = summon[TrendAnalyzer].analyze(values)

    Then("it should detect increasing")
    trend shouldBe Increasing

it should "detect worsening trends" in:
    Given("values with clear worsening trend")
    val values = List(0.5, 0.4, 0.3, 0.2, 0.1)

    When("analyzing the trend")
    val trend = summon[TrendAnalyzer].analyze(values)

    Then("it should detect Worsening")
    trend shouldBe Worsening
```

This example validates the logic of the `TrendAnalyzer`, verifying that the system correctly identifies trends (such as 'Increasing' or 'Worsening') based on a given sequence of numerical values.

### **Testing Time Machine Functionality**
The `TimeMachineTest` suite validates the snapshot and restore mechanism used for game state management. This example demonstrates testing the restore functionality:

```scala
"a snapshot is restored" should:
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
This test verify that the time machine correctly restores previous game states through deep copying, ensuring that modifications to the current state don't affect saved snapshots.