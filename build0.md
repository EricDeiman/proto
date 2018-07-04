# Minefield Build Zero
This build is mostly infrastructure setup.  

### common
This directory contains files that are useful in several other part of the system.

### compilerasm
This directory contains the files to compile Minefiled to x86-64 to (gas-style) assembly
code.

### compilevm
This directory contains the files to compile Minefiled to binary-encoded custom virtual
machine byte codes (_e.g._, bitcodes).

### disassembler
This directory contains the files to print Minefiled object files as a somewhat readable
assembly code.  

### interpreter
This directry contains the files to create the Minefield tree walking interpreter.

### parser
This directory contains the ANTLR grammar for Minefiled.

### virtualmachine
This directory contains the code for the virtrual machine interpreter.
