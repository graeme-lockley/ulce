package com.ucle.types

/**
 * Represents a type in the UCLE type system.
 */
sealed class Type {
    /**
     * Get the free type variables in this type.
     */
    abstract fun freeTypeVars(): Set<Int>
    
    /**
     * Apply a substitution to this type.
     */
    abstract fun applySubst(subst: Map<Int, Type>): Type
    
    /**
     * Create a pretty-printed string representation of this type.
     */
    abstract fun pretty(): String
    
    /**
     * Check whether this type occurs in another type.
     * Used for the occurs check in unification.
     */
    abstract fun occurs(typeVar: Int): Boolean
}

/**
 * Represents a type variable.
 * 
 * @property id The unique identifier for this type variable.
 */
data class TypeVariable(val id: Int) : Type() {
    override fun freeTypeVars(): Set<Int> = setOf(id)
    
    override fun applySubst(subst: Map<Int, Type>): Type = 
        when (val t = subst[id]) {
            null -> this
            is TypeVariable -> t.applySubst(subst)
            else -> t
        }
    
    override fun pretty(): String = "T$id"
    
    override fun occurs(typeVar: Int): Boolean = id == typeVar
    
    override fun toString(): String = "TypeVar($id)"
}

/**
 * Represents a function type.
 * 
 * @property paramTypes The types of the function parameters.
 * @property returnType The return type of the function.
 */
data class FunctionType(val paramTypes: List<Type>, val returnType: Type) : Type() {
    override fun freeTypeVars(): Set<Int> =
        paramTypes.flatMap { it.freeTypeVars() }.toSet() + returnType.freeTypeVars()
    
    override fun applySubst(subst: Map<Int, Type>): Type =
        FunctionType(
            paramTypes.map { it.applySubst(subst) },
            returnType.applySubst(subst)
        )
    
    override fun pretty(): String {
        val paramsStr = if (paramTypes.size == 1) {
            val paramType = paramTypes[0]
            if (paramType is FunctionType) {
                "(${paramType.pretty()})"
            } else {
                paramType.pretty()
            }
        } else {
            "(${paramTypes.joinToString(", ") { it.pretty() }})"
        }
        val returnStr = returnType.pretty()
        return "$paramsStr -> $returnStr"
    }
    
    override fun occurs(typeVar: Int): Boolean =
        paramTypes.any { it.occurs(typeVar) } || returnType.occurs(typeVar)
    
    override fun toString(): String =
        "(${paramTypes.joinToString(", ")}) -> $returnType"
}

/**
 * Represents a record type.
 * 
 * @property fields A map of field names to their types.
 */
data class RecordType(val fields: Map<String, Type>, val rowVar: TypeVariable? = null) : Type() {
    override fun freeTypeVars(): Set<Int> =
        fields.values.flatMap { it.freeTypeVars() }.toSet() + (rowVar?.let { setOf(it.id) } ?: emptySet())
    
    override fun applySubst(subst: Map<Int, Type>): Type =
        RecordType(fields.mapValues { it.value.applySubst(subst) }, rowVar?.applySubst(subst) as? TypeVariable)
    
    override fun pretty(): String =
        "rect { ${fields.entries.joinToString(", ") { "${it.key}: ${it.value.pretty()}" }}${rowVar?.let { " | ${it.pretty()}" } ?: ""} }"
    
    override fun occurs(typeVar: Int): Boolean =
        fields.values.any { it.occurs(typeVar) } || (rowVar?.occurs(typeVar) ?: false)
    
    override fun toString(): String =
        "RecordType{${fields.entries.joinToString(", ")}}${rowVar?.let { " | ${it}" } ?: ""}"
}

/**
 * Represents a named type (like String, Number, or a user-defined type).
 * 
 * @property name The name of the type.
 * @property typeArgs The type arguments for generic types.
 */
data class NamedType(val name: String, val typeArgs: List<Type> = emptyList()) : Type() {
    override fun freeTypeVars(): Set<Int> =
        typeArgs.flatMap { it.freeTypeVars() }.toSet()
    
    override fun applySubst(subst: Map<Int, Type>): Type =
        if (typeArgs.isEmpty()) {
            this
        } else {
            NamedType(name, typeArgs.map { it.applySubst(subst) })
        }
    
    override fun pretty(): String =
        if (typeArgs.isEmpty()) {
            name
        } else {
            "$name<${typeArgs.joinToString(", ") { it.pretty() }}>"
        }
    
    override fun occurs(typeVar: Int): Boolean =
        typeArgs.any { it.occurs(typeVar) }
    
    override fun toString(): String =
        if (typeArgs.isEmpty()) name else "$name<${typeArgs.joinToString(", ")}>"
    
    companion object {
        // Built-in types
        val NUMBER = NamedType("Number")
        val STRING = NamedType("String")
        val BOOLEAN = NamedType("Boolean")
        val ANY = NamedType("Any")
        val NOTHING = NamedType("Nothing")
    }
}

/**
 * Represents a union type (T1 | T2).
 * 
 * @property types The set of types in the union.
 */
data class UnionType(val types: Set<Type>) : Type() {
    override fun freeTypeVars(): Set<Int> =
        types.flatMap { it.freeTypeVars() }.toSet()
    
    override fun applySubst(subst: Map<Int, Type>): Type =
        UnionType(types.map { it.applySubst(subst) }.toSet())
    
    override fun pretty(): String =
        types.joinToString(" | ") { it.pretty() }
    
    override fun occurs(typeVar: Int): Boolean =
        types.any { it.occurs(typeVar) }
    
    override fun toString(): String =
        "UnionType(${types.joinToString(" | ")})"
}

/**
 * Represents an intersection type (T1 & T2).
 * 
 * @property types The set of types in the intersection.
 */
data class IntersectionType(val types: Set<Type>) : Type() {
    override fun freeTypeVars(): Set<Int> =
        types.flatMap { it.freeTypeVars() }.toSet()
    
    override fun applySubst(subst: Map<Int, Type>): Type =
        IntersectionType(types.map { it.applySubst(subst) }.toSet())
    
    override fun pretty(): String =
        types.joinToString(" & ") { it.pretty() }
    
    override fun occurs(typeVar: Int): Boolean =
        types.any { it.occurs(typeVar) }
    
    override fun toString(): String =
        "IntersectionType(${types.joinToString(" & ")})"
}

/**
 * Represents a literal type, like the string literal "hello".
 * 
 * @property value The literal value.
 * @property baseType The base type of the literal.
 */
data class LiteralType(val value: Any, val baseType: NamedType) : Type() {
    override fun freeTypeVars(): Set<Int> = emptySet()
    
    override fun applySubst(subst: Map<Int, Type>): Type = this
    
    override fun pretty(): String = value.toString()
    
    override fun occurs(typeVar: Int): Boolean = false
    
    override fun toString(): String = "LiteralType($value: $baseType)"
}

/**
 * Represents a type scheme with universally quantified type variables.
 * 
 * @property variables The quantified type variables.
 * @property type The underlying type.
 */
data class TypeScheme(val variables: List<Int>, val type: Type) {
    /**
     * Instantiate this type scheme by replacing quantified variables with fresh ones.
     * 
     * @param freshVarGen A function that generates fresh type variable IDs.
     * @return The instantiated type.
     */
    fun instantiate(freshVarGen: () -> Int): Type {
        val subst = variables.associateWith { TypeVariable(freshVarGen()) }
        return type.applySubst(subst)
    }
    
    override fun toString(): String =
        if (variables.isEmpty()) {
            type.toString()
        } else {
            "∀${variables.joinToString(", ") { "T$it" }}. $type"
        }
}

/**
 * Represents a type error in the program.
 */
data class TypeError(override val message: String) : Exception(message)
