package com.ucle.ast

import com.ucle.parser.Token
import com.ucle.parser.Visitor
import io.littlelanguages.data.Tuple2
import io.littlelanguages.data.Tuple3
import io.littlelanguages.scanpiler.LocationCoordinate

/**
 * AST Builder that implements the visitor interface to construct AST nodes.
 */
class AstBuilder : Visitor<
        Program, Statement, ExprStmt,
        TypeDecl, SingleTypeDecl, GenericParams, TypeVar, TypeName,
        LetDecl, SingleLetDecl, Expression, ExpressionSuffix, ApplicationSuffix, AccessSuffix,
        PrimaryExpr, Lambda, ParamList, Param, ConstExpr, MatchExpr, MatchArm,
        Pattern, IdentifierPattern, PatternArgs, PatternFields, RecordPattern, FieldPatternList, FieldPattern,
        ExpressionList, RecordLiteral, FieldList, Field, Parens, LiteralExpr,
        TypeExpr, FunctionType, UnionType, IntersectionType, PrimaryType, TypeNameType, TypeArgs,
        RecordType, TypeFieldList, TypeField, ParensType> {

    override fun visitProgram(a: List<Statement>): Program {
        val position = if (a.isNotEmpty()) a.first().position else LocationCoordinate(0, 0, 0)
        return Program(a, position)
    }

    override fun visitStatement1(a: TypeDecl): Statement = a

    override fun visitStatement2(a: LetDecl): Statement = a

    override fun visitStatement3(a: ExprStmt): Statement = a

    override fun visitExprStmt(a1: Expression, a2: Token): ExprStmt {
        return ExprStmt(a1, a2, a1.position)
    }

    override fun visitTypeDecl(
        a1: Token, a2: SingleTypeDecl, 
        a3: List<Tuple2<Token, SingleTypeDecl>>, 
        a4: Token
    ): TypeDecl {
        val declarations = mutableListOf(a2)
        a3.forEach { (_, decl) -> declarations.add(decl) }
        return TypeDecl(a1, declarations, a4, a1.location)
    }

    override fun visitSingleTypeDecl(
        a1: TypeName, a2: GenericParams?, a3: Token, a4: TypeExpr
    ): SingleTypeDecl {
        return SingleTypeDecl(a1, a2, a3, a4, a1.position)
    }

    override fun visitGenericParams(
        a1: Token, a2: TypeVar, 
        a3: List<Tuple2<Token, TypeVar>>, 
        a4: Token
    ): GenericParams {
        val typeVars = mutableListOf(a2)
        a3.forEach { (_, typeVar) -> typeVars.add(typeVar) }
        return GenericParams(a1, typeVars, a4, a1.location)
    }

    override fun visitTypeVar(a: Token): TypeVar {
        return TypeVar(a, a.location)
    }

    override fun visitTypeName(a: Token): TypeName {
        return TypeName(a, a.location)
    }

    override fun visitLetDecl(
        a1: Token, a2: SingleLetDecl, 
        a3: List<Tuple2<Token, SingleLetDecl>>, 
        a4: Token
    ): LetDecl {
        val declarations = mutableListOf(a2)
        a3.forEach { (_, decl) -> declarations.add(decl) }
        return LetDecl(a1, declarations, a4, a1.location)
    }

    override fun visitSingleLetDecl(
        a1: Token, a2: GenericParams?,
        a3: Tuple3<Token, ParamList?, Token>?,
        a4: Tuple2<Token, TypeExpr>?,
        a5: Token, a6: Expression
    ): SingleLetDecl {
        val paramListWrapper = if (a3 != null) {
            ParamListWrapper(a3.a, a3.b, a3.c, a3.a.location)
        } else {
            null
        }

        val typeAnnotation = if (a4 != null) {
            TypeAnnotation(a4.a, a4.b, a4.a.location)
        } else {
            null
        }

        return SingleLetDecl(a1, a2, paramListWrapper, typeAnnotation, a5, a6, a1.location)
    }

    override fun visitExpression(a1: PrimaryExpr, a2: List<ExpressionSuffix>): Expression {
        return CompoundExpression(a1, a2, a1.position)
    }

    override fun visitExpressionSuffix1(a: ApplicationSuffix): ExpressionSuffix = a

    override fun visitExpressionSuffix2(a: AccessSuffix): ExpressionSuffix = a

    override fun visitApplicationSuffix(a1: Token, a2: ExpressionList?, a3: Token): ApplicationSuffix {
        return ApplicationSuffix(a1, a2, a3, a1.location)
    }

    override fun visitAccessSuffix(a1: Token, a2: Token): AccessSuffix {
        return AccessSuffix(a1, a2, a1.location)
    }

    override fun visitPrimaryExpr1(a: Lambda): PrimaryExpr = a

    override fun visitPrimaryExpr2(a: ConstExpr): PrimaryExpr = a

    override fun visitPrimaryExpr3(a: MatchExpr): PrimaryExpr = a

    override fun visitPrimaryExpr4(a: RecordLiteral): PrimaryExpr = a

    override fun visitPrimaryExpr5(a: Token): PrimaryExpr {
        // Type identifier (uppercase)
        return UpperIdentifierExpr(a, a.location)
    }

    override fun visitPrimaryExpr6(a: Token): PrimaryExpr {
        // Variable reference (lowercase)
        return LowerIdentifierExpr(a, a.location)
    }

    override fun visitPrimaryExpr7(a: LiteralExpr): PrimaryExpr = a

    override fun visitPrimaryExpr8(a: Parens): PrimaryExpr = a

    override fun visitLambda(
        a1: Token, a2: Token, a3: ParamList?, a4: Token, a5: Token, a6: Expression
    ): Lambda {
        return Lambda(a1, a2, a3, a4, a5, a6, a1.location)
    }

    override fun visitParamList(a1: Param, a2: List<Tuple2<Token, Param>>): ParamList {
        val otherParams = a2.map { (token, param) -> Pair(token, param) }
        return ParamList(a1, otherParams, a1.position)
    }

    override fun visitParam(a1: Token, a2: Tuple2<Token, TypeExpr>?): Param {
        val typeAnnotation = if (a2 != null) {
            TypeAnnotation(a2.a, a2.b, a2.a.location)
        } else {
            null
        }
        return Param(a1, typeAnnotation, a1.location)
    }

    override fun visitConstExpr(
        a1: Token, a2: Token, a3: Token, a4: Expression, a5: Token, a6: Expression
    ): ConstExpr {
        return ConstExpr(a1, a2, a3, a4, a5, a6, a1.location)
    }

    override fun visitMatchExpr(
        a1: Token, a2: Expression, a3: Token, a4: MatchArm, a5: List<MatchArm>, a6: Token
    ): MatchExpr {
        return MatchExpr(a1, a2, a3, a4, a5, a6, a1.location)
    }

    override fun visitMatchArm(a1: Token, a2: Pattern, a3: Token, a4: Expression): MatchArm {
        return MatchArm(a1, a2, a3, a4, a1.location)
    }

    override fun visitPattern1(a: RecordPattern): Pattern = a

    override fun visitPattern2(a: IdentifierPattern): Pattern = a

    override fun visitPattern3(a: LiteralExpr): Pattern {
        return when (a) {
            is StringLiteralExpr -> StringLiteralPattern(a.token, a.position)
            is IntegerLiteralExpr -> IntegerLiteralPattern(a.token, a.position)
            is TrueLiteralExpr -> TrueLiteralPattern(a.token, a.position)
            is FalseLiteralExpr -> FalseLiteralPattern(a.token, a.position)
        }
    }

    override fun visitIdentifierPattern1(a1: Token, a2: PatternArgs?): IdentifierPattern {
        return ConstructorPattern(a1, a2, a1.location)
    }

    override fun visitIdentifierPattern2(a: Token): IdentifierPattern {
        return VariablePattern(a, a.location)
    }

    override fun visitPatternArgs(a1: Token, a2: PatternFields?, a3: Token): PatternArgs {
        return PatternArgs(a1, a2, a3, a1.location)
    }

    override fun visitPatternFields(a1: Pattern, a2: List<Tuple2<Token, Pattern>>): PatternFields {
        val otherPatterns = a2.map { (token, pattern) -> Pair(token, pattern) }
        return PatternFields(a1, otherPatterns, a1.position)
    }

    override fun visitRecordPattern(
        a1: Token, a2: Token, a3: FieldPatternList?, a4: Token
    ): RecordPattern {
        return RecordPattern(a1, a2, a3, a4, a1.location)
    }

    override fun visitFieldPatternList(
        a1: FieldPattern, a2: List<Tuple2<Token, FieldPattern>>
    ): FieldPatternList {
        val otherFields = a2.map { (token, field) -> Pair(token, field) }
        return FieldPatternList(a1, otherFields, a1.position)
    }

    override fun visitFieldPattern(a1: Token, a2: Token, a3: Pattern): FieldPattern {
        return FieldPattern(a1, a2, a3, a1.location)
    }

    override fun visitExpressionList(
        a1: Expression, a2: List<Tuple2<Token, Expression>>
    ): ExpressionList {
        val otherExprs = a2.map { (token, expr) -> Pair(token, expr) }
        return ExpressionList(a1, otherExprs, a1.position)
    }

    override fun visitRecordLiteral(
        a1: Token, a2: Token, a3: FieldList?, a4: Token
    ): RecordLiteral {
        return RecordLiteral(a1, a2, a3, a4, a1.location)
    }

    override fun visitFieldList(a1: Field, a2: List<Tuple2<Token, Field>>): FieldList {
        val otherFields = a2.map { (token, field) -> Pair(token, field) }
        return FieldList(a1, otherFields, a1.position)
    }

    override fun visitField(a1: Token, a2: Token, a3: Expression): Field {
        return Field(a1, a2, a3, a1.location)
    }

    override fun visitParens(a1: Token, a2: Expression, a3: Token): Parens {
        return Parens(a1, a2, a3, a1.location)
    }

    override fun visitLiteral1(a: Token): LiteralExpr {
        return StringLiteralExpr(a, a.location)
    }

    override fun visitLiteral2(a: Token): LiteralExpr {
        return IntegerLiteralExpr(a, a.location)
    }

    override fun visitLiteral3(a: Token): LiteralExpr {
        return TrueLiteralExpr(a, a.location)
    }

    override fun visitLiteral4(a: Token): LiteralExpr {
        return FalseLiteralExpr(a, a.location)
    }

    override fun visitTypeExpr(a: FunctionType): TypeExpr = a

    override fun visitFunctionType(
        a1: UnionType, a2: List<Tuple2<Token, UnionType>>
    ): FunctionType {
        val arrows = a2.map { (token, type) -> Pair(token, type) }
        return FunctionType(a1, arrows, a1.position)
    }

    override fun visitUnionType(
        a1: IntersectionType, a2: List<Tuple2<Token, IntersectionType>>
    ): UnionType {
        val otherTypes = a2.map { (token, type) -> Pair(token, type) }
        return UnionType(a1, otherTypes, a1.position)
    }

    override fun visitIntersectionType(
        a1: PrimaryType, a2: List<Tuple2<Token, PrimaryType>>
    ): IntersectionType {
        val otherTypes = a2.map { (token, type) -> Pair(token, type) }
        return IntersectionType(a1, otherTypes, a1.position)
    }

    override fun visitPrimaryType1(a: TypeNameType): PrimaryType = a

    override fun visitPrimaryType2(a: RecordType): PrimaryType = a

    override fun visitPrimaryType3(a: ParensType): PrimaryType = a

    override fun visitPrimaryType4(a: LiteralExpr): PrimaryType {
        return when (a) {
            is StringLiteralExpr -> StringLiteralType(a.token, a.position)
            is IntegerLiteralExpr -> IntegerLiteralType(a.token, a.position)
            is TrueLiteralExpr -> TrueLiteralType(a.token, a.position)
            is FalseLiteralExpr -> FalseLiteralType(a.token, a.position)
        }
    }

    override fun visitTypeNameType(a1: TypeName, a2: TypeArgs?): TypeNameType {
        return TypeNameType(a1, a2, a1.position)
    }

    override fun visitTypeArgs(
        a1: Token, a2: TypeExpr, a3: List<Tuple2<Token, TypeExpr>>, a4: Token
    ): TypeArgs {
        val otherArgs = a3.map { (token, type) -> Pair(token, type) }
        return TypeArgs(a1, a2, otherArgs, a4, a1.location)
    }

    override fun visitRecordType(
        a1: Token, a2: Token, a3: TypeFieldList?, a4: Token
    ): RecordType {
        return RecordType(a1, a2, a3, a4, a1.location)
    }

    override fun visitTypeFieldList(
        a1: TypeField, a2: List<Tuple2<Token, TypeField>>
    ): TypeFieldList {
        val otherFields = a2.map { (token, field) -> Pair(token, field) }
        return TypeFieldList(a1, otherFields, a1.position)
    }

    override fun visitTypeField(a1: Token, a2: Token, a3: TypeExpr): TypeField {
        return TypeField(a1, a2, a3, a1.location)
    }

    override fun visitParensType(a1: Token, a2: TypeExpr, a3: Token): ParensType {
        return ParensType(a1, a2, a3, a1.location)
    }
}
