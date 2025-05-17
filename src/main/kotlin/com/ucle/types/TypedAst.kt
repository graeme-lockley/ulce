package com.ucle.types

/**
 * Base class for all typed AST nodes.
 */
sealed class TypedAstNode {
    /**
     * The inferred type of this node.
     */
    abstract val type: Type
}

/**
 * A typed program.
 * 
 * @property statements The list of typed statements.
 * @property type The inferred type of the program.
 */
data class TypedProgram(
    val statements: List<TypedStatement>,
    override val type: Type
) : TypedAstNode()

/**
 * Base class for all typed statements.
 */
sealed class TypedStatement : TypedAstNode()

/**
 * A typed expression statement.
 * 
 * @property expression The typed expression.
 * @property type The inferred type of the statement.
 */
data class TypedExprStmt(
    val expression: TypedExpression,
    override val type: Type
) : TypedStatement()

/**
 * A typed type declaration statement.
 * 
 * @property declarations The list of typed type declarations.
 * @property type The inferred type of the statement.
 */
data class TypedTypeDecl(
    val declarations: List<TypedSingleTypeDecl>,
    override val type: Type
) : TypedStatement()

/**
 * A typed let declaration statement.
 * 
 * @property declarations The list of typed let declarations.
 * @property type The inferred type of the statement.
 */
data class TypedLetDecl(
    val declarations: List<TypedSingleLetDecl>,
    override val type: Type
) : TypedStatement()

/**
 * A typed single type declaration.
 * 
 * @property name The name of the type.
 * @property genericParams Optional typed generic parameters.
 * @property typeExpr The typed type expression.
 * @property type The inferred type of the declaration.
 */
data class TypedSingleTypeDecl(
    val name: String,
    val genericParams: TypedGenericParams?,
    val typeExpr: TypedTypeExpr,
    override val type: Type
) : TypedAstNode()

/**
 * A typed single let declaration.
 * 
 * @property name The name of the variable or function.
 * @property genericParams Optional typed generic parameters.
 * @property params The list of typed parameters (for functions).
 * @property body The typed body expression.
 * @property type The inferred type of the declaration.
 */
data class TypedSingleLetDecl(
    val name: String,
    val genericParams: TypedGenericParams?,
    val params: List<TypedParam>,
    val body: TypedExpression,
    override val type: Type
) : TypedAstNode()

/**
 * Typed generic parameters.
 * 
 * @property typeVars The list of typed type variables.
 */
data class TypedGenericParams(
    val typeVars: List<TypedTypeVar>
)

/**
 * A typed type variable.
 * 
 * @property name The name of the type variable.
 */
data class TypedTypeVar(
    val name: String
) : TypedTypeExpr()

/**
 * A typed parameter.
 * 
 * @property name The name of the parameter.
 * @property type The inferred type of the parameter.
 */
data class TypedParam(
    val name: String,
    override val type: Type
) : TypedAstNode()

/**
 * Base class for all typed expressions.
 */
sealed class TypedExpression : TypedAstNode()

/**
 * A typed compound expression.
 * 
 * @property primary The typed primary expression.
 * @property suffixes The list of typed expression suffixes.
 * @property type The inferred type of the expression.
 */
data class TypedCompoundExpression(
    val primary: TypedPrimaryExpr,
    val suffixes: List<TypedExpressionSuffix>,
    override val type: Type
) : TypedExpression()

/**
 * Base class for all typed expression suffixes.
 */
sealed class TypedExpressionSuffix : TypedAstNode()

/**
 * A typed function application suffix.
 * 
 * @property args The list of typed argument expressions.
 * @property type The inferred type of the suffix.
 */
data class TypedApplicationSuffix(
    val args: List<TypedExpression>,
    override val type: Type
) : TypedExpressionSuffix()

/**
 * A typed field access suffix.
 * 
 * @property fieldName The name of the accessed field.
 * @property type The inferred type of the suffix.
 */
data class TypedAccessSuffix(
    val fieldName: String,
    override val type: Type
) : TypedExpressionSuffix()

/**
 * Base class for all typed primary expressions.
 */
sealed class TypedPrimaryExpr : TypedAstNode()

/**
 * A typed identifier expression.
 * 
 * @property name The name of the identifier.
 * @property type The inferred type of the expression.
 */
data class TypedIdentifierExpr(
    val name: String,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed string literal expression.
 * 
 * @property value The string value.
 * @property type The inferred type of the expression.
 */
data class TypedStringLiteralExpr(
    val value: String,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed integer literal expression.
 * 
 * @property value The integer value.
 * @property type The inferred type of the expression.
 */
data class TypedIntegerLiteralExpr(
    val value: Int,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed boolean literal expression.
 * 
 * @property value The boolean value.
 * @property type The inferred type of the expression.
 */
data class TypedBooleanLiteralExpr(
    val value: Boolean,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed lambda expression.
 * 
 * @property params The list of typed parameters.
 * @property body The typed body expression.
 * @property type The inferred type of the expression.
 */
data class TypedLambda(
    val params: List<TypedParam>,
    val body: TypedExpression,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed record literal expression.
 * 
 * @property fields The list of typed fields.
 * @property type The inferred type of the expression.
 */
data class TypedRecordLiteral(
    val fields: List<TypedField>,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed field in a record literal.
 * 
 * @property name The name of the field.
 * @property value The typed value expression.
 */
data class TypedField(
    val name: String,
    val value: TypedExpression
)

/**
 * A typed constant expression.
 * 
 * @property name The name of the constant.
 * @property value The typed value expression.
 * @property body The typed body expression.
 * @property type The inferred type of the expression.
 */
data class TypedConstExpr(
    val name: String,
    val value: TypedExpression,
    val body: TypedExpression,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed match expression.
 * 
 * @property scrutinee The typed scrutinee expression.
 * @property arms The list of typed match arms.
 * @property type The inferred type of the expression.
 */
data class TypedMatchExpr(
    val scrutinee: TypedExpression,
    val arms: List<TypedMatchArm>,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * A typed match arm.
 * 
 * @property pattern The typed pattern.
 * @property body The typed body expression.
 */
data class TypedMatchArm(
    val pattern: TypedPattern,
    val body: TypedExpression
)

/**
 * A typed parenthesized expression.
 * 
 * @property expression The typed inner expression.
 * @property type The inferred type of the expression.
 */
data class TypedParensExpr(
    val expression: TypedExpression,
    override val type: Type
) : TypedPrimaryExpr()

/**
 * Base class for all typed patterns.
 */
sealed class TypedPattern : TypedAstNode()

/**
 * A typed variable pattern.
 * 
 * @property name The name of the variable.
 * @property type The inferred type of the pattern.
 */
data class TypedVariablePattern(
    val name: String,
    override val type: Type
) : TypedPattern()

/**
 * A typed string literal pattern.
 * 
 * @property value The string value.
 * @property type The inferred type of the pattern.
 */
data class TypedStringLiteralPattern(
    val value: String,
    override val type: Type
) : TypedPattern()

/**
 * A typed integer literal pattern.
 * 
 * @property value The integer value.
 * @property type The inferred type of the pattern.
 */
data class TypedIntegerLiteralPattern(
    val value: Int,
    override val type: Type
) : TypedPattern()

/**
 * A typed boolean literal pattern.
 * 
 * @property value The boolean value.
 * @property type The inferred type of the pattern.
 */
data class TypedBooleanLiteralPattern(
    val value: Boolean,
    override val type: Type
) : TypedPattern()

/**
 * A typed record pattern.
 * 
 * @property fields The list of typed field patterns.
 * @property type The inferred type of the pattern.
 */
data class TypedRecordPattern(
    val fields: List<TypedFieldPattern>,
    override val type: Type
) : TypedPattern()

/**
 * A typed field pattern.
 * 
 * @property name The name of the field.
 * @property pattern The typed pattern.
 */
data class TypedFieldPattern(
    val name: String,
    val pattern: TypedPattern
)

/**
 * A typed constructor pattern.
 * 
 * @property name The name of the constructor.
 * @property args The list of typed argument patterns.
 * @property type The inferred type of the pattern.
 */
data class TypedConstructorPattern(
    val name: String,
    val args: List<TypedPattern>,
    override val type: Type
) : TypedPattern()

/**
 * Base class for all typed type expressions.
 */
sealed class TypedTypeExpr

/**
 * A typed named type.
 * 
 * @property name The name of the type.
 */
data class TypedNamedType(
    val name: String
) : TypedTypeExpr()

/**
 * A typed generic type.
 * 
 * @property name The name of the type.
 * @property typeArgs The list of typed type arguments.
 */
data class TypedGenericType(
    val name: String,
    val typeArgs: List<TypedTypeExpr>
) : TypedTypeExpr()

/**
 * A typed function type.
 * 
 * @property paramTypes The list of typed parameter types.
 * @property returnType The typed return type.
 */
data class TypedFunctionType(
    val paramTypes: List<TypedTypeExpr>,
    val returnType: TypedTypeExpr
) : TypedTypeExpr()

/**
 * A typed record type.
 * 
 * @property fields The list of typed type fields.
 */
data class TypedRecordType(
    val fields: List<TypedTypeField>
) : TypedTypeExpr()

/**
 * A typed type field.
 * 
 * @property name The name of the field.
 * @property type The typed field type.
 */
data class TypedTypeField(
    val name: String,
    val type: TypedTypeExpr
)

/**
 * A typed union type.
 * 
 * @property types The list of typed types in the union.
 */
data class TypedUnionType(
    val types: List<TypedTypeExpr>
) : TypedTypeExpr()

/**
 * A typed intersection type.
 * 
 * @property types The list of typed types in the intersection.
 */
data class TypedIntersectionType(
    val types: List<TypedTypeExpr>
) : TypedTypeExpr()

/**
 * A typed string literal type.
 * 
 * @property value The string value.
 */
data class TypedStringLiteralType(
    val value: String
) : TypedTypeExpr()

/**
 * A typed integer literal type.
 * 
 * @property value The integer value.
 */
data class TypedIntegerLiteralType(
    val value: Int
) : TypedTypeExpr()

/**
 * A typed boolean literal type.
 * 
 * @property value The boolean value.
 */
data class TypedBooleanLiteralType(
    val value: Boolean
) : TypedTypeExpr()
