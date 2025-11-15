# Case Generation

In the project, I primarily worked on the investigative case generation subsystem. This includes the domain types that represent a case (`Case`, `Entity`, `Constraint`) and the LLM integration infrastructure that supports the generation pipeline (`BaseLLMClient`, `GroqProvider`, `GroqLLMProducer`, `Prompt`, `ResponseParser`).

## Core Domain Entities

The foundation of the case generation system consists of the core domain entities: `Case`, `Entity` (with its variants `Character`, `CaseFile`, `CustomEntity`), and `Constraint`.  
These components are modeled following functional programming principles, prioritizing **immutability** and **type safety** through Algebraic Data Types.

All domain components are implemented as immutable case classes. The `Case` trait exposes a read-only interface:

```scala
trait Case:
  def plot: Plot
  def characters: Set[Character]
  def caseFiles: Set[CaseFile]
  def solution: Solution
```

The concrete implementation, `CaseImpl`, is declared `private[model]` to enforce controlled construction through the factory method:

```scala
private[model] final case class CaseImpl(
    plot: Plot,
    characters: Set[Character],
    caseFiles: Set[CaseFile],
    solution: Solution
) extends Case derives ReadWriter
```

The `Case` companion object provides the factory method for controlled construction:
```scala
object Case:
  def apply(constraints: Constraint*)(using producer: Producer[Case]): Either[ProductionError, Case] =
    producer.produce(constraints*)
```

This design choice serves two purposes: it ensures all `Case` instances are created through the validated factory method (`Case.apply`), preventing arbitrary instantiations that might violate domain invariants. 

The entities composing a case are modeled as an Algebraic Data Type using a `sealed trait` with `final case class` constructors:

```scala
sealed trait Entity derives ReadWriter

final case class Character(name: String, role: CaseRole) extends Entity

final case class CaseFile(
    title: String,
    content: String,
    kind: CaseFileType,
    sender: Option[Character],
    receiver: Option[Character],
    date: Option[String]
) extends Entity

final case class CustomEntity(entityType: String) extends Entity
```

The `sealed` modifier creates a closed sum type known to the compiler, enabling exhaustiveness checking in pattern matching.  
When matching on `Entity`, the compiler verifies all cases are handled and produces warnings if a case is missing.
This eliminates entire classes of runtime errors, if we add a new entity type, all existing pattern matches become compilation errors until updated. 

For finite domains like `CaseRole` and `CaseFileType`, I used Scala `enum` syntax:

```scala
enum CaseRole derives ReadWriter:
    case Suspect
    case Victim
    case Witness
    case Investigator
    case Accomplice
    case Informant

enum CaseFileType derives ReadWriter:
    case Message
    case Email
    case Interview
    case Diary
    case TextDocument
    case Notes
```

## Constraint

The `Constraint` system demonstrates the power of ADTs combined with extension methods for domain-specific behavior. Constraints guide the case generation process by specifying user requirements:

```scala
sealed trait Constraint

enum Difficulty(val difficulty: String) extends Constraint:
    case Easy extends Difficulty("Easy")
    case Medium extends Difficulty("Medium")
    case Hard extends Difficulty("Hard")

final case class Theme(value: String) extends Constraint
final case class CharactersRange(min: Int, max: Int) extends Constraint
final case class CaseFilesRange(min: Int, max: Int) extends Constraint
final case class PrerequisitesRange(min: Int, max: Int) extends Constraint
final case class Context(content: String) extends Constraint
```

The sealed ADT structure ensures type safety. We cannot create invalid constraints and pattern matching is exhaustively checked by the compiler. The architecture is designed for extensibility: `Case.apply` accepts multiple constraints (`Constraint*`), allowing future additions without modifying the core generation logic.

Extension methods allow adding behavior to the `Constraint` type without modifying the domain model itself:
```scala
object Constraint:
    extension (c: Constraint)
        def toPromptDescription: String = c match
            case Theme(value) => s"Theme: $value"
            case CharactersRange(min, max) => s"Number of characters: between $min and $max"
            case CaseFilesRange(min, max) => s"Number of case files: between $min and $max"
            case PrerequisitesRange(min, max) => s"Solution prerequisites: between $min and $max"
            case HintKind.Helpful => s"The hint to be generated must be ${HintKind.Helpful.toString}"
            case HintKind.Misleading => s"The hint to be generated must be ${HintKind.Misleading.toString}"
            case Context(content) => s"Additional context:\n\n $content"
            case difficulty: Difficulty => formatDifficultyConstraints(difficulty)
```

The `toPromptDescription` method enables constraints to be transformed into natural language descriptions that are inserted into the LLM prompts. This design makes the constraint system both type-safe (through ADTs) and flexible (through extension methods), allowing easy addition of new constraint types without breaking existing code.

## LLM Integration

### BaseLLMClient

The architecture is built around `BaseLLMClient`, which defines a provider interface using abstract type members:

```scala
abstract class BaseLLMClient(protected val apiKey: String):
    type Request

    final def invoke[T](req: Request)(using parser: ResponseParser[T]): Either[ProductionError, T] =
        for
            rawResponse <- makeCall(req)
            res <- parser.parse(rawResponse)
        yield res

    protected def makeCall(req: Request): Either[ProductionError, String)
```

The type `Request` declaration is an abstract type member, a type that's declared but not defined. This pattern enables family polymorphism: the abstract class defines a family of related types (the LLM client and its request format), but leaves specific members abstract for concrete implementations to define.

This approach has several advantages over generic type parameters (`BaseLLMClient[Req]`):

- Cleaner signatures: No type parameter pollution in the class declaration
- Late binding: The type is defined where it's most natural (the provider implementation), not at the inheritance point
- Encapsulation: The request type becomes part of the provider's implementation details

The `invoke` method uses a for-comprehension to compose the API call and parsing steps.  
This is syntactic sugar for monadic composition:

```scala
makeCall(req).flatMap { rawResponse =>
    parser.parse(rawResponse)
}
```

The `Either` monad enables that: each step can succeed (`Right`) or fail (`Left`). The `for-comprehension` automatically handles the branching—if any step returns `Left`, execution short-circuits and the error propagates without executing subsequent steps. If all steps succeed, the final `yield` produces the result. This eliminates nested error checking while making failure modes explicit in the type signature.

### GroqProvider

The `GroqProvider` trait implements the Groq-specific logic as a mixin using a self-type constraint:

```scala
trait GroqProvider:
self: BaseLLMClient =>

type Request = GroqRequest

protected def makeCall(req: Request): Either[ProductionError, String] =
 /*Implementation of the Groq API call*/
 
protected case class GroqRequest(
    messages: List[GroqMessage],
    model: String,
    temperature: Double = 0.7,
    max_tokens: Int = 4000
)
```

The self-type annotation `self: BaseLLMClient =>` creates a compile-time constraint: this trait can only be mixed into classes that extend `BaseLLMClient`. This is not inheritance—we're not saying `GroqProvider` is a `BaseLLMClient`, but rather that it requires being composed with one. This gives us access to `BaseLLMClient`'s protected members (like `apiKey`) while keeping `GroqProvider` as a composable module.

The concrete `Request` type is defined here (`type Request = GroqRequest`), resolving the abstract type from `BaseLLMClient`. This creates a type-safe connection: the `makeCall` implementation works with `GroqRequest`, and the compiler ensures this matches the abstract `Request` type expected by `BaseLLMClient`.

### GroqLLMProducer

The final composition in `GroqLLMProducer` brings everything together:

```scala
class GroqLLMProducer[T](apiKey: String)
                        (systemPrompt: SystemPrompt = Base, userPrompt: UserPrompt)
                        (using parser: ResponseParser[T])
  extends BaseLLMClient(apiKey)
    with GroqProvider
    with Producer[T]:

    override def produce(constraints: Constraint*): Either[ProductionError, T] =
    val params = Seq(Prompt.Parameter(Prompt.Placeholder.Constraints, constraints.map(_.toPromptDescription)))
    for
      systemPrompt <- systemPrompt.build()
      userPrompt <- userPrompt.build(params)
      request = GroqRequest(
        model = model,
        messages = List(
          GroqMessage("system", systemPrompt),
          GroqMessage("user", userPrompt)
        )
      )
      result <- invoke[T](request)(using parser)
    yield result
```

Scala allow composes multiple traits into a single class following a well-defined order.  
The `GroqLLMProducer`:
- Extends `BaseLLMClient` to get the template method `invoke`
- Mixes in `GroqProvider` to get the concrete `Request` type and `makeCall` implementation
- Mixes in `Producer[T]` to satisfy the production interface

The self-type constraint in `GroqProvider` is satisfied because `GroqLLMProducer` extends `BaseLLMClient`.  
If we tried to mix `GroqProvider` into a class not extending `BaseLLMClient`, the code wouldn't compile—this is static, compile-time safety.


## Prompt System

The prompt system constructs LLM requests by loading templates from files and substituting constraint values. The `Prompt` trait provides the core functionality:

```scala
trait Prompt(path: String):
    def build(params: Seq[Prompt.Parameter] = Seq.empty): Either[ProductionError, String] =
        loadTemplate(path).map { template =>
          params.foldLeft(template) { case (acc, parameter) =>
          acc.replace(parameter.placeholder.tag, parameter.values.mkString("\n"))
    }
  }
```

The key pattern here is using `foldLeft` to apply successive transformations. Given a template with placeholders like `{{CONSTRAINTS}}`, the function iterates through parameters and replaces each placeholder with its corresponding values. This is a functional accumulation pattern—no mutable variables, just pure transformation of strings.
```scala
object Prompt:
    final case class Parameter(placeholder: Placeholder, values: Seq[String])
    
    enum Placeholder(val tag: String):
        case Constraints extends Placeholder("{{CONSTRAINTS}}")
        case None extends Placeholder("")
```

The constraint-to-prompt transformation uses the `toPromptDescription` extension method defined earlier, creating a pipeline: constraints → descriptions → parameters → substituted template.

Prompts are organized using enums that extend the `Prompt` trait:

```scala
enum SystemPrompt(path: String) extends Prompt(path):
    case Base extends SystemPrompt("/prompts/system/base.md")

enum UserPrompt(path: String) extends Prompt(path):
    case Case extends UserPrompt("/prompts/user/case-generation.md")
    case Hint extends UserPrompt("/prompts/user/hint-generation.md")
```
This design demonstrates enum inheritance: the enum cases inherit behavior from `Prompt` while providing specific template paths. The separation between `SystemPrompt` (defines the LLM's role) and `UserPrompt` (specifies the task) follows the standard LLM prompting pattern. Adding a new prompt type requires only adding an enum case—the template loading and substitution logic is inherited automatically.

## Response Parsing

The response parsing system uses type classes to achieve ad-hoc polymorphism without inheritance:

```scala
trait ResponseParser[T]:
  def parse(jsonString: String): Either[ProductionError, T]

object ResponseParser:
  given ResponseParser[Case] with
    override def parse(jsonString: String): Either[ProductionError, Case] =
      parseString[CaseImpl](jsonString)

  given ResponseParser[Hint] with
    override def parse(jsonString: String): Either[ProductionError, Hint] =
      parseString[HintImpl](jsonString)

  private def parseString[T: Reader](jsonString: String): Either[ProductionError, T] =
    Try(read[T](jsonString)).toEither.left.map { e =>
      ProductionError.ParseError(s"Unexpected error during parsing: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}")
    }
```

The type class pattern consists of three components:
1. Type class trait: `ResponseParser[T]` defines the interface
2. Type class instances: `given` definitions provide implementations for specific types
3. Type class constraints: `using` parameters require instances at call sites

The `parseString` helper uses a context bound `[T: Reader]`, which is syntactic sugar for `[T](using reader: Reader[T])`. This is a contextual parameter—the compiler automatically searches for and injects a `Reader[T]` instance from the implicit scope.

The critical implementation detail is that these `Reader` instances are not manually written—they're automatically generated by the compiler:

```scala
private[model] final case class CaseImpl(...) extends Case derives ReadWriter
```

The `derives` keyword triggers automatic derivation: compile-time metaprogramming where the compiler generates serialization/deserialization logic based on the case class structure. This is a form of compiler as a tool—we declare what we want (a serializable case class), and the compiler generates how to achieve it.

This eliminated hundreds of lines of manual parsing code that existed in the previous architecture. The old system required:
- Intermediate DTO (Data Transfer Object) classes
- Manual field-by-field copying from JSON to domain objects
- Error handling for each field

The new design is purely declarative: we declare that `CaseImpl` should be serializable (`derives ReadWriter`), and the compiler does the rest. This demonstrates the power of type-driven development—shifting complexity from runtime (error-prone manual code) to compile-time (verified code generation).

