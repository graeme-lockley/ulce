# UCLE Type System and Inference

## Abstract

This document presents a comprehensive description of the UCLE language's type system, including its syntax, type rules, and inference algorithm. UCLE is a statically-typed functional language with Hindley-Milner polymorphic type inference, enabling strong compile-time guarantees while minimizing the need for explicit type annotations. We present the formal semantics of the type system, along with inference algorithms, theorems of correctness, and example derivations.

## 1. Introduction

UCLE (Universal Composite Language for Expressions) is a statically-typed functional language designed to balance expressiveness with safety. Its key features include:

1. **Hindley-Milner type inference**: Types are inferred automatically with minimal annotations
2. **Algebraic data types**: Support for sum and product types
3. **Pattern matching**: First-class support for destructuring data
4. **Parametric polymorphism**: Generic types and functions
5. **First-class functions**: Functions as values with closures

The type system ensures that well-typed programs cannot encounter certain runtime errors, following the principle of "well-typed programs don't go wrong" [Milner, 1978].

## 2. Language Syntax

UCLE's syntax is designed to be concise yet readable, with explicit delimiters for statements and expressions. Below, we present the abstract syntax of the core language in BNF notation:

```
Program      ::= Statement*
Statement    ::= TypeDecl | LetDecl | ExprStmt
TypeDecl     ::= "type" TypeName GenericParams? "=" TypeExpr ";"
LetDecl      ::= "let" Identifier GenericParams? Params? TypeAnnotation? "=>" Expression ";"
ExprStmt     ::= Expression ";"

Expression   ::= PrimaryExpr ExpressionSuffix*
PrimaryExpr  ::= Lambda | ConstExpr | MatchExpr | RecordLiteral
               | Identifier | Literal | "(" Expression ")"

Lambda       ::= "fn" "(" ParamList? ")" "=>" Expression
ConstExpr    ::= "const" Identifier "=" Expression "in" Expression
MatchExpr    ::= "match" Expression "{" (Case)+ "}"
Case         ::= "case" Pattern "=>" Expression
RecordLiteral::= "rect" "{" (FieldInit ("," FieldInit)*)? "}"

Pattern      ::= RecordPattern | IdentifierPattern | Literal
RecordPattern::= "rect" "{" (FieldPattern ("," FieldPattern)*)? "}"
IdentifierPattern ::= ConstructorPattern | VariablePattern

TypeExpr     ::= UnionType ("->" TypeExpr)?
UnionType    ::= IntersectionType ("|" IntersectionType)*
IntersectionType ::= PrimaryType ("&" PrimaryType)*
PrimaryType  ::= TypeName TypeArgs? | RecordType | LiteralType | "(" TypeExpr ")"
```

## 3. Type System

### 3.1 Types

UCLE's type system includes the following kinds of types:

1. **Basic types**: Primitive types such as `Number`, `String`, `Boolean`
2. **Function types**: Types of the form `T1 -> T2`
3. **Record types**: Product types of named fields, e.g., `rect { name: String, age: Number }`
4. **Union types**: Sum types, e.g., `T1 | T2`
5. **Intersection types**: Types that satisfy multiple constraints, e.g., `T1 & T2`
6. **Type variables**: Used in polymorphic types, e.g., `α`, `β`
7. **Generic types**: Parameterized types, e.g., `List<T>`
8. **Literal types**: Types for literal values, e.g., `"hello"` as a string literal type

Formally, we define the set of types τ as:

```
τ ::= α                           (Type variable)
    | B                           (Base type: Number, String, Boolean)
    | L                           (Literal type: "hello", 42, true)
    | τ1 → τ2                     (Function type)
    | rect { l1: τ1, ..., ln: τn } (Record type)
    | τ1 | τ2                     (Union type)
    | τ1 & τ2                     (Intersection type)
    | D<τ1, ..., τn>              (Generic type application)
```

A **type scheme** σ is a type that may be universally quantified:

```
σ ::= τ                           (Monotype)
    | ∀α1...αn.τ                  (Polytype)
```

### 3.2 Type Contexts and Judgments

A **type context** Γ maps identifiers to type schemes, representing assumptions about variables in scope:

```
Γ ::= ∅                           (Empty context)
    | Γ, x:σ                      (Context extension)
```

We use the following **type judgments** to express typing properties:

1. `Γ ⊢ e : τ` - Expression `e` has type `τ` under context `Γ`
2. `Γ ⊢ p : τ ⫶ Δ` - Pattern `p` matches values of type `τ` and, when matched, binds variables according to context Δ
3. `Γ ⊢ d : Δ` - Declaration `d` binds variables according to context Δ

### 3.3 Type Rules

We present the key typing rules for UCLE below.

#### Variables and Literals

```
[VAR]
x:σ ∈ Γ     τ = inst(σ)
------------------------
     Γ ⊢ x : τ

[LIT-NUM]
------------------------
Γ ⊢ n : Number

[LIT-STR]
------------------------
Γ ⊢ s : String

[LIT-BOOL]
------------------------
Γ ⊢ b : Boolean
```

#### Functions and Application

```
[ABS]
Γ, x:τ1 ⊢ e : τ2
---------------------------------
Γ ⊢ fn(x:τ1) => e : τ1 → τ2

[APP]
Γ ⊢ e1 : τ1 → τ2    Γ ⊢ e2 : τ1
---------------------------------
       Γ ⊢ e1(e2) : τ2
```

#### Records and Field Access

```
[RECORD]
Γ ⊢ e1 : τ1  ...  Γ ⊢ en : τn
-----------------------------------------------
Γ ⊢ rect { l1: e1, ..., ln: en } : rect { l1: τ1, ..., ln: τn }

[FIELD]
Γ ⊢ e : rect { ..., l: τ, ... }
-------------------------------
       Γ ⊢ e.l : τ
```

#### Pattern Matching

```
[MATCH]
Γ ⊢ e : τ    Γ ⊢ p1 : τ ⫶ Δ1    Γ, Δ1 ⊢ e1 : τ'    ...    Γ ⊢ pn : τ ⫶ Δn    Γ, Δn ⊢ en : τ'
-----------------------------------------------------------------------------------------
                    Γ ⊢ match e { case p1 => e1 ... case pn => en } : τ'

[PAT-VAR]
----------------------------
Γ ⊢ x : τ ⫶ (x:τ)

[PAT-LIT]
----------------------
Γ ⊢ lit : typeof(lit) ⫶ ∅

[PAT-RECORD]
Γ ⊢ p1 : τ1 ⫶ Δ1    ...    Γ ⊢ pn : τn ⫶ Δn
-----------------------------------------------------------------------
Γ ⊢ rect {l1: p1, ..., ln: pn} : rect {l1: τ1, ..., ln: τn} ⫶ Δ1,...,Δn
```

#### Let Bindings and Recursion

```
[LET]
Γ ⊢ e1 : τ1    Γ, x:gen(Γ, τ1) ⊢ e2 : τ2
-----------------------------------------
Γ ⊢ const x = e1 in e2 : τ2

[LET-REC]
Γ, x:τ1 ⊢ e1 : τ1    Γ, x:gen(Γ, τ1) ⊢ e2 : τ2
----------------------------------------------
Γ ⊢ let x => e1; e2 : τ2
```

#### Type Declarations

```
[TYPE-DECL]
Γ, T:κ ⊢ τ : κ    Γ, T = τ ⊢ e : τ'
------------------------------------
  Γ ⊢ type T = τ; e : τ'
```

### 3.4 Subtyping

UCLE implements a structural subtyping relation, which we denote as `τ1 <: τ2` (τ1 is a subtype of τ2). Key subtyping rules include:

```
[S-REFL]
---------
τ <: τ

[S-TRANS]
τ1 <: τ2    τ2 <: τ3
---------------------
      τ1 <: τ3

[S-FUN]
τ3 <: τ1    τ2 <: τ4
---------------------
τ1 → τ2 <: τ3 → τ4

[S-RECORD]
τ1 <: τ'1  ...  τn <: τ'n
------------------------------------------------------
rect {l1: τ1, ..., ln: τn, ...} <: rect {l1: τ'1, ..., ln: τ'n}

[S-UNION-L]
τ1 <: τ3    τ2 <: τ3
---------------------
    τ1 | τ2 <: τ3

[S-UNION-R1]
--------------
τ1 <: τ1 | τ2

[S-UNION-R2]
--------------
τ2 <: τ1 | τ2

[S-INTERSECT-L1]
----------------
τ1 & τ2 <: τ1

[S-INTERSECT-L2]
----------------
τ1 & τ2 <: τ2

[S-INTERSECT-R]
τ3 <: τ1    τ3 <: τ2
---------------------
    τ3 <: τ1 & τ2
```

## 4. Type Inference Algorithm

UCLE implements a variant of the Algorithm W [Damas-Milner, 1982] for type inference, extended to handle records, unions, and intersection types. The algorithm infers the most general type for a program while requiring minimal type annotations.

### 4.1 Overview of the Inference Process

The type inference process occurs in several phases:

1. **Symbol table construction**: Build a hierarchical symbol table tracking identifiers in each scope
2. **Constraint generation**: Traverse the AST and generate constraints between types
3. **Constraint solving**: Unify type constraints to find a consistent solution
4. **Type decoration**: Annotate the AST with inferred types

### 4.2 Constraint Generation

The constraint generator produces a set of equations between types. For each AST node, we generate constraints based on the typing rules. For example:

```kotlin
fun generateConstraints(node: AstNode, typeEnv: TypeEnv): Pair<Type, Set<Constraint>> {
    return when (node) {
        is LiteralExpr -> when (node) {
            is IntegerLiteralExpr -> Pair(NumberType, emptySet())
            is StringLiteralExpr -> Pair(StringType, emptySet())
            is BooleanLiteralExpr -> Pair(BooleanType, emptySet())
        }
        
        is LowerIdentifierExpr -> {
            val varType = typeEnv.lookup(node.token.lexeme) ?: 
                throw TypeError("Undefined variable: ${node.token.lexeme}")
            Pair(varType, emptySet())
        }
        
        is Lambda -> {
            val paramTypes = node.params?.paramList?.map { 
                it.typeAnnotation?.let { ann -> 
                    resolveType(ann.typeExpr, typeEnv) 
                } ?: TypeVariable(freshTypeVar())
            } ?: emptyList()
            
            val extendedEnv = typeEnv.extend(
                node.params?.paramList?.zip(paramTypes) ?: emptyList()
            )
            
            val (bodyType, bodyConstraints) = generateConstraints(node.body, extendedEnv)
            val fnType = FunctionType(paramTypes, bodyType)
            
            Pair(fnType, bodyConstraints)
        }
        
        is ApplicationSuffix -> {
            val (fnType, fnConstraints) = generateConstraints(node.function, typeEnv)
            val (argTypes, argConstraints) = node.args?.let { args ->
                val (types, constraints) = args.map { generateConstraints(it, typeEnv) }.unzip()
                Pair(types, constraints.flatten().toSet())
            } ?: Pair(emptyList(), emptySet())
            
            val resultType = TypeVariable(freshTypeVar())
            val appConstraint = Constraint.Equal(
                fnType,
                FunctionType(argTypes, resultType)
            )
            
            Pair(resultType, fnConstraints + argConstraints + appConstraint)
        }
        
        // More cases for other node types...
    }
}
```

### 4.3 Constraint Solving with Unification

The constraint solver uses the unification algorithm to solve the generated constraints, producing a substitution (mapping from type variables to types) that satisfies all constraints:

```kotlin
fun solve(constraints: Set<Constraint>): Substitution {
    val substitution = mutableMapOf<Int, Type>()
    
    for (constraint in constraints) {
        when (constraint) {
            is Constraint.Equal -> {
                val s = unify(
                    applySubst(constraint.t1, substitution),
                    applySubst(constraint.t2, substitution)
                )
                substitution.putAll(s)
            }
            is Constraint.Subtype -> {
                val s = subtype(
                    applySubst(constraint.sub, substitution),
                    applySubst(constraint.sup, substitution)
                )
                substitution.putAll(s)
            }
        }
    }
    
    return substitution
}

fun unify(t1: Type, t2: Type): Substitution {
    return when {
        t1 == t2 -> emptyMap()
        
        t1 is TypeVariable -> {
            if (occursCheck(t1.id, t2)) {
                throw TypeError("Recursive type detected: ${t1.id} occurs in $t2")
            }
            mapOf(t1.id to t2)
        }
        
        t2 is TypeVariable -> unify(t2, t1)
        
        t1 is FunctionType && t2 is FunctionType -> {
            if (t1.paramTypes.size != t2.paramTypes.size) {
                throw TypeError("Function arity mismatch: ${t1.paramTypes.size} vs ${t2.paramTypes.size}")
            }
            
            val s1 = unify(t1.returnType, t2.returnType)
            val s2 = unifyList(
                t1.paramTypes.map { applySubst(it, s1) },
                t2.paramTypes.map { applySubst(it, s1) }
            )
            
            s1 + s2
        }
        
        t1 is RecordType && t2 is RecordType -> {
            val commonFields = t1.fields.keys.intersect(t2.fields.keys)
            val s = mutableMapOf<Int, Type>()
            
            for (field in commonFields) {
                val fieldS = unify(
                    applySubst(t1.fields[field]!!, s),
                    applySubst(t2.fields[field]!!, s)
                )
                s.putAll(fieldS)
            }
            
            // Handle width subtyping if needed
            
            s
        }
        
        // More cases for other type constructs...
        
        else -> throw TypeError("Cannot unify $t1 with $t2")
    }
}
```

### 4.4 Generalization and Instantiation

The algorithm implements generalization to introduce polymorphism and instantiation to use polymorphic types:

```kotlin
fun generalize(typeEnv: TypeEnv, type: Type): TypeScheme {
    val freeInType = freeTypeVars(type)
    val freeInEnv = typeEnv.freeTypeVars()
    val quantified = freeInType - freeInEnv
    
    return TypeScheme(quantified.toList(), type)
}

fun instantiate(scheme: TypeScheme): Type {
    val subst = scheme.variables.associateWith { TypeVariable(freshTypeVar()) }
    return applySubst(scheme.type, subst)
}
```

### 4.5 Decoration of the AST

After inference, we decorate the AST with inferred types, creating a typed version of the AST:

```kotlin
fun decorateAst(node: AstNode, types: Map<AstNode, Type>): TypedAstNode {
    return when (node) {
        is Program -> TypedProgram(
            node.statements.map { decorateAst(it, types) as TypedStatement },
            types[node] ?: ErrorType
        )
        
        is LowerIdentifierExpr -> TypedIdentifierExpr(
            node.token.lexeme,
            node.position,
            types[node] ?: ErrorType
        )
        
        // More cases for other node types...
    }
}
```

## 5. Type Inference Example

To illustrate type inference in action, let's walk through the inference process for a simple UCLE program:

```
let identity => fn(x) => x;
let pair => fn(a, b) => rect { first: a, second: b };
let getFst => fn(p) => p.first;

let result => getFst(pair(identity(5), "hello"));
```

### Step 1: Symbol Table Construction

First, we create a symbol table for the top-level declarations:
- `identity` - Function that returns its input
- `pair` - Function that creates a pair record
- `getFst` - Function that extracts the first element of a pair
- `result` - The result of applying these functions

### Step 2: Constraint Generation

For each expression, we generate type variables and constraints:

1. `identity` has type `α → α` where `x` has type `α`
2. `pair` has type `(β, γ) → rect { first: β, second: γ }`
3. `getFst` has type `δ → ε` with constraint `δ <: rect { first: ε, ... }`
4. `identity(5)` adds constraint `α = Number`
5. `pair(identity(5), "hello")` adds constraints `β = Number` and `γ = String`
6. `getFst(pair(identity(5), "hello"))` adds constraint `δ = rect { first: Number, second: String }`

### Step 3: Constraint Solving

Unifying these constraints:
1. `α = Number` from `identity(5)`
2. `β = Number`, `γ = String` from the pair construction
3. `δ = rect { first: Number, second: String }` from function application
4. `ε = Number` from unifying the result of `getFst`

### Step 4: Type Decoration

After solving, we can decorate the AST with inferred types:
- `identity : ∀α. α → α`
- `pair : ∀β,γ. (β, γ) → rect { first: β, second: γ }`
- `getFst : ∀ε,ζ. rect { first: ε, ... } → ε`
- `result : Number`

## 6. Theoretical Properties

### 6.1 Soundness

**Theorem (Type Soundness)**: If Γ ⊢ e : τ and e evaluates to a value v, then v has type τ.

Soundness is proven using the standard approach of progress and preservation theorems:

**Progress**: A well-typed expression is either a value or can take a step according to the evaluation rules.

**Preservation**: If an expression e has type τ and e steps to e', then e' also has type τ.

### 6.2 Principal Types

**Theorem (Principal Types)**: If an expression e is typable in context Γ, then there exists a principal type τ such that Γ ⊢ e : τ, and for any other type τ' where Γ ⊢ e : τ', there exists a substitution S such that S(τ) = τ'.

The type inference algorithm computes this principal type.

### 6.3 Complexity

The worst-case time complexity of the Hindley-Milner type inference is exponential, but in practice, the algorithm performs efficiently for most programs. Specific optimizations in UCLE's implementation improve performance for common cases.

## 7. Extensions and Future Work

### 7.1 Dependent Types

A natural extension to UCLE would be to support dependent types, allowing types to depend on values.

### 7.2 Effect System

Adding an effect system would enable tracking of side effects and improved reasoning about program behavior.

### 7.3 Refinement Types

Refinement types would allow more precise specifications, such as non-negative numbers or non-empty lists.

### 7.4 Gradual Typing

Introducing gradual typing would provide a smooth path between dynamic and static typing, beneficial for incremental adoption.

## 8. Conclusion

UCLE's type system combines the safety of static typing with the convenience of type inference. The Hindley-Milner algorithm, extended with subtyping for records and support for union and intersection types, enables expressive yet safe programming. The formal type system and inference algorithm ensure that well-typed UCLE programs don't go wrong while requiring minimal type annotations.

## References

1. Damas, L., & Milner, R. (1982). Principal type-schemes for functional programs. *In Proceedings of the 9th ACM SIGPLAN-SIGACT Symposium on Principles of Programming Languages*.

2. Hindley, R. (1969). The principal type-scheme of an object in combinatory logic. *Transactions of the American Mathematical Society*, 146, 29-60.

3. Milner, R. (1978). A theory of type polymorphism in programming. *Journal of Computer and System Sciences*, 17(3), 348-375.

4. Pierce, B. C. (2002). *Types and Programming Languages*. MIT Press.

5. Pottier, F., & Rémy, D. (2005). The essence of ML type inference. *Advanced Topics in Types and Programming Languages*, 389-489.

6. Reynolds, J. C. (1983). Types, abstraction and parametric polymorphism. *In Information Processing 83*, 513-523.

7. Wadler, P., & Blott, S. (1989). How to make ad-hoc polymorphism less ad hoc. *In Proceedings of the 16th ACM SIGPLAN-SIGACT Symposium on Principles of Programming Languages*, 60-76.
