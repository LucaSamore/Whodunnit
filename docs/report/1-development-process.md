# **Development Process**

For the project's development, an **Agile** approach inspired by the **Scrum** framework was adopted, adapted to the specific needs of the university context and the small size of the team.

The work was organized into weekly iterative cycles called *sprints*, through which the system was developed incrementally, progressively adding new functionalities or refining existing ones.

## **Roles and Team Organization**

Given the nature of the project and the team's composition, the members took on multiple roles:

- **Luca Samorè** held the role of **Product Owner**, responsible for managing and prioritizing the Product Backlog;
- **Lucia Castellucci** operated as **Scrum Master**, facilitating the application of the methodology and the resolution of impediments;
- All three team members — *Roberto Mitugno*, *Lucia Castellucci*, and *Luca Samorè* — constituted the **Development Team**, actively contributing to the implementation of functionalities.

This overlap of roles, despite representing a deviation from the pure Scrum model (which specifies that the Product Owner should not develop), was a necessary and conscious choice dictated by the group's size, allowing for a more flexible distribution of the workload.

## **Planning and Sprint Cycle**

The initial phase saw the definition of the system's general architecture through requirements analysis and modeling using UML class diagrams.

Subsequently, the **Product Backlog** was built, a prioritized list of functionalities (*items*), each characterized by an implementation complexity estimate.

Each weekly sprint was structured according to the following activities:

- **Combined Sprint Planning and Review**:

  To optimize time and considering the small team size, the Sprint Review for the completed sprint and the Sprint Planning for the next one were conducted in a single session at the beginning of each week.

  During this meeting, the team presented the completed work, discussed any issues that arose, selected the *items* to be implemented in the upcoming sprint, and produced the **Sprint Backlog** with task assignments.

At the end of each sprint, in addition to the Sprint Review, the following were conducted:

- **Product Backlog Refinement**: updating and reprioritizing the Product Backlog based on the results obtained and the feedback that emerged.
- **Sprint Retrospective**: critical reflection on the work process, identifying aspects to be improved for subsequent sprints.

The goal of each sprint was to produce a *Potentially Shippable Product Increment*, ensuring that the developed functionalities were complete and working.

---

## **Definition of Done**

To ensure uniform quality standards, the team explicitly defined a **Definition of Done** that each task had to satisfy before being considered completed:

- The code was **implemented and tested** (through automated tests or, where not possible, documented manual tests);
- A **collective code review** session was conducted, during which the entire team examined the produced code;
- The code was **integrated into the development branch (`dev`)**, successfully passing all CI pipeline checks;

---

## **Test-Driven Development**

During development, an attempt was made to apply, where possible, the **Test-Driven Development (TDD)** approach, a methodology that involves writing tests before implementation to anticipate correctness verification and reduce maintenance costs.

The cycle followed was *Red-Green-Refactor*:

1.  **Red**: writing a test that initially fails for a functionality to be implemented;
2.  **Green**: implementing the minimum code necessary to make the test pass;
3.  **Refactor**: restructuring the code (both test and production) to improve its quality and maintainability.

The application of TDD was prioritized for the *core* components of the system, where functional correctness was critical, while for less critical or experimental components, a more traditional post-implementation testing approach was sometimes used.

---

## **Branching Strategy and Code Management**

For code versioning, we adopted a branching strategy inspired by **GitFlow**, simplified to fit our sprint cycle:

- **`main`**: The main branch contains exclusively stable and releasable code. Each commit on `main` corresponds to a working version of the product.
- **`dev`**: The development branch serves as the integration line for all new functionalities. It represents the current state of development and is kept stable.
- **Feature Branch**: For each significant task or functionality, we created a dedicated branch starting from `dev` (e.g., `feat/case-generation`). This allowed us to
  isolate work in progress, promote targeted code reviews, and keep `dev` always integrable.

---

## **Meeting Organization**

Team meetings were coordinated through the **Microsoft Teams** platform on a regular basis.

As previously described, the beginning of each week saw a combined *Sprint Review* and *Sprint Planning* session, in which the team analyzed the completed work, updated the Product Backlog, and planned the activities for the upcoming sprint.

During the week, additional meetings (2-3 per sprint) allowed the team members to synchronize on progress, discuss technical issues that arose, and reorganize tasks when necessary to maintain alignment with the sprint goals.

---

## **Task Distribution**

At the beginning of each sprint, during *Sprint Planning*, the *Product Backlog* items were selected based on the priorities set by the Product Owner and broken down into operational tasks.

The distribution of tasks among team members was carried out collaboratively, respecting the principle of self-organization proper to Scrum: each member voluntarily chose the tasks to perform based on their skills and availability, providing complexity estimates shared with the rest of the team.

The task breakdown was tracked using **GitHub Projects**, which allowed us to visualize the overall progress of the sprint.

---

## **Development Tools**

The following tools were adopted to support the development process:

### **Build Automation**

**SBT (Simple Build Tool)** was used to automate the operations of source code compilation, dependency management, and test suite execution.

### **Testing**

**ScalaTest** was employed as a framework for defining and executing automated tests, supporting the application of *Test-Driven Development*.

### **Version Control**

**GitHub** was used as a platform for source code hosting, version control, project documentation management, and activity tracking through GitHub Projects.

### **Continuous Integration, Deployment, and Release**

To ensure an efficient and automated development flow, the project adopted an infrastructure based on **GitHub Actions**, configuring workflows for quality checks, testing, documentation deployment, and artifact release.

**Automated Workflows**

1.  **Check for commit compliance**:
    - A workflow was configured to verify that commit messages respect the **Conventional Commits** conventions. This ensures a clear and structured repository history.
    - The check is automatically triggered on every push or pull request to the `main` and `dev` branches.

2.  **Build and deploy documentation with VitePress**:
    - For generating and deploying the documentation, is used **VitePress**, with a workflow that automates its build and publication on **GitHub Pages**.
    - The workflow activates on every push to the `main` and `dev` branches, ensuring the documentation is always up-to-date and accessible.

3.  **Run checks, tests, and coverage**:
    - A main workflow was configured to run code quality checks and execute the test suite.
    - This workflow is configured to trigger automatically in two cases:
        - On every push to the `main` or `dev` branches (ignoring documentation files).
        - On every opening or update of a Pull Request targeting `dev`.
    - **No code can be merged** (via branch protection rules) if the formatting, linting, and test suite checks are not successfully passed.
    - This workflow includes several *jobs*:
        - **`code-checks`**: Runs formatting checks (with **Scalafmt**) and linting (with **Scalafix**).
        - **`test`**: Executes automated tests on a matrix of different operating systems (Linux, Windows, macOS) and JDK versions (17, 21).
        - **`coverage`**: (Executed after successful tests) Generates a code coverage report using **sbt-scoverage** and sends the data to **Coveralls** for a centralized view of test coverage. ([https://coveralls.io/github/LuciaCastellucci/PPS-24-whodunnit](https://coveralls.io/github/LuciaCastellucci/PPS-24-whodunnit))

4.  **Automated Release Packaging**:
    - A workflow was configured to automate the creation of application releases.
    - It triggers automatically whenever a new **tag** (e.g., `v1.0.0`) is pushed to the repository.
    - The workflow performs the following steps:
        - Sets up the Java and SBT environment.
        - Compiles the final executable JAR file (using `sbt clean assembly`).
        - Automatically creates a new **GitHub Release**, attaching the generated `.jar` file as a downloadable artifact and using the tag name.