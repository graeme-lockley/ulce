package com.ucle.types

/**
 * Represents a type constraint.
 */
sealed class Constraint {
    /**
     * A constraint that two types must be equal.
     * 
     * @property t1 The first type.
     * @property t2 The second type.
     */
    data class Equal(val t1: Type, val t2: Type) : Constraint() {
        override fun toString() = "$t1 = $t2"
    }
    
    /**
     * A constraint that one type must be a subtype of another.
     * 
     * @property sub The subtype.
     * @property sup The supertype.
     */
    data class Subtype(val sub: Type, val sup: Type) : Constraint() {
        override fun toString() = "$sub <: $sup"
    }
}

/**
 * A collection of constraints with utility methods to add constraints.
 */
class Constraints {
    private val constraints = mutableSetOf<Constraint>()
    
    /**
     * Add an equality constraint.
     * 
     * @param t1 The first type.
     * @param t2 The second type.
     */
    fun addEquality(t1: Type, t2: Type) {
        constraints.add(Constraint.Equal(t1, t2))
    }
    
    /**
     * Add a subtyping constraint.
     * 
     * @param sub The subtype.
     * @param sup The supertype.
     */
    fun addSubtype(sub: Type, sup: Type) {
        constraints.add(Constraint.Subtype(sub, sup))
    }
    
    /**
     * Get the set of all constraints.
     * 
     * @return The constraints.
     */
    fun getAll(): Set<Constraint> = constraints.toSet()
    
    /**
     * Combine with another constraints collection.
     * 
     * @param other The other constraints.
     */
    fun addAll(other: Constraints) {
        constraints.addAll(other.constraints)
    }
    
    /**
     * Check if this collection is empty.
     * 
     * @return True if there are no constraints.
     */
    fun isEmpty() = constraints.isEmpty()
}

/**
 * A substitution from type variables to types.
 */
typealias Substitution = Map<Int, Type>

/**
 * The empty substitution.
 */
val EmptySubst: Substitution = emptyMap()

/**
 * Apply a substitution to a type.
 * 
 * @param type The type to apply the substitution to.
 * @param subst The substitution to apply.
 * @return The resulting type.
 */
fun applySubst(type: Type, subst: Substitution): Type = type.applySubst(subst)

/**
 * Compose two substitutions.
 * 
 * @param s1 The first substitution.
 * @param s2 The second substitution.
 * @return The composed substitution.
 */
fun composeSubst(s1: Substitution, s2: Substitution): Substitution {
    val result = mutableMapOf<Int, Type>()
    
    // Apply s1 to the range of s2
    s2.forEach { (id, type) ->
        result[id] = applySubst(type, s1)
    }
    
    // Add mappings from s1 that don't conflict with s2
    s1.forEach { (id, type) ->
        if (id !in s2) {
            result[id] = type
        }
    }
    
    return result
}

/**
 * Check if a type variable occurs in a type.
 * 
 * @param typeVar The type variable ID.
 * @param type The type to check.
 * @return True if the variable occurs in the type.
 */
fun occursCheck(typeVar: Int, type: Type): Boolean = type.occurs(typeVar)
