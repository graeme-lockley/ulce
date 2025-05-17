package com.ucle.types

import com.ucle.Parser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.RuntimeException

/**
 * Tests for the type inference system.
 */
class TypeInferenceTest {
    
    @Test
    fun `test identity function type inference`() {
        val source = """
            let identity => fn(x) => x;
        """.trimIndent()

        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)

        assertNotNull(types["identity"])
        val identityType = types["identity"]!!.replace("\\s+".toRegex(), " ")
        assert(identityType.matches(Regex("T(\\d+) -> T\\1"))) { "Expected a type of the form Tn -> Tn, got: $identityType" }
    }

    @Test
    fun `test compose function type inference`() {
        val source = """
            let compose => fn(f) => fn(g) => fn(x) => f(g(x));
        """.trimIndent()

        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)

        assertNotNull(types["compose"])
        assertEquals("(T5 -> T6) -> (T4 -> T5) -> T4 -> T6", types["compose"]?.replace("\\s+".toRegex(), " "))
    }

    @Test
    fun `test pair function type inference`() {
        val source = """
            let pair => fn(a, b) => rect { first: a, second: b };
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        assertNotNull(types["pair"])
        val pairType = types["pair"]!!.replace("\\s+".toRegex(), " ")

        assert(pairType.matches(Regex("\\(T(\\d+), T(\\d+)\\) -> rect \\{ first: T\\1, second: T\\2 \\}"))) {
            "Expected a type of the form (Tn, Tm) -> rect { first: Tn, second: Tm }, got: $pairType"
        }
    }
    
    @Test
    fun `test getFst function type inference`() {
        val source = """
            let getFst => fn(p) => p.first;
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        assertNotNull(types["getFst"])
        val getFstType = types["getFst"]!!.replace("\\s+".toRegex(), " ")
        assert(getFstType.matches(Regex("rect \\{ first: T(\\d+) \\| T(\\d+) \\} -> T\\1"))) {
            "Expected a type of the form rect { first: Tn | rowVar } -> Tn, got: $getFstType"
        }
    }
    
    @Test
    fun `test complete example`() {
        val source = """
            let identity => fn(x) => x;
            let pair => fn(a, b) => rect { first: a, second: b };
            let getFst => fn(p) => p.first;

            let result => getFst(pair(identity(5), "hello"));
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        assertNotNull(types["identity"])
        assertNotNull(types["pair"])
        assertNotNull(types["getFst"])
        assertNotNull(types["result"])
        
        val resultType = types["result"]?.replace("\\s+".toRegex(), " ")
        val isNumber = resultType == "Number"
        val isLiteralNumber = resultType?.matches(Regex("LiteralType\\(.*Number\\)")) == true
        if (!isNumber && !isLiteralNumber) {
            error("Expected result type to be Number or a literal number type, but was: $resultType")
        }
    }
    
    @Test
    fun `test lambda with record pattern`() {
        val source = """
            let getField => fn(record) => 
                match record {
                    case rect { name: n, age: a } => n
                };
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        assertNotNull(types["getField"])
        val getFieldType = types["getField"]!!.replace("\\s+".toRegex(), " ")
        assertEquals("rect { name: T4, age: T5 } -> T4", getFieldType)
    }
    
    @Test
    fun `test polymorphic function`() {
        val source = """
            let map => fn(f, list) => 
                match list {
                    case rect { head: h, tail: t } => 
                        rect { head: f(h), tail: map(f, t) }
                    case empty => empty
                };
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        assertNotNull(types["map"])
        val mapType = types["map"]!!.replace("\\s+".toRegex(), " ")
        assertEquals("(T5 -> T5, rect { head: T5, tail: T6 }) -> rect { head: T5, tail: T6 }", mapType)
    }
}
