/*
  The minefield programming language
  Copyright 2018 Eric J. Deiman

  This file is part of the minefield programming language.

  The minefield programming language is free software: you can redistribute it
  and/ormodify it under the terms of the GNU General Public License as published by the
  Free Software Foundation, either version 3 of the License, or (at your option) any
  later version.

  The minefield programming language is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with the
  minefield programming language. If not, see <https://www.gnu.org/licenses/>
*/

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

import parser.MinefieldLexer;
import parser.MinefieldParser;

public class Main {
    public static void main(String[] args) throws Exception {
        String inputFileName = null;
        if(args.length > 0) {
            inputFileName = args[0];
        }

        InputStream is = System.in;
        if(inputFileName != null) {
            is = new FileInputStream(inputFileName);
        }

        CharStream input = CharStreams.fromStream(is);
        MinefieldLexer lexer = new MinefieldLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MinefieldParser parser = new MinefieldParser(tokens);

        ParseTree tree = parser.prog();

        if(parser.getNumberOfSyntaxErrors() == 0){
            try {
                MinefieldInterpreter interpreter = new MinefieldInterpreter();
                interpreter.visit(tree);
            }
            catch(Exception err) {
                System.err.println("The program doesn't mean what you think it means: " +
                                   err.getMessage());
            }
        }
        else {
            System.out.println("oops! try again.");
        }
    }
}
