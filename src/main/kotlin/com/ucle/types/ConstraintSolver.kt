package com.ucle.types

/**
 * Solves type constraints using unification.
 */
class ConstraintSolver {
    /**
     * Solve a set of constraints, producing a substitution.
     * 
     * @param constraints The constraints to solve.
     * @return A substitution that satisfies all constraints.
     * @throws TypeError if the constraints cannot be satisfied.
     */
    fun solve(constraints: Set<Constraint>): Substitution {
        val substitution = mutableMapOf<Int, Type>()
        
        for (constraint in constraints) {
            when (constraint) {
                is Constraint.Equal -> {
                    val s = unify(
                        applySubst(constraint.t1, substitution),
                        applySubst(constraint.t2, substitution)
                    )
                    
                    // Compose the new substitution with the existing one
                    val composedSubst = composeSubst(s, substitution)
                    substitution.clear()
                    substitution.putAll(composedSubst)
                }
                
                is Constraint.Subtype -> {
                    val s = subtype(
                        applySubst(constraint.sub, substitution),
                        applySubst(constraint.sup, substitution)
                    )
                    
                    // Compose the new substitution with the existing one
                    val composedSubst = composeSubst(s, substitution)
                    substitution.clear()
                    substitution.putAll(composedSubst)
                }
            }
        }
        
        return substitution
    }
    
    /**
     * Unify two types, producing a substitution that makes them equal.
     * 
     * @param t1 The first type.
     * @param t2 The second type.
     * @return A substitution that makes t1 and t2 equal.
     * @throws TypeError if the types cannot be unified.
     */
    fun unify(t1: Type, t2: Type): Substitution {
        return when {
            t1 == t2 -> EmptySubst
            
            t1 is TypeVariable -> {
                if (occursCheck(t1.id, t2)) {
                    throw TypeError("Recursive type detected: ${t1.id} occurs in $t2")
                }
                mapOf(t1.id to t2)
            }
            
            t2 is TypeVariable -> unify(t2, t1)
            
            t1 is FunctionType && t2 is FunctionType -> {
                // Check arity match for function types
                if (t1.paramTypes.size != t2.paramTypes.size) {
                    throw TypeError("Function arity mismatch: ${t1.paramTypes.size} vs ${t2.paramTypes.size}")
                }
                
                // Unify return types first
                val s1 = unify(t1.returnType, t2.returnType)
                
                // Then unify parameter types with substitution from return types applied
                val s2 = unifyList(
                    t1.paramTypes.map { applySubst(it, s1) },
                    t2.paramTypes.map { applySubst(it, s1) }
                )
                
                // Compose the substitutions
                composeSubst(s1, s2)
            }
            
            t1 is RecordType && t2 is RecordType -> {
                // For record types, unify fields common to both records
                val subst = mutableMapOf<Int, Type>()
                
                // Find common fields to both records
                val commonFields = t1.fields.keys.intersect(t2.fields.keys)
                
                for (field in commonFields) {
                    val t1Field = t1.fields[field] ?: 
                        throw TypeError("Field $field not found in record $t1")
                    val t2Field = t2.fields[field] ?: 
                        throw TypeError("Field $field not found in record $t2")
                    
                    // Apply current substitution to fields before unifying
                    val fieldSubst = unify(
                        applySubst(t1Field, subst),
                        applySubst(t2Field, subst)
                    )
                    
                    // Compose the new substitution with the existing one
                    val composedSubst = composeSubst(fieldSubst, subst)
                    subst.clear()
                    subst.putAll(composedSubst)
                }
                
                // Handle width subtyping if needed, for now strict matching
                if (t1.fields.keys != t2.fields.keys) {
                    throw TypeError("Record field mismatch: ${t1.fields.keys} vs ${t2.fields.keys}")
                }
                
                subst
            }
            
            t1 is NamedType && t2 is NamedType -> {
                if (t1.name != t2.name) {
                    throw TypeError("Type mismatch: ${t1.name} vs ${t2.name}")
                }
                
                if (t1.typeArgs.size != t2.typeArgs.size) {
                    throw TypeError("Type argument count mismatch: ${t1.typeArgs.size} vs ${t2.typeArgs.size}")
                }
                
                unifyList(t1.typeArgs, t2.typeArgs)
            }
            
            t1 is UnionType && t2 is UnionType -> {
                // For union types, each type in t1 must unify with some type in t2
                // and vice versa. This is a complex unification, for now require exact match.
                if (t1.types.size != t2.types.size) {
                    throw TypeError("Union type size mismatch: ${t1.types.size} vs ${t2.types.size}")
                }
                
                // For simplicity, try to match each type in t1 with the same type in t2
                // This is a limitation and doesn't handle all valid unifications
                unifyList(t1.types.toList(), t2.types.toList())
            }
            
            t1 is IntersectionType && t2 is IntersectionType -> {
                // Similar to union types, for now require exact match
                if (t1.types.size != t2.types.size) {
                    throw TypeError("Intersection type size mismatch: ${t1.types.size} vs ${t2.types.size}")
                }
                
                unifyList(t1.types.toList(), t2.types.toList())
            }
            
            t1 is LiteralType && t2 is LiteralType -> {
                if (t1.value != t2.value || t1.baseType != t2.baseType) {
                    throw TypeError("Literal mismatch: ${t1.value}:${t1.baseType} vs ${t2.value}:${t2.baseType}")
                }
                EmptySubst
            }
            
            t1 is LiteralType && t2 is NamedType -> {
                if (t1.baseType.name != t2.name) {
                    throw TypeError("Type mismatch: ${t1.baseType.name} vs ${t2.name}")
                }
                EmptySubst
            }
            
            t2 is LiteralType && t1 is NamedType -> {
                if (t2.baseType.name != t1.name) {
                    throw TypeError("Type mismatch: ${t1.name} vs ${t2.baseType.name}")
                }
                EmptySubst
            }
            
            else -> throw TypeError("Cannot unify $t1 with $t2")
        }
    }
    
    /**
     * Unify a list of types pairwise.
     * 
     * @param ts1 The first list of types.
     * @param ts2 The second list of types.
     * @return A substitution that unifies all pairs of types.
     * @throws TypeError if any pair cannot be unified.
     */
    private fun unifyList(ts1: List<Type>, ts2: List<Type>): Substitution {
        if (ts1.size != ts2.size) {
            throw TypeError("List size mismatch: ${ts1.size} vs ${ts2.size}")
        }
        
        var subst = EmptySubst
        for (i in ts1.indices) {
            val s = unify(applySubst(ts1[i], subst), applySubst(ts2[i], subst))
            subst = composeSubst(s, subst)
        }
        
        return subst
    }
    
    /**
     * Check if a subtype relationship is satisfiable, producing a substitution.
     * 
     * @param sub The subtype.
     * @param sup The supertype.
     * @return A substitution that makes sub a subtype of sup.
     * @throws TypeError if the subtyping relationship cannot be satisfied.
     */
    fun subtype(sub: Type, sup: Type): Substitution {
        return when {
            sub == sup -> EmptySubst
            
            sup is TypeVariable -> {
                if (occursCheck(sup.id, sub)) {
                    throw TypeError("Recursive type detected: ${sup.id} occurs in $sub")
                }
                mapOf(sup.id to sub)
            }
            
            sub is TypeVariable -> {
                if (occursCheck(sub.id, sup)) {
                    throw TypeError("Recursive type detected: ${sub.id} occurs in $sup")
                }
                mapOf(sub.id to sup)
            }
            
            sub is FunctionType && sup is FunctionType -> {
                // For function subtypes: (T1->T2) <: (S1->S2) if S1 <: T1 and T2 <: S2
                // (contravariant in argument, covariant in result)
                if (sub.paramTypes.size != sup.paramTypes.size) {
                    throw TypeError("Function arity mismatch: ${sub.paramTypes.size} vs ${sup.paramTypes.size}")
                }
                
                // First check return type (covariant)
                val s1 = subtype(sub.returnType, sup.returnType)
                
                // Then check parameter types (contravariant)
                val s2List = mutableListOf<Substitution>()
                for (i in sub.paramTypes.indices) {
                    val paramSubst = subtype(
                        applySubst(sup.paramTypes[i], s1),
                        applySubst(sub.paramTypes[i], s1)
                    )
                    s2List.add(paramSubst)
                }
                
                // Compose all substitutions
                var composedSubst = s1
                for (s in s2List) {
                    composedSubst = composeSubst(s, composedSubst)
                }
                
                composedSubst
            }
            
            sub is RecordType && sup is RecordType -> {
                // Record subtyping: use unification for field types
                val subst = mutableMapOf<Int, Type>()
                
                // All fields in the supertype must be in the subtype
                for ((field, supType) in sup.fields) {
                    val subType = sub.fields[field] ?:
                        throw TypeError("Field $field not found in record $sub")
                    
                    // Use unification instead of subtyping for field types
                    // This ensures proper bidirectional type flow
                    val fieldSubst = unify(
                        applySubst(subType, subst),
                        applySubst(supType, subst)
                    )
                    
                    // Compose substitutions
                    val composedSubst = composeSubst(fieldSubst, subst)
                    subst.clear()
                    subst.putAll(composedSubst)
                }
                
                subst
            }
            
            sub is NamedType && sup is NamedType -> {
                if (sub.name != sup.name) {
                    throw TypeError("Type mismatch: ${sub.name} vs ${sup.name}")
                }
                
                if (sub.typeArgs.size != sup.typeArgs.size) {
                    throw TypeError("Type argument count mismatch: ${sub.typeArgs.size} vs ${sup.typeArgs.size}")
                }
                
                // For named types, type arguments must be invariant for now
                unifyList(sub.typeArgs, sup.typeArgs)
            }
            
            sub is UnionType -> {
                // T1|T2 <: S if T1 <: S and T2 <: S
                var subst = EmptySubst
                for (t in sub.types) {
                    val s = subtype(applySubst(t, subst), applySubst(sup, subst))
                    subst = composeSubst(s, subst)
                }
                subst
            }
            
            sup is UnionType -> {
                // T <: S1|S2 if T <: S1 or T <: S2
                for (s in sup.types) {
                    try {
                        return subtype(sub, s)
                    } catch (e: TypeError) {
                        // Try the next type in the union
                    }
                }
                throw TypeError("$sub is not a subtype of any type in union $sup")
            }
            
            sub is IntersectionType -> {
                // T1&T2 <: S if T1 <: S or T2 <: S
                for (t in sub.types) {
                    try {
                        return subtype(t, sup)
                    } catch (e: TypeError) {
                        // Try the next type in the intersection
                    }
                }
                throw TypeError("No type in intersection $sub is a subtype of $sup")
            }
            
            sup is IntersectionType -> {
                // T <: S1&S2 if T <: S1 and T <: S2
                var subst = EmptySubst
                for (s in sup.types) {
                    val newSubst = subtype(applySubst(sub, subst), applySubst(s, subst))
                    subst = composeSubst(newSubst, subst)
                }
                subst
            }
            
            sub is LiteralType && sup is NamedType -> {
                if (sub.baseType.name != sup.name) {
                    throw TypeError("Type mismatch: ${sub.baseType.name} vs ${sup.name}")
                }
                EmptySubst
            }
            
            else -> throw TypeError("$sub is not a subtype of $sup")
        }
    }
}
