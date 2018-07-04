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
import org.antlr.v4.runtime.tree.*;

import common.Version;
import parser.MinefieldParser;
import parser.MinefieldBaseVisitor;

public class MinefieldInterpreter extends MinefieldBaseVisitor<Object>
                                  implements Version {

    @Override
    public Object visitProg( MinefieldParser.ProgContext ctx ) {
        for( var ectx : ctx.expr() ) {
            visit( ectx );
        }
        return null;
    }

    @Override
    public Object visitPrintInt( MinefieldParser.PrintIntContext ctx ) {
        var value = Integer.valueOf( ctx.INTEGER().getText().replace( "_", "" ) );
        System.out.print( value );

        return null;
    }

    @Override
    public Object visitPrintStr( MinefieldParser.PrintStrContext ctx ) {
        var output = ctx.STRING().getSymbol().getText().replace("\"", "");
        System.out.print( output );

        return null;
    }

    @Override
    public Object visitPrintLn( MinefieldParser.PrintLnContext ctx) {
        System.out.println();

        return null;
    }
}
