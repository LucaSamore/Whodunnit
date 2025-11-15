# Sprint 1 (06/10/2025 - 12/10/2025)

## Goal
The primary goal of this first sprint is to bootstrap the project.
This will involve the initial setup of the Scala project and its repository, establishing the test suite, and configuring the documentation and CI pipeline.
Furthermore, this sprint will focus on the user interface by producing mockups and the initial view-only implementation of several game screens.
Finally, a preliminary design for the case generation logic will be explored and defined.

## Sprint Backlog
| Priority | Product Backlog Item  | Sprint Task                 |     Assignee      | Initial Estimate of Effort | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|:--------:|:---------------------:|:----------------------------|:-----------------:|:--------------------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|    1     |         Setup         | Project (#1)                | Lucia Castellucci |             1              | 1 | - | - | - | - | - | - |
|    1     |         Setup         | Documentation (#11)         | Lucia Castellucci |             1              | - | - | 1 | - | - | - | - |
|    1     |         Setup         | GUI Engine (#50)            | Lucia Castellucci |             -              | - | - | - | - | - | - | - |
|    1     |         Setup         | CI (#2)                     |    Luca Samorè    |             3              | - | - | 2 | - | - | - | - |
|    1     |   Case Generation     | Logic Design (#13)          |  Roberto Mitugno  |             14             | 2 | - | 3 | - | - | - | - |
|    2     |        Mockup         | Home Page (#33)             | Lucia Castellucci |             2              | - | 2 | - | - | - | - | - |
|    2     |        Mockup         | Main Game Page (#46 -> #35) | Lucia Castellucci |             2              | - | - | - | 2 | - | - | - |
|    2     |         View          | Home Page (#33)             | Lucia Castellucci |             3              | - | - | - | - | 3 | - | - |
|    2     | GUI - Game Generation | Configuration Page (#34)    |    Luca Samorè    |             5              | - | - | - | - | - | 3 | 6 |
|    2     |         Setup         | Test Suite (#32)            |    Luca Samorè    |             1              | - | - | - | 1 | - | - | - |

## Review
The setup tasks for this sprint were successfully completed.
Regarding Continuous Integration, a workflow for test execution has been added as a starting point.
Additional workflows, such as those for deployment, documentation deployment, and code quality checks, will be implemented in subsequent sprints.
Mockups have been created for the Home Page, Game Configuration, and the main Game Board.
For the first two, an initial implementation has been developed in Scala with the objective of becoming familiar with the ScalaFX library for creating the views.

## Retrospective
Tackling the UI implementation first was the right call. Since view development is typically time-consuming, this early exposure to ScalaFX will increase our efficiency moving forward, enabling a stronger focus on model's aspects.
Progress on the case generation feature, however, was impeded by Roberto Mitugno's limited availability due to his work schedule. The task has therefore been rescheduled for the upcoming sprint.