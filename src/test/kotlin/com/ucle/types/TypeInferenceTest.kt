package com.ucle.types

import com.ucle.Parser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

        println("Inferred types: $types")
        assertNotNull(types["identity"])
        assertEquals("T0 -> T0", types["identity"]?.replace("\\s+".toRegex(), " "))
    }

    @Test
    fun `test compose function type inference`() {
        val source = """
            let compose => fn(f) => fn(g) => fn(x) => f(g(x));
        """.trimIndent()

        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)

        println("Inferred types: $types")
        assertNotNull(types["compose"])
        assertEquals("T0 -> T0", types["compose"]?.replace("\\s+".toRegex(), " "))
    }

    @Test
    fun `test pair function type inference`() {
        val source = """
            let pair => fn(a, b) => rect { first: a, second: b };
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        println("Inferred types: $types")
        assertNotNull(types["pair"])
        // Should be something like: (T0, T1) -> rect { first: T0, second: T1 }
    }
    
    @Test
    fun `test getFst function type inference`() {
        val source = """
            let getFst => fn(p) => p.first;
        """.trimIndent()
        
        val program = Parser.parse(source)
        val typeChecker = TypeChecker()
        val types = typeChecker.getInferredTypes(program)
        
        println("Inferred types: $types")
        assertNotNull(types["getFst"])
        // Should be something like: rect { first: T0, ... } -> T0
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
        
        println("Inferred types: $types")
        assertNotNull(types["identity"])
        assertNotNull(types["pair"])
        assertNotNull(types["getFst"])
        assertNotNull(types["result"])
        
        println("Result type: ${types["result"]}")
        // Check why we're getting the wrong type
        assertEquals("Number", types["result"])
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
        
        println("Inferred types: $types")
        assertNotNull(types["getField"])
        // Should be something like: rect { name: T0, age: T1 } -> T0
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
        
        println("Inferred types: $types")
        assertNotNull(types["map"])
        // Should infer a polymorphic type for the map function
    }
}
