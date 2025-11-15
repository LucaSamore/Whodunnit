# Retrospective
Reflecting on the project, the team unanimously agrees that the exploration and application of advanced programming concepts were a major success. 
We successfully integrated advanced Scala features such as mixin composition, type classes for ad-hoc polymorphism, and family polymorphism. 
This not only elevated the technical quality of the codebase but also significantly enhanced our design skills. The adoption of a Test-Driven Development (TDD) process, combined with a BDD-style approach, proved highly effective, making the testing phase both fluid and intuitive. 
Furthermore, our agile development process, characterized by well-structured meetings and clear task ownership, fostered strong collaboration and individual autonomy, creating a positive and productive team dynamic.

However, the project was not without its challenges. The architectural implementation using the Cake Pattern, while an interesting experiment in achieving type-safe dependency injection, proved to be cumbersome. 
In hindsight, we would reconsider this choice and adopt a simpler approach. The ControllerModule required explicitly listing every controller, a design that could be improved with an abstract factory to decouple their declarations. 
Similarly, the ViewModule is tightly coupled to ScalaFX, and a future iteration should abstract the UI library to improve portability.

We also recognize that the user interface received less attention than the backend logic. The view code could be better organized by separating container scenes from their constituent components to improve maintainability. 
Lastly, in our push to deliver features, we occasionally prioritized functionality over code quality, leading to an accumulation of technical debt. This sometimes slowed subsequent development, as refactoring became necessary to move forward, a valuable lesson in balancing speed with long-term code health.