# Proto
This is the prototype implementation of the Minefield programming language.  This is an expression-based language with a slight SML flavor.

# Directories
The `common` directory holds a number of source files that are used in several of the other parts of the language.

The `compilerasm` directory holds the beginnings of a compiler to AT&T assembly code.

The `compilervm` directory holds the code to generate code for the virtual machine in the `virtualmachine` directory.

The `interpreter` directory holds the code for a tree-walking interpreter.

The `parser` directory holds the ANTLR 4 grammar.

The `tests` directory holds a number of tests for the Minefield programming language.

The `tools` directory holds a number of tools for building code files for the compiler, along with running test and comparing outputs.

The `virtualmachine` holds the code for the virtual machine `compilervm` generates code for.

# Note
This code is originally from 2020.  I'm only now putting it on my GitHub.
