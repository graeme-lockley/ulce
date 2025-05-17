package com.ucle

import com.ucle.parser.Scanner
import com.ucle.types.TypeError
import java.io.File
import java.io.StringReader

/**
 * Main entry point for the UCLE compiler.
 */
fun main(args: Array<String>) {
    println("UCLE Compiler - Ready to parse and type-check!")
    
    if (args.isEmpty()) {
        printUsage()
        return
    }
    
    when (args[0]) {
        "--parse" -> {
            if (args.size < 2) {
                println("Error: Missing file to parse")
                printUsage()
                return
            }
            
            val file = File(args[1])
            if (!file.exists()) {
                println("Error: File not found: ${args[1]}")
                return
            }
            
            try {
                val program = Parser.parse(file.readText())
                println("Parsing successful!")
                println(program)
            } catch (e: Exception) {
                println("Parsing error: ${e.message}")
            }
        }
        
        "--type-check", "-t" -> {
            if (args.size < 2) {
                println("Error: Missing file to type-check")
                printUsage()
                return
            }
            
            val file = File(args[1])
            if (!file.exists()) {
                println("Error: File not found: ${args[1]}")
                return
            }
            
            try {
                val types = Parser.getInferredTypes(file.readText())
                println("Type checking successful!")
                println("Inferred types:")
                types.forEach { (name, type) ->
                    println("  $name : $type")
                }
            } catch (e: TypeError) {
                println("Type error: ${e.message}")
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            }
        }
        
        "--interactive", "-i" -> {
            interactiveMode()
        }
        
        else -> {
            println("Unknown command: ${args[0]}")
            printUsage()
        }
    }
}

/**
 * Print usage information.
 */
fun printUsage() {
    println("Usage:")
    println("  ucle --parse <file>        Parse a file and print the AST")
    println("  ucle --type-check <file>   Parse and type-check a file")
    println("  ucle --interactive         Start interactive mode")
}

/**
 * Interactive REPL mode.
 */
fun interactiveMode() {
    println("UCLE Interactive Mode")
    println("Type '.exit' to quit, '.help' for help")
    
    val history = mutableListOf<String>()
    val environment = mutableMapOf<String, String>()
    
    while (true) {
        print("> ")
        val input = readLine() ?: break
        
        if (input.isBlank()) continue
        
        when {
            input == ".exit" -> break
            
            input == ".help" -> {
                println("UCLE Interactive Mode Commands:")
                println("  .exit           Exit the REPL")
                println("  .help           Show this help message")
                println("  .history        Show command history")
                println("  .clear          Clear the environment")
                println("  .env            Show current environment")
            }
            
            input == ".history" -> {
                history.forEachIndexed { i, cmd -> println("${i + 1}: $cmd") }
            }
            
            input == ".clear" -> {
                environment.clear()
                println("Environment cleared")
            }
            
            input == ".env" -> {
                if (environment.isEmpty()) {
                    println("Environment is empty")
                } else {
                    println("Environment:")
                    environment.forEach { (name, type) -> println("  $name : $type") }
                }
            }
            
            input.startsWith(".") -> {
                println("Unknown command: $input")
            }
            
            else -> {
                try {
                    val types = Parser.getInferredTypes(input)
                    environment.putAll(types)
                    types.forEach { (name, type) -> println("$name : $type") }
                    history.add(input)
                } catch (e: TypeError) {
                    println("Type error: ${e.message}")
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
        }
    }
    
    println("Goodbye!")
}
