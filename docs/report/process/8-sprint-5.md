# Sprint 5 (03/11/2025 - 09/11/2025)

## Goal
The primary goal of this sprint is to focus on implementing the core game functionalities, particularly solution management and completing the main GUI. 
Work will proceed on refactoring the Versioning logic following the previous sprint's intensive implementation. 
The Trigger/In-game hint feature (FR4) will be finalized with its complete TDD cycle, controller implementation, and deployment. 
Additionally, the Solution feature (FR5) will be designed and minimally implemented, while controllers for Case Generation and Knowledge Graph will be developed to integrate these features into the game flow.

## Sprint Backlog
| Priority |    Product Backlog Item    | Sprint Task                                   |     Assignee      | Initial Estimate of Effort | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|:--------:|:--------------------------:|:----------------------------------------------|:-----------------:|:--------------------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|    1     |      FR3 - Versioning      | Logic Implementation - Refactor (#29)         | Lucia Castellucci |             5              | 3 | 2 | - | - | - | - | - |
|    1     |      Case generation       | Controller (#12)                              |  Roberto Mitugno  |             3              | 3 | - | - | - | - | - | - |
|    1     | FR4 - Trigger/In-game hint | Logic Design and Documentation (#24) - Finish |    Luca Samorè    |             12             | 2 | 1 | - | - | - | - | - |
|    1     | FR4 - Trigger/In-game hint | Logic Implementation (TDD loop) (#26)         |    Luca Samorè    |             6              | - | - | - | 4 | - | - | - |
|    1     | FR4 - Trigger/In-game hint | Controller Design and Implementation (#25)    |    Luca Samorè    |             4              | - | - | - | - | - | 1 | 2 |
|    1     | FR4 - Trigger/In-game hint | Test and Final Deploy (#27)                   |    Luca Samorè    |             2              | - | - | - | - | - | - | 1 |
|    2     |      FR3 - Versioning      | Controller Design & Implementation (#30)      | Lucia Castellucci |             2              | - | 2 | - | - | - | - | - |
|    2     |   FR2 - Knowledge Graph    | Controller (#19)                              |  Roberto Mitugno  |             4              | - | 3 | 3 | - | - | - | - |
|    2     |           Timer            | Controller (#65)                              |  Roberto Mitugno  |             3              | - | - | - | - | - | - | - |
|    3     |       FR5 - Solution       | Logic Design (#28)                            | Lucia Castellucci |             1              | - | - | 1 | - | - | - | 3 |
|    3     |       FR5 - Solution       | Logic Minimal Implementation (#29)            | Lucia Castellucci |             1              | - | - | 1 | - | - | - | - |
|    3     |       FR5 - Solution       | Controller Design and Implementation (#30)    | Lucia Castellucci |             1              | - | - | - | 1 | - | - | - |
|    3     |            View            | Solution Submission (#39)                     | Lucia Castellucci |             2              | - | - | - | - | 2 | - | - |
|    3     |            View            | Notification (#38)                            |  Roberto Mitugno  |             3              | - | - | - | 3 | - | - | - |
|    4     |       Documentation        | Introduction                                  | Lucia Castellucci |             1              | - | - | - | - | - | 1 | - |

## Review
The Versioning logic refactoring was successfully completed, improving code maintainability and preparing the foundation for the Solution feature. 
The Trigger/In-game hint functionality (FR4) was fully delivered, following a complete TDD cycle and including controller implementation and final deployment. 
The Solution submission feature (FR5) progressed through its design and minimal implementation phases, with controller integration completed. 
Controllers for both Case Generation and Knowledge Graph were implemented, establishing the connection between core logic and the game interface. 
The view components for solution submission and notification were delivered, enhancing the user experience. 
The Timer controller implementation was deferred to subsequent sprints due to prioritization of higher-value features.

## Retrospective
The refactoring of the Versioning system proved essential and validated the decision made at the end of Sprint 4. This technical debt resolution significantly improved code quality and enabled smoother implementation of the Solution feature. 
The TDD approach for the Trigger/In-game hint feature continued to demonstrate its effectiveness, producing robust and well-tested components with a fluid testing experience. The application of advanced Scala concepts, including mixin composition and type classes, enhanced the overall design quality of the implemented features.
However, the Cake Pattern architecture began to show its limitations during controller integration, requiring explicit listing of every controller in the ControllerModule. This coupling became apparent when implementing multiple controllers in parallel, suggesting that a simpler dependency injection approach or an abstract factory pattern might have been more maintainable.
As the project approaches completion, the team recognized the need to balance feature delivery with code quality. Some technical debt accumulated during this sprint in the push to complete core functionalities will need to be addressed alongside the comprehensive documentation effort planned for Sprint 6.