package com.ucle

import com.ucle.ast.AstBuilder
import com.ucle.ast.Program
import com.ucle.parser.Parser
import com.ucle.parser.Scanner
import com.ucle.types.TypeChecker
import com.ucle.types.TypedProgram
import java.io.StringReader

/**
 * Utility class for parsing and type checking UCLE code.
 */
object Parser {
    /**
     * Parse a UCLE program from a scanner.
     * 
     * @param scanner The scanner to read tokens from.
     * @return The parsed program AST.
     */
    fun parse(scanner: Scanner): Program {
        return Parser(scanner, AstBuilder()).program()
    }
    
    /**
     * Parse a UCLE program from a string.
     * 
     * @param input The source code string.
     * @return The parsed program AST.
     */
    fun parse(input: String): Program =
        parse(Scanner(StringReader(input)))
    
    /**
     * Parse and type-check a UCLE program from a string.
     * 
     * @param input The source code string.
     * @return The parsed and type-checked program.
     */
    fun parseAndTypeCheck(input: String): TypedProgram {
        val program = parse(input)
        val typeChecker = TypeChecker()
        return typeChecker.checkProgram(program)
    }
    
    /**
     * Parse a program and get the inferred types for top-level declarations.
     * 
     * @param input The source code string.
     * @return A map of declaration names to their inferred type strings.
     */
    fun getInferredTypes(input: String): Map<String, String> {
        val program = parse(input)
        val typeChecker = TypeChecker()
        return typeChecker.getInferredTypes(program)
    }
}
