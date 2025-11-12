# Requirements

## Business Requirements
The system aims to create a single-player investigative gaming experience based on the construction and analysis of a Knowledge Graph generated dynamically using a Large Language Model (LLM). 

The main business requirements are:
- **Personalisation of the experience** – The system must allow the player to configure game parameters (difficulty, theme) to adapt the complexity and setting to their preferences.
- **Realistic investigative simulation** – The game must generate credible cases with coherent plots, realistically interconnected documents and characters with plausible relationships, maintaining a balance between challenge and solution.
- **Support for deductive thinking** – The system must provide tools for visualising and manipulating the Knowledge Graph (versioning, snapshots) that facilitate the organisation of information and the development of investigative hypotheses.
- **Gameplay adaptability** – The system must monitor the player's progress and dynamically intervene with help or distractions to maintain an optimal level of engagement.
- **Modularity and extensibility** – The architecture must support the addition of new types of entities within the Knowledge Graph.


## Functional Requirements
### User Requirements
| ID | Requirement                | Description                                                                                                                                 |
|:----|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| UR1 | Case Generation via LLM    | The user must be able to set the initial parameters: difficulty (Easy/Medium/Hard) and theme.                                               |
| UR2 | Case Generation via LLM    | The user must be able to consult the generated plot of the investigation at any time.                                                       |
| UR3 | Case Generation via LLM    | The user must be able to view the list of documents, link them to entities.                                                                 |
| UR4 | Case Generation via LLM    | The user must be able to create new entities (custom), create relationships between entities, and modify and delete existing relationships. |
| UR5 | Undo/Redo                  | The user must be able to undo/redo the last 5 moves made on the Knowledge Graph.                                                            |
| UR6 | Solution Submission        | The user must be able to accuse a character only after meeting the connection prerequisites.                                                |
| UR7 | Hint Receipt               | The user must be able to receive help or distractions through an automatically triggered notification system.                               |


### System Requirements
| ID    | Requirement                   | Description                                                                                                                                                                                 |
|:------|:------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NFR1  | Case Generation via LLM       | The system must generate synchronously with the configuration: a narrative plot, a set of related documents, a set of related characters and a solution graph containing the expected arcs. |
| NFR2  | Management of Knowledge Graph | The user must be able to consult the generated plot of the investigation at any time.                                                                                                       |
| NFR3  | Versioning                    | The system must implement a queue of 5 slots for history of moves. Undo decrements, redo increments the index. New actions overwrite the queue beyond the limit.                            |
| NFR4  | Snapshot Management           | The system must allow the saving of a deep copy of the Knowledge Graph and the versioning state, with restoration and redefinition of the undo/redo action.                                 |
| NFR5  | Hints System                  | The system must generate hints when: <br/>- Time elapsed ≥ 1/3 total time, <br/>- Time elapsed ≥ 2/3 total time, according to the metrics enstablished.                                     |
| NFR6  | Solution Validation           | The system must compare the player's Knowledge Graph with the generated solution.                                                                                                           |


## Non Functional Requirements

| ID | Requirement   | Description                                                                                                                                                                                                                                                                               |
|:----|:--------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SR1 | Performance   | The system must guarantee fast response times to maintain player immersion. In particular, the generation of a new case must be completed within 20 seconds, while all operations on the Knowledge Graph (creation of entities, links, restore snapshot) must respond in less than 100ms. |
| SR2 | Reliability   | The system must not generate unhandled exceptions or inconsistent states during game execution.                                                                                                                                                                                           |
| SR3 | Usability     | The interface must be intuitive and require a minimum number of interactions for common actions.                                                                                                                                                                                          |
| SR4 | Testability   | Automatic test coverage (unit and integration) on business code must be at least 75%.                                                                                                                                                                                                     |
| SR5 | Portability   | The system must be executable on all major operating systems (Linux, macOS, Windows) and compatible with JVM 11 or higher.                                                                                                                                                                |


## Optional Requirements

In addition to the mandatory functional requirements, optional requirements have been identified that could significantly enrich the gaming experience and improve the system's usability. These requirements, while not critical to the initial release, add value to the final product and increase user reuse of games.

- **Advanced Chat Interaction with Characters**: The user must be able to converse with the characters in question via an instant messaging interface. For a character of their choice, the chat is free with an LLM that responds in a manner consistent with the character's storyline and personality. For other characters, only predefined questions are available.
- **Dynamic Filtering of the Knowledge Graph**: The user must be able to apply combined filters to the graph to focus on specific relationships or entity types. 
- **Restore Interrupted Match**: The user must be able to interrupt the match at any time and resume it later from the exact state of interruption.  Saving must occur automatically every 30 seconds and upon exit, without requiring explicit user action.
- **Replay Games**: The user must be able to access an archive of completed games and select one to replay with the exact same elements (plot, documents, characters, solution).


