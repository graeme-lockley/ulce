# UCLE Design Document

Unified Composable Live Environment (UCLE) is a reactive, strongly-typed, live-programmable environment for composing computation from agent-based components, stream interactions, and structurally typed dataflows. This document outlines the core principles, language syntax, runtime semantics, type system, agent model, lifecycle mechanics, and foundational libraries that make up UCLE.

---

## 1. Philosophy

UCLE is a blend of:

* **Smalltalk's moldable environment**
* **Oberon's declarative modularity**
* **Unix's stream composition and process table**
* **Elm's runtime effect model**
* **Functional combinator orchestration**

It is designed for:

* Live, inspectable systems
* Declarative orchestration of stateful agents
* Reactive composition of streams
* Structural and open typing
* Functional and observable semantics

---

## 2. Core Concepts

### 2.1 Why "Agent"?

Although the underlying execution model of UCLE strongly resembles the **Actor model**—with encapsulated state, message-passing, and isolated concurrency—the term **Agent** was chosen deliberately:

* **Approachability**: "Agent" is more welcoming and intuitive than "Actor," especially to developers not versed in concurrency theory.
* **Intentionality**: Agents do things — they compute, transform, react. The name evokes autonomy and functional purpose.
* **Composability**: In UCLE, agents are composable, structured, and reactive. The term aligns well with how one constructs a network of behavior.
* **Avoids Confusion**: "Actor" is overloaded by frameworks like Akka and Erlang, which carry performance and concurrency expectations. UCLE agents emphasize *semantic structure*, *live interaction*, and *type safety*.

Thus, the UCLE system adopts the term **agent** to describe its fundamental units of computation.

### 2.2 Agents

* Agents are autonomous reactive components.
* Each agent has:

  * Implicit `self.in: Stream<A>` and `self.out: Stream<B>`
  * Declared as `agent Name<T>[A, B](params...) { ... }`
  * Top-level `let` declarations for static or computed state
  * An optional `init { ... }` block for imperative setup
  * Declarative message handlers (`on`) and public methods (`fn`)

### 2.3 Streams

* Streams are append-only sequences of values.
* Typed as `Stream<T>`
* Created implicitly as part of agent instantiation
* Lifecycle is tied to the owning agent
* Stream observers can be attached via `observe(...)`
* Observers are cancelled and streams closed when the agent is destroyed

### 2.4 Agent Lifecycle

* Constructor parameters are evaluated and bound immediately
* Top-level `let` values are initialized in declaration order
* The `init { ... }` block is executed after bindings
* Then stream `on` handlers become active
* Agents live as long as they are:

  * Bound to a value in scope
  * Registered in an `agentTable`
* When no references exist and the agent is not registered, it is garbage collected
* When destroyed:

  * `init` and handlers conclude
  * All observers are cancelled
  * Streams are closed

### 2.5 Agent Table

* Global or scoped registry of long-lived agents
* Prevents garbage collection and retains identity
* Auto-generates a **typed interface** for lookup and inspection

```ulce
val agents: {
  "/fs/reader": Agent<FileReaderInput, Char>,
  "/analytics/parser": Agent<String, AST>
}
```

---

## 3. Language

### 3.1 Syntax

* Expression-oriented
* Strongly typed
* Declarative structure with functional blocks

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

* Structural, not nominal
* Open union types (e.g. `A | B`) support subtype extensibility
* Record types (e.g. `rect { x: Int, y: Int }`)
* Function types `(A) → B`
* Optional values via `?`:

  * Optional fields: `rect { name: String, age?: Int }`
  * Optional expressions: `val maybe = value?`
* Tagged variant sugar:

  ```ulce
  Left(v: A)  ≡  rect { tag: "Left", v: A }
  Right(v: B) ≡  rect { tag: "Right", v: B }
  ```
* Literal types allowed in type expressions: e.g. `tag: "Ok"`, `tag: "Err"`
* Pattern matching on structural tags
* Recursive and mutually recursive types allowed via `type` and `and`

---

## 4. Agents

### 4.1 Minimal Agent Example

```ulce
agent Mapper<A, B>[A, B](f: (A) -> B) {
  on v: A => emit(f(v))
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
    emit(msg)
  }
}
```

### 4.3 Combinator Agents

A **combinator agent** is one that reacts to observed output streams rather than listening on `self.in`. These are used for wiring and composing agents into reactive systems.

#### Naming Convention:

| Category      | Prefix | Example   |
| ------------- | ------ | --------- |
| Source        | `Gen`  | `GenTime` |
| Transformer   | –      | `Mapper`  |
| Combinator    | `Comb` | `CombZip` |
| Observer/Sink | `Tap`  | `TapLog`  |

#### Template:

```ulce
agent CombZip<A, B>[Unknown, (A, B)](left: Agent<Any, A>, right: Agent<Any, B>) {
  let leftBuffer: List<A> = []
  let rightBuffer: List<B> = []

  let leftObs = left.stdout.observe(fun(msg: A) => { ... })
  let rightObs = right.stdout.observe(fun(msg: B) => { ... })

  fun deinit() => {
    leftObs.cancel()
    rightObs.cancel()
  }
}
```

#### Characteristics:

* No `self.in` usage
* Driven by observation (`observe(...)`)
* Often used for coordination, wiring, or combinator logic
* Output-only behavior

---

## 5. Runtime & Execution Semantics

### 5.1 Lifecycle

* Parameter binding → let initialization → `init {}` → reactive `on` handlers

### 5.2 Emission and Observation

* `emit(self.out, value)` emits to the output stream
* `observe(agent, fn)` attaches listeners to output

### 5.3 Determinism

* Single-threaded agents
* Atomic message handling
* Observer callbacks fire after handler returns

---

## 6. Libraries & Combinators

* `mapAgent(f: A → B): Agent<A, B>`
* `filterAgent(pred: A → Bool): Agent<A, A>`
* `compose(a: Agent<A, B>, b: Agent<B, C>): Agent<A, C>`
* `zipAgent<A, B> = Zip<ZipInput<A, B>, [A, B]>`
* `merge(a: Agent<A, C>, b: Agent<B, C>): Agent<A | B, C>`

---

## 7. Design Doctrines

1. **Agents are stream transformers with lifecycle and structure.**
2. **Type system is structural and open.**
3. **Tagged union sugar builds extensible records.**
4. **Optional fields and values use `?`.**
5. **Record literals use `rect { ... }` for clarity.**
6. **Literal types are valid type expressions for pattern matching.**
7. **Lifecycle is deterministic and observable.**
8. **Initialization is declarative (params, let) and procedural (`init`).**
9. **Recursive types and mutual ADTs are supported via `type` + `and`.**
10. **Reactive orchestration encourages compositionality.**
11. **Combinator agents enable flexible, input-free stream wiring.**

---

## 8. Relation to Prior Work

UCLE is informed by many streams of programming language and system design research but introduces a novel synthesis that repositions key ideas:

### Actor Model

* Inspired by Hewitt/Agha's message-passing model.
* UCLE agents differ by being structurally typed, lifecycle-managed, and declaratively wired — unlike traditional opaque actors.

### Functional Reactive Programming (FRP)

* Shares reactivity and stream transformation primitives.
* Diverges by grounding in compositional agent constructs with initialization and teardown semantics.

### Elm Architecture

* Similar in modeling updates from events.
* UCLE decentralizes this model, allowing multiple autonomous reactive agents instead of a central loop.

### Rx and Observables

* Aligns with reactive composition and observation semantics.
* UCLE introduces typed agents with memory, structural typing, and stream lifecycle binding — not present in Rx.

### Dataflow Languages

* UCLE inherits the spirit of wiring boxes and flows but embeds the model in a live-coded, strongly typed, inspectable environment.

### Novel Contributions

* **Optional input stream**: UCLE agents can function without owning `in`, relying instead on observation.
* **Lifecycle-aware observation**: Observers attach and detach cleanly with `deinit()`.
* **Declarative wiring** of reactive computation.
* **Structural typing** and open union models support extensible composition.
* **Compositional agent design** makes orchestration reusable and analyzable.

---

This document represents the foundational vision of UCLE and may evolve as the system matures. Future extensions may include foreign function interfaces, stream backpressure strategies, actor supervision trees, distributed messaging, and UI composition.
