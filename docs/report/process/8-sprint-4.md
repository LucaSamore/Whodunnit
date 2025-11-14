# Sprint 4 (27/10/2025 - 02/11/2025)

## Goal

The fourth sprint focuses on implementing critical game mechanics and establishing the architectural foundation for the controller layer.
The primary objectives are to design and implement the timer system, which is essential for the gameplay experience, and to develop the trigger mechanism that will handle game events.
Additionally, we will begin documenting the development process and continue UI development by integrating the timer visualization.
On the architectural side, the Cake Pattern design for the controller will be established to ensure proper separation of concerns and modularity.

## Sprint Backlog
| Priority |    Product Backlog Item    | Sprint Task                                  |     Assignee      | Initial Estimate of Effort | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|:--------:|:--------------------------:|:---------------------------------------------|:-----------------:|:--------------------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|    4     |      FR3 - Versioning      | Logic Implementation (#29)                   | Lucia Castellucci |             5              | 2 | 3 | - | - | - | - | - |
|    4     |        Architecture        | Architecture Design & Implementation (#39)   | Lucia Castellucci |             10             | - | - | 2 | 3 | 2 | 3 | - |
|    4     |            View            | Loading Page (#44)                           | Lucia Castellucci |             2              | - | - | - | - | - | - | 2 |
|    4     |           Timer            | Design (#65)                                 |  Roberto Mitugno  |             10             | 3 | - | - | - | - | - | - |
|    4     |           Timer            | Test and Implementation (TDD loop) (#65)     |  Roberto Mitugno  |             10             | - | 2 | 2 | 3 | - | - | - |
|    4     |       Documentation        | Development Process (#52)                    |  Roberto Mitugno  |             2              | - | - | - | - | - | - | 2 |
|    4     | FR4 - Trigger/In-game hint | Logic Design and Documentation (#24) - Start |    Luca Samorè    |             12             | - | - | 2 | 2 | - | 1 | 1 |


## Review

The timer system has been successfully designed and implemented following TDD principles. The implementation includes both the core timing logic and its visual representation in the game interface, providing players with a clear indication of time progression during gameplay.
The trigger mechanism has been designed and developed, enabling the game to respond to various events and manage state transitions effectively. This component is fundamental for orchestrating game flow and interactions.
Work on the controller architecture has progressed with the definition of the Cake Pattern design, which will provide a solid foundation for managing dependencies and ensuring testability across the controller layer.
Documentation of the development process has been initiated, capturing key decisions and methodologies employed throughout the project.

## Retrospective

The continued application of Test-Driven Development has proven valuable in maintaining code quality and ensuring the reliability of core game mechanics. The timer and trigger systems, being critical components, have benefited significantly from this disciplined approach.
The team's decision to invest time in architectural design, particularly the Cake Pattern for the controller, reflects a mature understanding of the need for maintainability and scalability as the codebase grows.
The initial integration of documentation tasks within the sprint has been beneficial, ensuring that knowledge and design decisions are captured while they are still fresh, rather than being deferred to the end of the project.
As we move forward, the focus will shift toward integrating these newly developed components into a cohesive gameplay experience and continuing to bridge the gap between model, controller, and view layers.