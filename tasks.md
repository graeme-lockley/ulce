# Task List: Implementing a Recursive Descent Parser for the mini_ucle_ll1 Grammar in Kotlin

## Background
This document outlines the tasks required to build a recursive descent parser for the mini_ucle_ll1 grammar in Kotlin. The parser will:
1. Accept an input string
2. Parse it according to the LL(1) grammar defined in `mini_ucle_ll1.grammar`
3. Return an Abstract Syntax Tree (AST) if parsing succeeds
4. Return precise error information if parsing fails (expected tokens, encountered token, and error location)

## Project Structure
```
src/
  main/
    kotlin/
      com/
        ucle/
          lexer/           # Tokenization components
            Token.kt
            TokenType.kt
            Lexer.kt
            Position.kt
          parser/          # Parsing components
            Parser.kt
            ParseError.kt
          ast/             # Abstract Syntax Tree nodes
            AstNode.kt
            Program.kt
            Statement.kt
            Expression.kt
            Type.kt
            Pattern.kt
          util/            # Utility classes
            ErrorReporter.kt
  test/
    kotlin/
      com/
        ucle/
          lexer/
            LexerTest.kt
          parser/
            ParserTest.kt
```

## Tasks

### 1. Initial Setup (Project Configuration)
**Description:** Configure the Kotlin project with Gradle and set up the basic directory structure.

**Steps:**
- Create a new Kotlin project with Gradle
- Set up the project structure according to the directory layout above
- Configure dependencies (e.g., JUnit for testing)
- Setup build scripts and ensure compilation works

**Expected Output:** A buildable Kotlin project with the appropriate directory structure.

### 2. Define Token and Position Classes
**Description:** Create the classes needed to represent source positions and tokens.

**Steps:**
- Create a `Position` class to track line and column information
- Define a `TokenType` enum that covers all token types in the grammar:
  - Keywords (`type`, `let`, `fn`, `const`, `match`, `case`, `rect`, `in`, etc.)
  - Operators and delimiters (`=>`, `=`, `;`, `,`, `:`, `(`, `)`, `{`, `}`, `<`, `>`, etc.)
  - Primitive types (`string`, `boolean`, `number`)
  - Identifiers
  - Literals (number, string, boolean)
- Create a `Token` class with properties for:
  - Type (`TokenType`)
  - Lexeme (the actual text)
  - Value (typed value for literals)
  - Position (start and end positions)

**Expected Output:** Classes that represent tokens and their positions in the source code.

### 3. Implement the Lexer
**Description:** Create a lexer that converts the input string into a stream of tokens.

**Steps:**
- Implement a `Lexer` class that takes a source string as input
- Add methods to scan different token types (identifiers, keywords, literals, operators)
- Implement whitespace and comment skipping
- Track line and column numbers for error reporting
- Implement a method to get the next token from the input
- Create an `ErrorReporter` utility for lexer errors

**Expected Output:** A lexer that converts an input string into a sequence of tokens with position information.

### 4. Define AST Node Types
**Description:** Create classes to represent all AST nodes according to the grammar.

**Steps:**
- Create a base `AstNode` interface/abstract class with position information
- Define node types for all grammar productions:
  - Program and statements
  - Type declarations and type expressions
  - Expressions (primary expressions, applications, etc.)
  - Patterns and pattern matching
  - Literals and identifiers
- Ensure all node types include appropriate position information for error reporting

**Expected Output:** A complete set of data classes representing all AST node types.

### 5. Implement the Parser
**Description:** Create a recursive descent parser that builds an AST from tokens.

**Steps:**
- Implement a `Parser` class that takes a sequence of tokens
- Create a `ParseError` class for error reporting
- Implement recursive descent parsing methods for each grammar rule:
  - `parseProgram()`
  - `parseStatement()`
  - `parseTypeDecl()`
  - `parseExpression()`
  - `parsePrimaryExpr()`
  - etc.
- Implement helper methods for token consumption and lookahead
- Implement error handling and recovery strategies

**Expected Output:** A parser that converts a sequence of tokens into an AST.

### 6. Implement Error Handling and Reporting
**Description:** Create precise and helpful error messages for parse errors.

**Steps:**
- Enhance the `ParseError` class to include:
  - Expected token types
  - Actual token received
  - Position information
  - Context for the error
- Implement error recovery strategies (e.g., synchronization points)
- Create human-readable error messages with source context

**Expected Output:** Comprehensive error handling that produces helpful error messages with location information.

### 7. Write Test Suite
**Description:** Create a comprehensive test suite for both the lexer and parser.

**Steps:**
- Write unit tests for the lexer to ensure correct tokenization
- Write unit tests for each parser component
- Create integration tests for complete program parsing
- Develop specific tests for error cases and error messages
- Test boundary conditions and edge cases

**Expected Output:** A comprehensive test suite that validates the functionality of the lexer and parser.

### 8. Implement Sample Programs and Test Parsing
**Description:** Create sample programs and validate the parser with them.

**Steps:**
- Implement sample programs covering all language features
- Parse these programs and validate the resulting ASTs
- Create programs with deliberate errors to test error reporting
- Document the sample programs and their expected ASTs

**Expected Output:** A set of sample programs that exercise all features of the grammar and test the parser's capabilities.

### 9. Optimization and Refactoring
**Description:** Optimize the parser for performance and readability.

**Steps:**
- Profile the parser to identify performance bottlenecks
- Refactor code to improve readability and maintainability
- Optimize token handling and AST construction
- Consider implementing a token buffer/lookahead buffer for performance

**Expected Output:** An optimized and refactored parser implementation.

### 10. Documentation
**Description:** Create comprehensive documentation for the parser.

**Steps:**
- Document the API for lexer and parser
- Create a user guide for error messages
- Document the AST structure
- Provide examples of parser usage
- Create diagrams of the parsing process

**Expected Output:** Comprehensive documentation for the parser implementation.

## Implementation Notes

### Lexer Implementation
For the lexer, consider using a state machine approach where the lexer transitions between states based on the input characters. This can make the code more maintainable as the grammar evolves.

### Parser Implementation 
The grammar is LL(1), which means a recursive descent parser with one token of lookahead is sufficient. The parser can be implemented using these general patterns:

```kotlin
fun parseX(): XNode {
    val startPos = currentToken.position
    // Parsing logic specific to X...
    return XNode(..., position = Position(startPos, previousToken.position))
}

fun match(tokenType: TokenType): Token {
    if (currentToken.type == tokenType) {
        val token = currentToken
        advance()
        return token
    }
    throw ParseError("Expected $tokenType, but found ${currentToken.type}", currentToken.position)
}

fun advance() {
    currentToken = nextToken()
}

fun check(tokenType: TokenType): Boolean {
    return currentToken.type == tokenType
}
```

### Error Handling
For error reporting, ensure that each error message contains:
1. What was expected
2. What was actually encountered
3. Where in the source the error occurred
4. Context around the error location

Example error message:
```
Error at line 5, column 10: Expected ')', but found ';'
  let foo(a, b; c) = a + b + c
             ^
```

### AST Construction
When building the AST:
1. Capture source positions in all nodes
2. Make AST nodes immutable (use Kotlin data classes)
3. Include enough information in each node to enable future phases (type checking, code generation)
