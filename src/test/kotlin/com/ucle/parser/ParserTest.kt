package com.ucle.parser

import com.ucle.ast.*
import com.ucle.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("UCLE Simple Parser Tests")
class ParserTest {

    @Test
    @DisplayName("Simple Expression Statement")
    fun simpleExpressionStatement() {
        val result = Parser.parse("x;")
        
        // Check that we have one statement
        assertEquals(1, result.statements.size)
        
        // Check that it's an expression statement
        val stmt = result.statements[0]
        assertTrue(stmt is ExprStmt)
        
        // Check the expression is a variable reference
        val exprStmt = stmt as ExprStmt
        val expression = exprStmt.expression
        assertTrue(expression is CompoundExpression)
        
        val compoundExpr = expression as CompoundExpression
        assertTrue(compoundExpr.primary is LowerIdentifierExpr)
        assertEquals("x", (compoundExpr.primary as LowerIdentifierExpr).token.lexeme)
    }

    @Test
    @DisplayName("Simple Type Declaration")
    fun simpleTypeDeclaration() {
        val result = Parser.parse("type Person = rect { name: String };")
        
        // Check that we have one statement
        assertEquals(1, result.statements.size)
        
        // Check that it's a type declaration
        val stmt = result.statements[0]
        assertTrue(stmt is TypeDecl)
        
        val typeDecl = stmt as TypeDecl
        assertEquals("type", typeDecl.typeToken.lexeme)
        assertEquals(1, typeDecl.declarations.size)
    }
    
    @Test
    @DisplayName("Simple Let Declaration")
    fun simpleLetDeclaration() {
        val result = Parser.parse("let x => 42;")
        
        // Check that we have one statement
        assertEquals(1, result.statements.size)
        
        // Check that it's a let declaration
        val stmt = result.statements[0]
        assertTrue(stmt is LetDecl)
        
        val letDecl = stmt as LetDecl
        assertEquals("let", letDecl.letToken.lexeme)
        assertEquals(1, letDecl.declarations.size)
    }

    @Test
    @DisplayName("Simple Pattern Matching")
    fun simplePatternMatching() {
        val result = Parser.parse("match value { case x => x };")
        
        // Check that we have one statement
        assertEquals(1, result.statements.size)
        
        // Check the match expression structure
        val stmt = result.statements[0] as ExprStmt
        val expr = stmt.expression as CompoundExpression
        assertTrue(expr.primary is MatchExpr)
    }

    @Test
    @DisplayName("Missing Semicolon Error")
    fun missingSemicolon() {
        assertThrows<Exception> {
            Parser.parse("let x => 10")
        }
    }

    @Test
    @DisplayName("Simple Multiple Statements")
    fun simpleMultipleStatements() {
        val program = """
            type Person = rect { name: String };
            let x => 42;
        """.trimIndent()
        
        // Just check that it parses without exceptions
        val result = Parser.parse(program)
        
        // Check basic structure is correct
        assertEquals(2, result.statements.size)
    }
}
