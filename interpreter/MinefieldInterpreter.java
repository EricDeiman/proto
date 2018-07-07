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

import common.ImmInt;
import common.ImmStr;
import common.MeflExpr;
import common.Version;

import parser.MinefieldParser;
import parser.MinefieldBaseVisitor;

public class MinefieldInterpreter extends MinefieldBaseVisitor< MeflExpr >
                                  implements Version {

    @Override
    public MeflExpr visitProg( MinefieldParser.ProgContext ctx ) {
        printVisitor = new PrintVisitor( System.out );

        for( var ectx : ctx.specialForm() ) {
            visit( ectx );
        }
        return null;
    }

    @Override
    public MeflExpr visitPrintExpr( MinefieldParser.PrintExprContext ctx ) {
        visit( ctx.expr() ).accept( printVisitor );

        return null;
    }

    @Override
    public MeflExpr visitPrintLn( MinefieldParser.PrintLnContext ctx) {
        System.out.println();

        return null;
    }

    @Override
    public MeflExpr visitImmInt( MinefieldParser.ImmIntContext ctx ) {
        return new ImmInt( Integer.parseInt( ctx.INTEGER().getText().replace( "_", "" ) ) );
    }

    @Override
    public MeflExpr visitImmStr( MinefieldParser.ImmStrContext ctx ) {
        return new ImmStr( stripQuotes( ctx.getText() ) );
    }

    private String stripQuotes( String subject ) {
        return subject.substring( 1, subject.length() - 1 );
    }

    private PrintVisitor printVisitor;
}
