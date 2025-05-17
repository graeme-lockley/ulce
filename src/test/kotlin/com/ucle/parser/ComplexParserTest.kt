package com.ucle.parser

import com.ucle.ast.*
import com.ucle.Parser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("UCLE Complex Parser Tests")
class ComplexParserTest {

    @Test
    @DisplayName("Simple Type with Record")
    fun simpleTypeWithRecord() {
        val result = Parser.parse("""
            type Person = rect { 
                name: String, 
                age: Number
            };
            
            let createPerson => fn(name, age) => rect { 
                name: name, 
                age: age
            };
            
            let person => createPerson("John", 30);
        """.trimIndent())
        
        // Check that we parsed the expected number of statements
        assertEquals(3, result.statements.size)
        
        // Check first statement is a type declaration
        val typeDecl = result.statements[0] as TypeDecl
        assertEquals(1, typeDecl.declarations.size)
        
        val singleTypeDecl = typeDecl.declarations[0]
        assertEquals("Person", singleTypeDecl.name.token.lexeme)
        
        // Verify that Person is a record type
        val personType = singleTypeDecl.typeExpr
        assertTrue(personType is FunctionType)
        val recordType = (personType as FunctionType).inputType.firstType.firstType as RecordType
        assertEquals("rect", recordType.rectToken.lexeme)
        
        // Check the second statement is a let declaration for createPerson
        val letDecl1 = result.statements[1] as LetDecl
        assertEquals("createPerson", letDecl1.declarations[0].name.lexeme)
        
        // Check the body is a lambda function
        val body1 = letDecl1.declarations[0].body as CompoundExpression
        assertTrue(body1.primary is Lambda)
        
        // Check the third statement is a let declaration for person
        val letDecl2 = result.statements[2] as LetDecl
        assertEquals("person", letDecl2.declarations[0].name.lexeme)
    }
    
    @Test
    @DisplayName("Simple Match Expression")
    fun simpleMatchExpression() {
        val result = Parser.parse("""
            let checkValue => fn(n) =>
              match n {
                case 0 => "zero"
                case 1 => "one"
              };
              
            let result => checkValue(1);
        """.trimIndent())
        
        // Check we have two statements
        assertEquals(2, result.statements.size)
        
        // Check first statement is a let declaration
        val letDecl = result.statements[0] as LetDecl
        assertEquals("checkValue", letDecl.declarations[0].name.lexeme)
        
        // Check the body is a lambda
        val body = letDecl.declarations[0].body as CompoundExpression
        assertTrue(body.primary is Lambda)
        
        // Check the lambda has a match expression
        val lambda = body.primary as Lambda
        val lambdaBody = lambda.body as CompoundExpression
        assertTrue(lambdaBody.primary is MatchExpr)
        
        // Check match expression has two arms
        val matchExpr = lambdaBody.primary as MatchExpr
        assertEquals(1, matchExpr.otherArms.size)  // Plus the first arm
        
        // Check the first arm matches literal 0
        val firstArm = matchExpr.firstArm
        assertTrue(firstArm.pattern is IntegerLiteralPattern)
        assertEquals("0", (firstArm.pattern as IntegerLiteralPattern).token.lexeme)
        
        // Check the second arm matches literal 1
        val secondArm = matchExpr.otherArms[0]
        assertTrue(secondArm.pattern is IntegerLiteralPattern)
        assertEquals("1", (secondArm.pattern as IntegerLiteralPattern).token.lexeme)
    }
    
    @Test
    @DisplayName("Error: Unbalanced Braces")
    fun unbalancedBraces() {
        assertThrows<Exception> {
            Parser.parse("type Person = rect { name: String, age: Number;")
        }
    }
    
    @Test
    @DisplayName("Error: Missing Arrow in Let Declaration")
    fun missingArrowInLetDecl() {
        assertThrows<Exception> {
            Parser.parse("let x 42;")
        }
    }
    
    @Test
    @DisplayName("Function Call with Field Access")
    fun functionCallWithFieldAccess() {
        val result = Parser.parse("""
            let x => getPerson().name;
        """.trimIndent())
        
        // Check we have one statement
        assertEquals(1, result.statements.size)
        
        // Check it's a let declaration
        val letDecl = result.statements[0] as LetDecl
        
        // Check the body is a compound expression with a function call and field access
        val body = letDecl.declarations[0].body as CompoundExpression
        assertEquals(2, body.suffixes.size)
        
        // First suffix is function application
        assertTrue(body.suffixes[0] is ApplicationSuffix)
        
        // Second suffix is field access
        assertTrue(body.suffixes[1] is AccessSuffix)
        assertEquals("name", (body.suffixes[1] as AccessSuffix).fieldName.lexeme)
    }
}
