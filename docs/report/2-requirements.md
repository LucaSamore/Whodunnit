# Requirements

## Business Requirements (BR)
These requirements describe the high-level goals of the product and the value it should bring to users and stakeholders.
- **Immersive Investigative Experience**: Offer a single-player game that simulates an investigation, with plausible stories, coherent documents and characters.
- **Experience Personalization**: Allow the player to configure relevant game parameters (difficulty, theme) to tailor the challenge to individual preferences.
- **Support for deductive thinking**: provide tools that help organize information and generate hypotheses.

## Functional Requirements (FR)
These requirements describe the observable behavior of the system from the users' point of view.
### User Functional Requirements

| Requirement                        | Description                                                                                    |
|:-----------------------------------|:-----------------------------------------------------------------------------------------------|
| Set initial parameters             | User can set initial game parameters (difficulty and theme).                                   |
| View plot                          | User can view the case plot at any time.                                                       |
| Consult generated documents        | User can view case documents.                                                                  |
| View knowledge graph               | User can view the Knowledge Graph with nodes and semantic links.                               |
| Create custom entities             | User can create custom entities (locations, objects, etc.) to include in the graph.            |
| Link entities/documents/characters | User can create semantic links between custom entities, documents and characters.              |
| View timer                         | User can view the active timer.                                                                |
| Receive notifications              | User receives notifications during gameplay (helpful or misleading).                           |
| Undo / Redo recent actions         | User can undo and redo a limited window of recent actions.                                     |
| Move graph elements (drag & drop)  | User can move graph nodes via drag & drop.                                                     |
| Save and restore graph state       | User can save a snapshot of the Knowledge Graph and restore it later.                          |
| Submit solution and view result    | User can submit a suspected character; after submission or timer expiry the solution is shown. |



### System Functional Requirements
This section lists the system-level features and services that support user-facing functionality.

| Requirement                            | Description                                                                                                                                                    |
|:---------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Create new case from parameters        | Create a new investigative case to play using the parameters chosen by the user.                                                                               |
| Show case characters                   | Always display all the case characters on the main board view.                                                                                                 |
| Show relationships                     | Display the relationships created by the user through the Knowledge Graph on the main board view.                                                              |
| Start timer                            | Automatically start a timer when a new investigative case is initialized                                                                                       |
| Emit timed notifications               | Create notifications at predefined intervals, whether helpful or misleading.                                                                                   |
| Maintain action history and versioning | Maintains a timeline of graph states to support undo/redo navigation for a window of moves performed.                                                          |
| Restore snapshots                      | Save a state of the Knowledge Graph to be able to restore the snapshot taken by the user.                                                                      |
| Solution submission policies           | Only allow the solution to be submitted after certain requirements have been met.                                                                              |
| Verify submitted solution              | verify the solution submitted by the user through a check on the Knowledge Graph.                                                                              |


## Non Functional Requirements
Non-functional requirements define quality, platform constraints, and measurement criteria.

| Requirement   | Description                                                                                                                                                                                                                                                                               |
|:--------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Performance   | The system must guarantee fast response times to maintain player immersion. In particular, the generation of a new case must be completed within 30 seconds, while all operations on the Knowledge Graph (creation of entities, links, restore snapshot) must respond in less than 100ms. |
| Reliability   | The system must not generate unhandled exceptions or inconsistent states during game execution.                                                                                                                                                                                           |
| Usability     | The interface must be intuitive and require a minimum number of interactions for common actions.                                                                                                                                                                                          |
| Testability   | Automatic test coverage (unit and integration) on business code must be at least 75%.                                                                                                                                                                                                     |
| Portability   | The system must be executable on all major operating systems (Linux, macOS, Windows) and compatible with JVM 11 or higher.                                                                                                                                                                |


## Optional Requirements

In addition to the mandatory functional requirements, optional requirements have been identified that could significantly enrich the gaming experience and improve the system's usability. These requirements, while not critical to the initial release, add value to the final product and increase user reuse of games:

- **Advanced Chat Interaction with Characters**: The user must be able to converse with the characters via an instant messaging interface. For a character of their choice, the chat is allowed with an LLM that responds in a manner consistent with the character's storyline and personality. For other characters, only predefined questions are available.
- **Dynamic Filtering of the Knowledge Graph**: The user must be able to apply combined filters to the graph to focus on specific relationships or entity types.
- **Restore Interrupted Match**: The user must be able to interrupt the match at any time and resume it later from the exact state of interruption. Saving must occur automatically every 30 seconds and upon exit, without requiring explicit user action.
- **Replay Games**: The user must be able to access an archive of completed games and select one to replay with the exact same elements (plot, documents, characters, solution).