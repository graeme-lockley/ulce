package com.ucle.types

import com.ucle.ast.*
import io.littlelanguages.scanpiler.Location

/**
 * Collects type constraints from an AST node.
 */
class ConstraintGenerator {
    private val typeVarIdGenerator = FreshVarGen
    
    /**
     * Generate a fresh type variable.
     */
    private fun freshTypeVar(): TypeVariable = TypeVariable(typeVarIdGenerator.fresh())

    /**
     * Generate constraints for an AST node.
     * 
     * @param node The AST node.
     * @param typeEnv The current type environment.
     * @return A triple of (inferred type, constraints, type variable bindings).
     */
    fun generateConstraints(node: AstNode, typeEnv: TypeEnv): Triple<Type, Constraints, Map<AstNode, Type>> {
        val constraints = Constraints()
        val bindings = mutableMapOf<AstNode, Type>()
        
        val type = when (node) {
            is Program -> TODO("Internal Error: $node")
            
            is ExprStmt -> {
                val (exprType, exprConstraints, exprBindings) = 
                    generateConstraints(node.expression, typeEnv)
                constraints.addAll(exprConstraints)
                bindings.putAll(exprBindings)
                bindings[node] = exprType
                exprType
            }
            
            is TypeDecl -> {
                // Process each type declaration
                for (decl in node.declarations) {
                    val (_, declConstraints, declBindings) =
                        generateConstraints(decl, typeEnv)
                    constraints.addAll(declConstraints)
                    bindings.putAll(declBindings)
                }
                bindings[node] = NamedType.NOTHING
                NamedType.NOTHING
            }
            
            is SingleTypeDecl -> {
                val typeName = node.name.token.lexeme
                
                // Handle generic parameters if present
//                val typeParams = if (node.genericParams != null) {
//                    node.genericParams.typeVars.map { it.token.lexeme }
//                } else {
//                    emptyList()
//                }
                
                // Process the type expression
                val (_, typeExprConstraints, typeExprBindings) =
                    generateConstraints(node.typeExpr, typeEnv)
                constraints.addAll(typeExprConstraints)
                bindings.putAll(typeExprBindings)
                
                // Create a named type for this declaration
                val namedType = NamedType(typeName)
                bindings[node] = namedType
                
                namedType
            }
            
            is LetDecl -> {
                val allBindings = mutableMapOf<AstNode, Type>()
                val allConstraints = Constraints()
                var currentEnv = typeEnv
                var lastType: Type = NamedType.NOTHING

                for (decl in node.declarations) {
                    // Generate constraints for this declaration
                    val (declType, declConstraints, declBindings) = generateConstraints(decl, currentEnv)
                    
                    // Add this declaration's constraints to the running set
                    allConstraints.addAll(declConstraints)
                    allBindings.putAll(declBindings)
                    
                    // Solve constraints up to this point
                    val solver = ConstraintSolver()
                    val subst = solver.solve(allConstraints.getAll())
                    
                    // Apply substitution to this declaration's type
                    val resolvedType = applySubst(declType, subst)
                    
                    // Generalize using the current environment
                    val scheme = currentEnv.generalize(resolvedType)
                    
                    // Extend environment with generalized type for subsequent declarations
                    currentEnv = currentEnv.extend(mapOf(decl.name.lexeme to scheme))
                    
                    // Store the resolved type for this declaration
                    allBindings[decl] = resolvedType
                    lastType = resolvedType
                }

                allBindings[node] = lastType
                return Triple(lastType, allConstraints, allBindings)
            }
            
            is SingleLetDecl -> {
                val name = node.name.lexeme
                
                // Create a type variable for this declaration
                val declType = freshTypeVar()
                bindings[node] = declType
                
                // Process the body expression
                val (bodyType, bodyConstraints, bodyBindings) = 
                    generateConstraints(node.body, typeEnv)
                constraints.addAll(bodyConstraints)
                bindings.putAll(bodyBindings)
                
                val lambdaType = when (val body = node.body) {
                    is CompoundExpression -> {
                        val primary = body.primary
                        if (primary is Lambda) {
                            bindings[primary] ?: bodyType
                        } else {
                            bodyType
                        }
                    }
                }
                
                constraints.addEquality(declType, lambdaType)
                
                declType
            }
            
            is CompoundExpression -> {
                // Process the primary expression
                val (primaryType, primaryConstraints, primaryBindings) = 
                    generateConstraints(node.primary, typeEnv)
                constraints.addAll(primaryConstraints)
                bindings.putAll(primaryBindings)
                
                // Process all suffixes, threading through the type
                var currentType = primaryType
                for (suffix in node.suffixes) {
                    val (suffixType, suffixConstraints, suffixBindings) = 
                        generateSuffixConstraints(suffix, currentType, typeEnv)
                    constraints.addAll(suffixConstraints)
                    bindings.putAll(suffixBindings)
                    currentType = suffixType
                }
                
                bindings[node] = currentType
                currentType
            }
            
            is LowerIdentifierExpr -> {
                val name = node.token.lexeme
                val varType = typeEnv.lookup(name) ?:
                    throw TypeError("Undefined variable: $name at ${node.position}")
                bindings[node] = varType
                varType
            }
            
            is UpperIdentifierExpr -> {
                val name = node.token.lexeme
                val varType = typeEnv.lookup(name) ?:
                    throw TypeError("Undefined type or constructor: $name at ${node.position}")
                bindings[node] = varType
                varType
            }
            
            is StringLiteralExpr -> {
                val stringType = NamedType.STRING
                bindings[node] = stringType
                stringType
            }
            
            is IntegerLiteralExpr -> {
                val numberType = NamedType.NUMBER
                bindings[node] = numberType
                numberType
            }
            
            is TrueLiteralExpr, is FalseLiteralExpr -> {
                val booleanType = NamedType.BOOLEAN
                bindings[node] = booleanType
                booleanType
            }
            
            is Lambda -> {
                // Create type variables for parameters
                val paramTypes = mutableListOf<Type>()
                val paramToType = mutableMapOf<Param, Type>()
                val paramNameToParam = mutableMapOf<String, Param>()
                
                if (node.params != null) {
                    val allParams = listOf(node.params.firstParam) + 
                                    node.params.otherParams.map { it.second }
                    
                    for (param in allParams) {
                        val paramType = if (param.typeAnnotation != null) {
                            val (annotationType, annotationConstraints, annotationBindings) = 
                                generateConstraints(param.typeAnnotation.typeExpr, typeEnv)
                            constraints.addAll(annotationConstraints)
                            bindings.putAll(annotationBindings)
                            annotationType
                        } else {
                            freshTypeVar()
                        }
                        paramTypes.add(paramType)
                        paramToType[param] = paramType
                        paramNameToParam[param.name.lexeme] = param
                        bindings[param] = paramType  // Bind the type to the param node
                    }
                }
                
                // Create new environment with parameter bindings
                val extendedEnv = typeEnv.extend(paramToType.toList())
                
                // Process the body expression
                val (bodyType, bodyConstraints, bodyBindings) = 
                    generateConstraints(node.body, extendedEnv)
                constraints.addAll(bodyConstraints)
                bindings.putAll(bodyBindings)
                
                // Create the function type
                val functionType = FunctionType(paramTypes, bodyType)
                bindings[node] = functionType
                
                // If the body is another lambda, we don't need to add any additional constraints
                // The type variables will be properly unified through the constraint solving process
                
                functionType
            }
            
            is RecordLiteral -> {
                val fields = mutableMapOf<String, Type>()
                
                if (node.fields != null) {
                    val allFields = listOf(node.fields.firstField) + 
                                   node.fields.otherFields.map { it.second }
                    
                    for (field in allFields) {
                        val name = field.name.lexeme
                        
                        val (fieldType, fieldConstraints, fieldBindings) = 
                            generateConstraints(field.value, typeEnv)
                        constraints.addAll(fieldConstraints)
                        bindings.putAll(fieldBindings)
                        
                        fields[name] = fieldType
                    }
                }
                
                val recordType = RecordType(fields)
                bindings[node] = recordType
                recordType
            }
            
            is ConstExpr -> {
                val name = node.name.lexeme
                
                // Process the value expression
                val (valueType, valueConstraints, valueBindings) = 
                    generateConstraints(node.value, typeEnv)
                constraints.addAll(valueConstraints)
                bindings.putAll(valueBindings)
                
                // Create new environment with the constant binding
                val extendedEnv = typeEnv.extend(
                    mapOf(name to TypeScheme(emptyList(), valueType))
                )
                
                // Process the body expression
                val (bodyType, bodyConstraints, bodyBindings) = 
                    generateConstraints(node.body, extendedEnv)
                constraints.addAll(bodyConstraints)
                bindings.putAll(bodyBindings)
                
                bindings[node] = bodyType
                bodyType
            }
            
            is MatchExpr -> {
                // Process the scrutinee expression
                val (scrutineeType, scrutineeConstraints, scrutineeBindings) = 
                    generateConstraints(node.scrutinee, typeEnv)
                constraints.addAll(scrutineeConstraints)
                bindings.putAll(scrutineeBindings)
                
                // Process all match arms
                val resultType = freshTypeVar()
                val allArms = listOf(node.firstArm) + node.otherArms
                
                for (arm in allArms) {
                    val (patternType, patternConstraints, patternBindings, patternEnv) = 
                        generatePatternConstraints(arm.pattern, typeEnv)
                    constraints.addAll(patternConstraints)
                    bindings.putAll(patternBindings)
                    
                    // Ensure pattern matches scrutinee
                    constraints.addEquality(scrutineeType, patternType)
                    
                    // Process arm body with extended environment
                    val extendedEnv = typeEnv.extend(patternEnv.env)
                    val (bodyType, bodyConstraints, bodyBindings) = 
                        generateConstraints(arm.body, extendedEnv)
                    constraints.addAll(bodyConstraints)
                    bindings.putAll(bodyBindings)
                    
                    // All arms must have the same result type
                    constraints.addEquality(resultType, bodyType)
                }
                
                bindings[node] = resultType
                resultType
            }
            
            is Parens -> {
                val (exprType, exprConstraints, exprBindings) = 
                    generateConstraints(node.expression, typeEnv)
                constraints.addAll(exprConstraints)
                bindings.putAll(exprBindings)
                bindings[node] = exprType
                exprType
            }
            
            is TypeExpr -> {
                // For type expressions we need to resolve the actual type
                val resolvedType = resolveTypeExpr(node, typeEnv)
                bindings[node] = resolvedType
                resolvedType
            }

            else -> {
                throw TypeError("Unsupported AST node type: ${node.javaClass.name}")
            }
        }
        
        return Triple(type, constraints, bindings)
    }
    
    /**
     * Generate constraints for an expression suffix.
     * 
     * @param suffix The suffix node.
     * @param baseType The type of the expression the suffix is applied to.
     * @param typeEnv The current type environment.
     * @return A triple of (result type, constraints, type variable bindings).
     */
    private fun generateSuffixConstraints(
        suffix: ExpressionSuffix, 
        baseType: Type, 
        typeEnv: TypeEnv
    ): Triple<Type, Constraints, Map<AstNode, Type>> {
        val constraints = Constraints()
        val bindings = mutableMapOf<AstNode, Type>()
        
        val type = when (suffix) {
            is ApplicationSuffix -> {
                val argTypes = mutableListOf<Type>()
                
                if (suffix.args != null) {
                    val allArgs = listOf(suffix.args.firstExpr) + 
                                 suffix.args.otherExprs.map { it.second }
                    
                    for (arg in allArgs) {
                        val (argType, argConstraints, argBindings) = 
                            generateConstraints(arg, typeEnv)
                        constraints.addAll(argConstraints)
                        bindings.putAll(argBindings)
                        argTypes.add(argType)
                    }
                }
                
                // Create a fresh type variable for the function result
                val resultType = freshTypeVar()
                
                // Always constrain the base to be a function type
                val functionType = FunctionType(argTypes, resultType)
                constraints.addEquality(baseType, functionType)
                
                bindings[suffix] = resultType
                resultType
            }
            
            is AccessSuffix -> {
                val fieldName = suffix.fieldName.lexeme
                val recordFieldType = freshTypeVar()
                val rowVar = freshTypeVar()
                
                // Base must be a record with the accessed field and a row variable for extra fields
                val recordType = RecordType(mapOf(fieldName to recordFieldType), rowVar)
                
                // Use equality constraint for record access
                constraints.addEquality(baseType, recordType)
                
                bindings[suffix] = recordFieldType
                recordFieldType
            }
        }
        
        return Triple(type, constraints, bindings)
    }
    
    /**
     * Generate constraints for a pattern.
     * 
     * @param pattern The pattern node.
     * @param typeEnv The current type environment.
     * @return A quadruple of (pattern type, constraints, type bindings, environment bindings).
     */
    private fun generatePatternConstraints(
        pattern: Pattern,
        typeEnv: TypeEnv
    ): Quadruple<Type, Constraints, Map<AstNode, Type>, TypeEnv> {
        val constraints = Constraints()
        val bindings = mutableMapOf<AstNode, Type>()
        val envBindings = mutableMapOf<String, TypeScheme>()
        
        val type = when (pattern) {
            is VariablePattern -> {
                val varName = pattern.name.lexeme
                val varType = freshTypeVar()
                
                // Add variable to environment
                envBindings[varName] = TypeScheme(emptyList(), varType)
                
                bindings[pattern] = varType
                varType
            }
            
            is RecordPattern -> {
                val fields = mutableMapOf<String, Type>()
                
                if (pattern.fields != null) {
                    val allFields = listOf(pattern.fields.firstField) + 
                                   pattern.fields.otherFields.map { it.second }
                    
                    for (field in allFields) {
                        val name = field.name.lexeme
                        
                        val (fieldType, fieldConstraints, fieldBindings, fieldEnv) = 
                            generatePatternConstraints(field.pattern, typeEnv)
                        constraints.addAll(fieldConstraints)
                        bindings.putAll(fieldBindings)
                        envBindings.putAll(fieldEnv.env)
                        
                        fields[name] = fieldType
                    }
                }
                
                val recordType = RecordType(fields)
                bindings[pattern] = recordType
                recordType
            }
            
            is ConstructorPattern -> {
                val constructorName = pattern.name.lexeme
                val constructorType = typeEnv.lookup(constructorName) ?:
                    throw TypeError("Undefined constructor: $constructorName at ${pattern.position}")
                
                // For now, treat constructors as functions that return their type
                if (pattern.args != null && pattern.args.fields != null) {
                    val allArgs = listOf(pattern.args.fields.firstPattern) +
                                  pattern.args.fields.otherPatterns.map { it.second }
                    val argTypes = mutableListOf<Type>()
                    
                    for (arg in allArgs) {
                        val (argType, argConstraints, argBindings, argEnv) = 
                            generatePatternConstraints(arg, typeEnv)
                        constraints.addAll(argConstraints)
                        bindings.putAll(argBindings)
                        envBindings.putAll(argEnv.env)
                        
                        argTypes.add(argType)
                    }
                    
                    val resultType = freshTypeVar()
                    val functionType = FunctionType(argTypes, resultType)
                    constraints.addEquality(constructorType, functionType)
                    
                    bindings[pattern] = resultType
                    resultType
                } else {
                    bindings[pattern] = constructorType
                    constructorType
                }
            }
            
            is StringLiteralPattern -> {
                val stringType = NamedType.STRING
                bindings[pattern] = stringType
                stringType
            }
            
            is IntegerLiteralPattern -> {
                val numberType = NamedType.NUMBER
                bindings[pattern] = numberType
                numberType
            }
            
            is TrueLiteralPattern, is FalseLiteralPattern -> {
                val booleanType = NamedType.BOOLEAN
                bindings[pattern] = booleanType
                booleanType
            }
        }
        
        return Quadruple(type, constraints, bindings, TypeEnv(envBindings))
    }

    /**
     * Resolve a type expression node to a Type.
     * 
     * @param node The type expression node.
     * @param typeEnv The current type environment.
     * @return The resolved type.
     */
    private fun resolveTypeExpr(node: AstNode, typeEnv: TypeEnv): Type {
        return when (node) {
            is TypeNameType -> {
                val typeName = node.name.token.lexeme
                val baseType = typeEnv.lookup(typeName) ?: 
                    throw TypeError("Undefined type: $typeName at ${node.position}")
                
                if (node.args != null) {
                    // Handle generic type arguments
                    val typeArgs = mutableListOf<Type>()
                    val allArgs = listOf(node.args.firstArg) + 
                                 node.args.otherArgs.map { it.second }
                    
                    for (arg in allArgs) {
                        typeArgs.add(resolveTypeExpr(arg, typeEnv))
                    }
                    
                    when (baseType) {
                        is NamedType -> NamedType(baseType.name, typeArgs)
                        else -> throw TypeError("Type $typeName is not a generic type at ${node.position}")
                    }
                } else {
                    baseType
                }
            }
            
            is com.ucle.ast.RecordType -> {
                val fields = mutableMapOf<String, Type>()
                
                if (node.fields != null) {
                    val allFields = listOf(node.fields.firstField) + 
                                   node.fields.otherFields.map { it.second }
                    
                    for (field in allFields) {
                        val name = field.name.lexeme
                        fields[name] = resolveTypeExpr(field.type, typeEnv)
                    }
                }
                
                RecordType(fields)
            }
            
            is ParensType -> resolveTypeExpr(node.type, typeEnv)
            
            is com.ucle.ast.FunctionType -> {
                val inputType = resolveTypeExpr(node.inputType, typeEnv)
                
                if (node.arrows.isEmpty()) {
                    inputType
                } else {
                    // Build up nested function types for each arrow
                    var currentType = inputType
                    for ((_, outputTypeNode) in node.arrows) {
                        val outputType = resolveTypeExpr(outputTypeNode, typeEnv)
                        currentType = FunctionType(listOf(currentType), outputType)
                    }
                    currentType
                }
            }
            
            is com.ucle.ast.UnionType -> {
                val firstType = resolveTypeExpr(node.firstType, typeEnv)
                
                if (node.otherTypes.isEmpty()) {
                    firstType
                } else {
                    val types = mutableSetOf<Type>()
                    types.add(firstType)
                    
                    for ((_, typeNode) in node.otherTypes) {
                        types.add(resolveTypeExpr(typeNode, typeEnv))
                    }
                    
                    UnionType(types)
                }
            }
            
            is com.ucle.ast.IntersectionType -> {
                val firstType = resolveTypeExpr(node.firstType, typeEnv)
                
                if (node.otherTypes.isEmpty()) {
                    firstType
                } else {
                    val types = mutableSetOf<Type>()
                    types.add(firstType)
                    
                    for ((_, typeNode) in node.otherTypes) {
                        types.add(resolveTypeExpr(typeNode, typeEnv))
                    }
                    
                    IntersectionType(types)
                }
            }
            
            is StringLiteralType -> LiteralType(node.token.lexeme, NamedType.STRING)
            is IntegerLiteralType -> {
                val value = node.token.lexeme.toIntOrNull() ?: 
                    throw TypeError("Invalid integer literal: ${node.token.lexeme} at ${node.position}")
                LiteralType(value, NamedType.NUMBER)
            }
            is TrueLiteralType -> LiteralType(true, NamedType.BOOLEAN)
            is FalseLiteralType -> LiteralType(false, NamedType.BOOLEAN)
            
            else -> throw TypeError("Unsupported type expression: ${node.javaClass.name}")
        }
    }
}

/**
 * A quadruple of values.
 */
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
