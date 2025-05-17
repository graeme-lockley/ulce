package com.ucle.types

import com.ucle.ast.Program
import com.ucle.ast.LetDecl

/**
 * The main entry point for the type checker.
 */
class TypeChecker {
    private val typeInference = TypeInference()
    
    /**
     * Type-check a program AST.
     * 
     * @param program The program AST.
     * @return A typed version of the program with inferred types.
     * @throws TypeError if the program contains type errors.
     */
    fun checkProgram(program: Program): TypedProgram {
        // Reset the type variable counter
        FreshVarGen.reset()
        
        // Infer types for the program
        val (_, result) = typeInference.inferProgram(program)
        
        // Create a typed version of the AST
        return result
    }
    
    /**
     * Get the inferred type of an expression as a string.
     * 
     * @param program The program AST.
     * @return A map of pretty-printed inferred types for expressions.
     */
    fun getInferredTypes(program: Program): Map<String, String> {
        // Reset the type variable counter
        FreshVarGen.reset()
        
        // Infer types for the program
        val (typeEnv, _) = typeInference.inferProgram(program)
        
        // Collect all variable names and their types from the generalized environment
        val result = mutableMapOf<String, String>()
        for ((name, scheme) in typeEnv.env) {
            result[name] = scheme.type.pretty()
        }
        return result
    }
}
