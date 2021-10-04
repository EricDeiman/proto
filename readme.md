# Proto
This is the prototype implementation of my Minefield programming language.

# What
I've build a couple of parsers and tree-walking interpreters, and with this project, I 
want to get familiar with other aspects of compilers, things like generating code and more 
efficient interpretation.  I've designed a small expression-based language, Minefield, and a 
byte-code virtual machine to explore these tasks.

The main pieces are a compiler from Minefield to the bytecodes, along with a virtual machine
to interprete those.

This project also contains a simple tree-walking interpreter.  The main test harness runs
both the tree-walking interpreter along side the VM code and compares the output of each.

# Example
Here's one of the test programs as an example of what the language looks like:
```
let two = one + one
let one = 2 - 1
in
  let seven = four + three
  let three = one + two
  let four = two * two
  in
    print "one is "   print one println
    print "two is "   print two println
    print "three is " print three println
    print "four is "  print four println
    print "seven is " print seven println
  end
  print "one is still " print one println
  print "two is still " print two println
end
```
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
This code is originally from 2018.
