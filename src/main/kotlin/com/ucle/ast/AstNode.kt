package com.ucle.ast

import com.ucle.parser.Token
import io.littlelanguages.scanpiler.Location

/**
 * Base class for all AST nodes.
 */
sealed class AstNode {
    /**
     * Gets the position of this node in the source code.
     */
    abstract val position: Location
}

/**
 * Represents the root of the AST.
 *
 * @property statements The list of statements in the program.
 */
data class Program(
    val statements: List<Statement>,
    override val position: Location
) : AstNode()

/**
 * Base class for all statements in the language.
 */
sealed class Statement : AstNode()

/**
 * A statement that declares a type.
 *
 * @property typeToken The 'type' keyword token
 * @property declarations The list of type declarations
 * @property semicolonToken The semicolon token
 */
data class TypeDecl(
    val typeToken: Token,
    val declarations: List<SingleTypeDecl>,
    val semicolonToken: Token,
    override val position: Location
) : Statement()

/**
 * A single type declaration within a type declaration statement.
 *
 * @property name The name of the type
 * @property genericParams Optional generic parameters
 * @property equalToken The equals token
 * @property typeExpr The type expression
 */
data class SingleTypeDecl(
    val name: TypeName,
    val genericParams: GenericParams?,
    val equalToken: Token,
    val typeExpr: TypeExpr,
    override val position: Location
) : AstNode()

/**
 * Generic parameters for a type declaration.
 *
 * @property leftBracketToken The left angle bracket token
 * @property typeVars The list of type variables
 * @property rightBracketToken The right angle bracket token
 */
data class GenericParams(
    val leftBracketToken: Token,
    val typeVars: List<TypeVar>,
    val rightBracketToken: Token,
    override val position: Location
) : AstNode()

/**
 * Represents a type variable.
 *
 * @property token The token for the type variable (uppercase identifier)
 */
data class TypeVar(
    val token: Token,
    override val position: Location
) : AstNode()

/**
 * Represents a type name.
 *
 * @property token The token for the type name (uppercase identifier)
 */
data class TypeName(
    val token: Token,
    override val position: Location
) : AstNode()

/**
 * A statement that declares variables or functions.
 *
 * @property letToken The 'let' keyword token
 * @property declarations The list of let declarations
 * @property semicolonToken The semicolon token
 */
data class LetDecl(
    val letToken: Token,
    val declarations: List<SingleLetDecl>,
    val semicolonToken: Token,
    override val position: Location
) : Statement()

/**
 * A single let declaration within a let declaration statement.
 *
 * @property name The name of the variable or function
 * @property genericParams Optional generic parameters
 * @property parameterList Optional function parameters
 * @property typeAnnotation Optional type annotation
 * @property arrowToken The arrow token
 * @property body The body expression
 */
data class SingleLetDecl(
    val name: Token,
    val genericParams: GenericParams?,
    val parameterList: ParamListWrapper?,
    val typeAnnotation: TypeAnnotation?,
    val arrowToken: Token,
    val body: Expression,
    override val position: Location
) : AstNode()

/**
 * Wrapper for parameter list to include the parentheses tokens.
 *
 * @property leftParenToken The left parenthesis token
 * @property paramList The parameter list (null if empty)
 * @property rightParenToken The right parenthesis token
 */
data class ParamListWrapper(
    val leftParenToken: Token,
    val paramList: ParamList?,
    val rightParenToken: Token,
    override val position: Location
) : AstNode()

/**
 * A type annotation.
 *
 * @property colonToken The colon token
 * @property typeExpr The type expression
 */
data class TypeAnnotation(
    val colonToken: Token,
    val typeExpr: TypeExpr,
    override val position: Location
) : AstNode()

/**
 * An expression statement.
 *
 * @property expression The expression
 * @property semicolonToken The semicolon token
 */
data class ExprStmt(
    val expression: Expression,
    val semicolonToken: Token,
    override val position: Location
) : Statement()

/**
 * Base class for all expressions.
 */
sealed class Expression : AstNode()

/**
 * An expression with a primary expression and optional suffixes.
 *
 * @property primary The primary expression
 * @property suffixes The list of expression suffixes
 */
data class CompoundExpression(
    val primary: PrimaryExpr,
    val suffixes: List<ExpressionSuffix>,
    override val position: Location
) : Expression()

/**
 * Base class for all expression suffixes.
 */
sealed class ExpressionSuffix : AstNode()

/**
 * A function application suffix.
 *
 * @property leftParenToken The left parenthesis token
 * @property args Optional expression list (arguments)
 * @property rightParenToken The right parenthesis token
 */
data class ApplicationSuffix(
    val leftParenToken: Token,
    val args: ExpressionList?,
    val rightParenToken: Token,
    override val position: Location
) : ExpressionSuffix()

/**
 * A field access suffix.
 *
 * @property dotToken The dot token
 * @property fieldName The field name token
 */
data class AccessSuffix(
    val dotToken: Token,
    val fieldName: Token,
    override val position: Location
) : ExpressionSuffix()

/**
 * Base class for primary expressions.
 */
sealed class PrimaryExpr : AstNode()

/**
 * A lambda expression.
 *
 * @property fnToken The 'fn' keyword token
 * @property leftParenToken The left parenthesis token
 * @property params Optional parameter list
 * @property rightParenToken The right parenthesis token
 * @property arrowToken The arrow token
 * @property body The body expression
 */
data class Lambda(
    val fnToken: Token,
    val leftParenToken: Token,
    val params: ParamList?,
    val rightParenToken: Token,
    val arrowToken: Token,
    val body: Expression,
    override val position: Location
) : PrimaryExpr()

/**
 * A parameter list.
 *
 * @property firstParam The first parameter
 * @property otherParams The rest of the parameters with their comma tokens
 */
data class ParamList(
    val firstParam: Param,
    val otherParams: List<Pair<Token, Param>>,
    override val position: Location
) : AstNode()

/**
 * A parameter.
 *
 * @property name The parameter name token
 * @property typeAnnotation Optional type annotation
 */
data class Param(
    val name: Token,
    val typeAnnotation: TypeAnnotation?,
    override val position: Location
) : AstNode()

/**
 * A constant expression.
 *
 * @property constToken The 'const' keyword token
 * @property name The variable name token
 * @property equalToken The equals token
 * @property value The value expression
 * @property inToken The 'in' keyword token
 * @property body The body expression
 */
data class ConstExpr(
    val constToken: Token,
    val name: Token,
    val equalToken: Token,
    val value: Expression,
    val inToken: Token,
    val body: Expression,
    override val position: Location
) : PrimaryExpr()

/**
 * A match expression.
 *
 * @property matchToken The 'match' keyword token
 * @property scrutinee The expression to match against
 * @property leftBraceToken The left brace token
 * @property firstArm The first match arm
 * @property otherArms The rest of the match arms
 * @property rightBraceToken The right brace token
 */
data class MatchExpr(
    val matchToken: Token,
    val scrutinee: Expression,
    val leftBraceToken: Token,
    val firstArm: MatchArm,
    val otherArms: List<MatchArm>,
    val rightBraceToken: Token,
    override val position: Location
) : PrimaryExpr()

/**
 * A match arm.
 *
 * @property caseToken The 'case' keyword token
 * @property pattern The pattern to match
 * @property arrowToken The arrow token
 * @property body The body expression
 */
data class MatchArm(
    val caseToken: Token,
    val pattern: Pattern,
    val arrowToken: Token,
    val body: Expression,
    override val position: Location
) : AstNode()

/**
 * Base class for all patterns.
 */
sealed class Pattern : AstNode()

/**
 * A record pattern.
 *
 * @property rectToken The 'rect' keyword token
 * @property leftBraceToken The left brace token
 * @property fields Optional field pattern list
 * @property rightBraceToken The right brace token
 */
data class RecordPattern(
    val rectToken: Token,
    val leftBraceToken: Token,
    val fields: FieldPatternList?,
    val rightBraceToken: Token,
    override val position: Location
) : Pattern()

/**
 * Base class for identifier patterns.
 */
sealed class IdentifierPattern : Pattern()

/**
 * A constructor pattern.
 *
 * @property name The constructor name token
 * @property args Optional pattern arguments
 */
data class ConstructorPattern(
    val name: Token,
    val args: PatternArgs?,
    override val position: Location
) : IdentifierPattern()

/**
 * A variable pattern.
 *
 * @property name The variable name token
 */
data class VariablePattern(
    val name: Token,
    override val position: Location
) : IdentifierPattern()

/**
 * A pattern with arguments.
 *
 * @property leftParenToken The left parenthesis token
 * @property fields Optional pattern fields
 * @property rightParenToken The right parenthesis token
 */
data class PatternArgs(
    val leftParenToken: Token,
    val fields: PatternFields?,
    val rightParenToken: Token,
    override val position: Location
) : AstNode()

/**
 * A list of pattern fields.
 *
 * @property firstPattern The first pattern
 * @property otherPatterns The rest of the patterns with their comma tokens
 */
data class PatternFields(
    val firstPattern: Pattern,
    val otherPatterns: List<Pair<Token, Pattern>>,
    override val position: Location
) : AstNode()

/**
 * A list of field patterns.
 *
 * @property firstField The first field pattern
 * @property otherFields The rest of the field patterns with their comma tokens
 */
data class FieldPatternList(
    val firstField: FieldPattern,
    val otherFields: List<Pair<Token, FieldPattern>>,
    override val position: Location
) : AstNode()

/**
 * A field pattern.
 *
 * @property name The field name token
 * @property colonToken The colon token
 * @property pattern The pattern
 */
data class FieldPattern(
    val name: Token,
    val colonToken: Token,
    val pattern: Pattern,
    override val position: Location
) : AstNode()

/**
 * A list of expressions.
 *
 * @property firstExpr The first expression
 * @property otherExprs The rest of the expressions with their comma tokens
 */
data class ExpressionList(
    val firstExpr: Expression,
    val otherExprs: List<Pair<Token, Expression>>,
    override val position: Location
) : AstNode()

/**
 * A record literal.
 *
 * @property rectToken The 'rect' keyword token
 * @property leftBraceToken The left brace token
 * @property fields Optional field list
 * @property rightBraceToken The right brace token
 */
data class RecordLiteral(
    val rectToken: Token,
    val leftBraceToken: Token,
    val fields: FieldList?,
    val rightBraceToken: Token,
    override val position: Location
) : PrimaryExpr()

/**
 * A list of fields for a record literal.
 *
 * @property firstField The first field
 * @property otherFields The rest of the fields with their comma tokens
 */
data class FieldList(
    val firstField: Field,
    val otherFields: List<Pair<Token, Field>>,
    override val position: Location
) : AstNode()

/**
 * A field in a record literal.
 *
 * @property name The field name token
 * @property colonToken The colon token
 * @property value The value expression
 */
data class Field(
    val name: Token,
    val colonToken: Token,
    val value: Expression,
    override val position: Location
) : AstNode()

/**
 * An expression wrapped in parentheses.
 *
 * @property leftParenToken The left parenthesis token
 * @property expression The wrapped expression
 * @property rightParenToken The right parenthesis token
 */
data class Parens(
    val leftParenToken: Token,
    val expression: Expression,
    val rightParenToken: Token,
    override val position: Location
) : PrimaryExpr()

/**
 * Base class for literal expressions.
 */
sealed class LiteralExpr : PrimaryExpr()

/**
 * A string literal expression.
 *
 * @property token The string literal token
 */
data class StringLiteralExpr(
    val token: Token,
    override val position: Location
) : LiteralExpr()

/**
 * An integer literal expression.
 *
 * @property token The integer literal token
 */
data class IntegerLiteralExpr(
    val token: Token,
    override val position: Location
) : LiteralExpr()

/**
 * A true literal expression.
 *
 * @property token The 'true' keyword token
 */
data class TrueLiteralExpr(
    val token: Token,
    override val position: Location
) : LiteralExpr()

/**
 * A false literal expression.
 *
 * @property token The 'false' keyword token
 */
data class FalseLiteralExpr(
    val token: Token,
    override val position: Location
) : LiteralExpr()

/**
 * Base class for identifier expressions.
 */
sealed class IdentifierExpr : PrimaryExpr()

/**
 * An uppercase identifier expression (typically a type or constructor reference).
 *
 * @property token The uppercase identifier token
 */
data class UpperIdentifierExpr(
    val token: Token,
    override val position: Location
) : IdentifierExpr()

/**
 * A lowercase identifier expression (typically a variable reference).
 *
 * @property token The lowercase identifier token
 */
data class LowerIdentifierExpr(
    val token: Token,
    override val position: Location
) : IdentifierExpr()

/**
 * Base class for literal patterns.
 */
sealed class LiteralPattern : Pattern()

/**
 * A string literal pattern.
 *
 * @property token The string literal token
 */
data class StringLiteralPattern(
    val token: Token,
    override val position: Location
) : LiteralPattern()

/**
 * An integer literal pattern.
 *
 * @property token The integer literal token
 */
data class IntegerLiteralPattern(
    val token: Token,
    override val position: Location
) : LiteralPattern()

/**
 * A true literal pattern.
 *
 * @property token The 'true' keyword token
 */
data class TrueLiteralPattern(
    val token: Token,
    override val position: Location
) : LiteralPattern()

/**
 * A false literal pattern.
 *
 * @property token The 'false' keyword token
 */
data class FalseLiteralPattern(
    val token: Token,
    override val position: Location
) : LiteralPattern()

/**
 * Base class for all type expressions.
 */
sealed class TypeExpr : AstNode()

/**
 * A function type.
 *
 * @property inputType The input type
 * @property arrows The list of arrow tokens and output types
 */
data class FunctionType(
    val inputType: UnionType,
    val arrows: List<Pair<Token, UnionType>>,
    override val position: Location
) : TypeExpr()

/**
 * A union type.
 *
 * @property firstType The first type
 * @property otherTypes The rest of the types with their bar tokens
 */
data class UnionType(
    val firstType: IntersectionType,
    val otherTypes: List<Pair<Token, IntersectionType>>,
    override val position: Location
) : AstNode()

/**
 * An intersection type.
 *
 * @property firstType The first type
 * @property otherTypes The rest of the types with their ampersand tokens
 */
data class IntersectionType(
    val firstType: PrimaryType,
    val otherTypes: List<Pair<Token, PrimaryType>>,
    override val position: Location
) : AstNode()

/**
 * Base class for all primary types.
 */
sealed class PrimaryType : AstNode()

/**
 * A type name type.
 *
 * @property name The type name
 * @property args Optional type arguments
 */
data class TypeNameType(
    val name: TypeName,
    val args: TypeArgs?,
    override val position: Location
) : PrimaryType()

/**
 * Type arguments for a type.
 *
 * @property leftBracketToken The left angle bracket token
 * @property firstArg The first type argument
 * @property otherArgs The rest of the type arguments with their comma tokens
 * @property rightBracketToken The right angle bracket token
 */
data class TypeArgs(
    val leftBracketToken: Token,
    val firstArg: TypeExpr,
    val otherArgs: List<Pair<Token, TypeExpr>>,
    val rightBracketToken: Token,
    override val position: Location
) : AstNode()

/**
 * A record type.
 *
 * @property rectToken The 'rect' keyword token
 * @property leftBraceToken The left brace token
 * @property fields Optional type field list
 * @property rightBraceToken The right brace token
 */
data class RecordType(
    val rectToken: Token,
    val leftBraceToken: Token,
    val fields: TypeFieldList?,
    val rightBraceToken: Token,
    override val position: Location
) : PrimaryType()

/**
 * A list of type fields.
 *
 * @property firstField The first type field
 * @property otherFields The rest of the type fields with their comma tokens
 */
data class TypeFieldList(
    val firstField: TypeField,
    val otherFields: List<Pair<Token, TypeField>>,
    override val position: Location
) : AstNode()

/**
 * A type field in a record type.
 *
 * @property name The field name token
 * @property colonToken The colon token
 * @property type The field type
 */
data class TypeField(
    val name: Token,
    val colonToken: Token,
    val type: TypeExpr,
    override val position: Location
) : AstNode()

/**
 * A type expression wrapped in parentheses.
 *
 * @property leftParenToken The left parenthesis token
 * @property type The wrapped type expression
 * @property rightParenToken The right parenthesis token
 */
data class ParensType(
    val leftParenToken: Token,
    val type: TypeExpr,
    val rightParenToken: Token,
    override val position: Location
) : PrimaryType()

/**
 * A literal type.
 */
sealed class LiteralType : PrimaryType()

/**
 * A string literal type.
 *
 * @property token The string literal token
 */
data class StringLiteralType(
    val token: Token,
    override val position: Location
) : LiteralType()

/**
 * An integer literal type.
 *
 * @property token The integer literal token
 */
data class IntegerLiteralType(
    val token: Token,
    override val position: Location
) : LiteralType()

/**
 * A true literal type.
 *
 * @property token The 'true' keyword token
 */
data class TrueLiteralType(
    val token: Token,
    override val position: Location
) : LiteralType()

/**
 * A false literal type.
 *
 * @property token The 'false' keyword token
 */
data class FalseLiteralType(
    val token: Token,
    override val position: Location
) : LiteralType()
