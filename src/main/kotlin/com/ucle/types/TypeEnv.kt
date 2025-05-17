package com.ucle.types

import com.ucle.ast.*

/**
 * Represents a type environment that maps identifiers to type schemes.
 * 
 * @property env The map from identifier names to type schemes.
 */
class TypeEnv(val env: Map<String, TypeScheme> = emptyMap()) {
    /**
     * Look up a type scheme by identifier name.
     * 
     * @param name The name to look up.
     * @return The type scheme, or null if not found.
     */
    fun lookup(name: String): Type? {
        val scheme = env[name] ?: return null
        return scheme.type
    }
    
    /**
     * Extend this environment with a mapping from parameter name to type.
     * 
     * @param params The list of parameter names and their types.
     * @return A new extended environment.
     */
    fun extend(params: List<Pair<Param, Type>>): TypeEnv {
        val newEnv = env.toMutableMap()
        params.forEach { (param, type) ->
            newEnv[param.name.lexeme] = TypeScheme(emptyList(), type)
        }
        return TypeEnv(newEnv)
    }
    
    /**
     * Extend this environment with new bindings.
     * 
     * @param bindings The map of new bindings to add.
     * @return A new extended environment.
     */
    fun extend(bindings: Map<String, TypeScheme>): TypeEnv {
        return TypeEnv(env + bindings)
    }
    
    /**
     * Get the free type variables in this environment.
     * 
     * @return The set of free type variables.
     */
    fun freeTypeVars(): Set<Int> {
        return env.values
            .flatMap { 
                val scheme = it
                val typeVars = scheme.type.freeTypeVars()
                typeVars - scheme.variables.toSet()
            }
            .toSet()
    }
    
    /**
     * Generalize a type based on this environment.
     * 
     * @param type The type to generalize.
     * @return A type scheme with appropriate quantified variables.
     */
    fun generalize(type: Type): TypeScheme {
        val freeInEnv = freeTypeVars()
        val freeInType = type.freeTypeVars()
        val quantified = freeInType - freeInEnv
        return TypeScheme(quantified.toList(), type)
    }
    
    override fun toString(): String = env.toString()
}

/**
 * A symbol table that tracks identifiers in different scopes.
 * 
 * @property scopes The stack of scopes, where each scope is a map from names to types.
 */
class SymbolTable {
    private val scopes = mutableListOf<MutableMap<String, TypeScheme>>()
    
    init {
        // Initialize with global scope
        enterScope()
        
        // Add built-in types
        define("Number", TypeScheme(emptyList(), NamedType.NUMBER))
        define("String", TypeScheme(emptyList(), NamedType.STRING))
        define("Boolean", TypeScheme(emptyList(), NamedType.BOOLEAN))
        define("Any", TypeScheme(emptyList(), NamedType.ANY))
        define("Nothing", TypeScheme(emptyList(), NamedType.NOTHING))
    }
    
    /**
     * Enter a new scope.
     */
    fun enterScope() {
        scopes.add(0, mutableMapOf())
    }
    
    /**
     * Exit the current scope.
     */
    fun exitScope() {
        if (scopes.size > 1) {
            scopes.removeAt(0)
        }
    }
    
    /**
     * Define a new symbol in the current scope.
     * 
     * @param name The name of the symbol.
     * @param type The type scheme for the symbol.
     */
    fun define(name: String, type: TypeScheme) {
        scopes[0][name] = type
    }
    
    /**
     * Look up a symbol in all scopes, from innermost to outermost.
     * 
     * @param name The name to look up.
     * @return The type scheme, or null if not found.
     */
    fun lookup(name: String): TypeScheme? {
        for (scope in scopes) {
            scope[name]?.let { return it }
        }
        return null
    }
    
    /**
     * Get a type environment containing all visible symbols.
     * 
     * @return The type environment.
     */
    fun toTypeEnv(): TypeEnv {
        val env = mutableMapOf<String, TypeScheme>()
        // Process scopes from outermost to innermost so inner definitions override outer ones
        scopes.asReversed().forEach { scope ->
            env.putAll(scope)
        }
        return TypeEnv(env)
    }
}

/**
 * A counter for generating fresh type variable IDs.
 */
object FreshVarGen {
    private var nextId = 0
    
    /**
     * Generate a fresh type variable ID.
     * 
     * @return A unique ID.
     */
    fun fresh(): Int = nextId++
    
    /**
     * Reset the counter.
     */
    fun reset() {
        nextId = 0
    }
}
