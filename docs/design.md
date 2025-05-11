# UCLE Design Document

Unified Composable Live Environment (UCLE) is a reactive, strongly-typed,
live-programmable environment for composing computation from agent-based
components, stream interactions, and structurally typed dataflows. This document
outlines the core principles, language syntax, runtime semantics, type system,
agent model, lifecycle mechanics, and foundational libraries that make up UCLE.

## 1. Philosophy

UCLE is a blend of:

- **Smalltalk's moldable environment**
- **Oberon's declarative modularity**
- **Unix's stream composition and process table**
- **Elm's runtime effect model**
- **Functional combinator orchestration**

It is designed for:

- Live, inspectable systems
- Declarative orchestration of stateful agents
- Reactive composition of streams
- Structural and open typing
- Functional and observable semantics

## 2. Core Concepts

### 2.1 Agents

- Agents are autonomous reactive components.
- Each agent has:

  - Implicit `self.in: Stream<A>` and `self.out: Stream<B>`
  - Declared as `agent Name<T>[A, B](params...) { ... }`
  - Top-level `let` declarations for static or computed state
  - An optional `init { ... }` block for imperative setup
  - Declarative message handlers (`on`) and public methods (`fn`)

### 2.2 Streams

- Streams are append-only sequences of values.
- Typed as `Stream<T>`
- Created implicitly as part of agent instantiation
- Lifecycle is tied to the owning agent
- Stream observers can be attached via `observe(...)`
- Observers are cancelled and streams closed when the agent is destroyed

### 2.3 Agent Lifecycle

- Constructor parameters are evaluated and bound immediately
- Top-level `let` values are initialized in declaration order
- The `init { ... }` block is executed after bindings
- Then stream `on` handlers become active
- Agents live as long as they are:

  - Bound to a value in scope
  - Registered in an `agentTable`
- When no references exist and the agent is not registered, it is garbage
  collected
- When destroyed:

  - `init` and handlers conclude
  - All observers are cancelled
  - Streams are closed

### 2.4 Agent Table

- Global or scoped registry of long-lived agents
- Prevents garbage collection and retains identity
- Auto-generates a **typed interface** for lookup and inspection

```ulce
val agents: {
  "/fs/reader": Agent<FileReaderInput, Char>,
  "/analytics/parser": Agent<String, AST>
}
```

## 3. Language

### 3.1 Syntax

- Expression-oriented
- Strongly typed
- Declarative structure with functional blocks

### 3.2 Agent Declaration Syntax

```ulce
agent Name<T1, T2>[Input, Output](params...) {
  let ...
  init { ... }
  on ... => ...
  fn ... => ...
}
```

### 3.3 Type System

- Structural, not nominal
- Open union types (e.g. `A | B`) support subtype extensibility
- Record types (e.g. `rect { x: Int, y: Int }`)
- Function types `(A) → B`
- Optional values via `?`:

  - Optional fields: `rect { name: String, age?: Int }`
  - Optional expressions: `val maybe: Int? = Nothing`
- Tagged variant sugar:

  ```ulce
  Left(v: A)  ≡  rect { tag: "Left", v: A }
  Right(v: B) ≡  rect { tag: "Right", v: B }
  ```
- Literal types allowed in type expressions: e.g. `tag: "Ok"`, `tag: "Err"`
- Pattern matching on structural tags
- Recursive and mutually recursive types allowed via `type` and `and`

## 4. Agents

### 4.1 Minimal Agent Example

```ulce
agent Mapper<A, B>[A, B](f: (A) -> B) {
  on v: A => !f(v)
}
```

### 4.2 Extended Lifecycle Example

```ulce
agent Logger<T>[T, T](prefix: String) {
  let count = 0

  init {
    println("Logger initialized with prefix $prefix")
  }

  on msg: T => {
    println("$prefix [$count]: $msg")
    count = count + 1
    !msg
  }
}
```

## 5. Runtime & Execution Semantics

### 5.1 Lifecycle

- Parameter binding → let initialization → `init {}` → reactive `on` handlers

### 5.2 Emission and Observation

- `self.out ! value` emits to the output stream
- `agent ? fn` attaches listeners to output

### 5.3 Determinism

- Single-threaded agents
- Atomic message handling
- Observer callbacks fire after handler returns

## 6. Libraries & Combinators

- `mapAgent(f: A → B): Agent<A, B>`
- `filterAgent(pred: A → Bool): Agent<A, A>`
- `compose(a: Agent<A, B>, b: Agent<B, C>): Agent<A, C>`
- `zipAgent<A, B> = Zip<ZipInput<A, B>, [A, B]>`
- `merge(a: Agent<A, C>, b: Agent<B, C>): Agent<A | B, C>`

## 7. Design Doctrines

1. **Agents are stream transformers with lifecycle and structure.**
2. **Type system is structural and open.**
3. **Tagged union sugar builds extensible records.**
4. **Optional fields and values use ************`?`************.**
5. **Record literals use ************`rect { ... }`************ for clarity.**
6. **Literal types are valid type expressions for pattern matching.**
7. **Lifecycle is deterministic and observable.**
8. **Initialization is declarative (params, let) and procedural
   (********`init`****\*\*\*\*\*\*\*\*).**
9. **Recursive types and mutual ADTs are supported via
   ************`type`************ + ************`and`************.**
10. **Reactive orchestration encourages compositionality.**

This document represents the foundational vision of UCLE and may evolve as the
system matures. Future extensions may include foreign function interfaces,
stream backpressure strategies, actor supervision trees, distributed messaging,
and UI composition.
