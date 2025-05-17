package com.ucle

import com.ucle.ast.AstBuilder
import com.ucle.ast.Program
import com.ucle.parser.Parser
import com.ucle.parser.Scanner
import java.io.StringReader

fun parse(scanner: Scanner): Program {
        return Parser(scanner, AstBuilder()).program()
}

fun parse(input: String): Program =
    parse(Scanner(StringReader(input)))
