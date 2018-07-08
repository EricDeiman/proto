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

public class MinefieldInterpreter extends MinefieldBaseVisitor< InterpValue >
                                  implements Version {

    @Override
    public InterpValue visitProg( MinefieldParser.ProgContext ctx ) {

        for( var ectx : ctx.specialForm() ) {
            visit( ectx );
        }
        return null;
    }

    @Override
    public InterpValue visitPrintExpr( MinefieldParser.PrintExprContext ctx ) {
        System.out.print( visit( ctx.expr() ).toString() );

        return null;
    }

    @Override
    public InterpValue visitPrintLn( MinefieldParser.PrintLnContext ctx) {
        System.out.println();

        return null;
    }

    @Override
    public InterpValue visitImmInt( MinefieldParser.ImmIntContext ctx ) {
        return new InterpInt( Integer.parseInt( ctx.INTEGER().getText()
                                                             .replace( "_", "" ) ) );
    }

    @Override
    public InterpValue visitImmStr( MinefieldParser.ImmStrContext ctx ) {
        return new InterpString( stripQuotes( ctx.getText() ) );
    }

    @Override
    public InterpValue visitArithGroup( MinefieldParser.ArithGroupContext ctx ) {
        return visit( ctx.arithExpr() );
    }

    @Override
    public InterpValue visitPower( MinefieldParser.PowerContext ctx ) {
        var left = ( InterpInt )visit( ctx.left );
        var right = ( InterpInt )visit( ctx.right );
        var result = left.math( ctx.op.getText(), right );

        return result;
    }

    @Override
    public InterpValue visitMulti( MinefieldParser.MultiContext ctx ) {
        var left = ( InterpInt )visit( ctx.left );
        var right = ( InterpInt )visit( ctx.right );
        var result = left.math( ctx.op.getText(), right );

        return result;
    }

    @Override
    public InterpValue visitAddi( MinefieldParser.AddiContext ctx ) {
        var left = ( InterpInt )visit( ctx.left );
        var right = ( InterpInt )visit( ctx.right );
        var result = left.math( ctx.op.getText(), right );

        return result;
    }

    @Override
    public InterpValue visitCompGroup( MinefieldParser.CompGroupContext ctx ) {
        return visit( ctx.compExpr() );
    }

    @Override
    public InterpValue visitCompOp( MinefieldParser.CompOpContext ctx ) {
        var left = visit( ctx.left );
        var right = visit( ctx.right );

        boolean result = left.getComp().compare( left, ctx.op.getText(), right );

        return new InterpBool( result );
    }

    @Override
    public InterpValue visitCompInt( MinefieldParser.CompIntContext ctx ) {
        return new InterpInt( Integer.parseInt( ctx.INTEGER().getText()
                                                .replace( "_", "" ) ) );
    }

    @Override
    public InterpValue visitCompStr( MinefieldParser.CompStrContext ctx ) {
        return new InterpString( stripQuotes( ctx.getText() ) );
    }

    private String stripQuotes( String subject ) {
        return subject.substring( 1, subject.length() - 1 );
    }
}

