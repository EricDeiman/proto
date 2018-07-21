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

package common;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import parser.MinefieldParser;
import parser.MinefieldBaseVisitor;

public class FreeInVisitor extends MinefieldBaseVisitor< List< String > > {

    @Override
    public List< String > visitPrintExpr( MinefieldParser.PrintExprContext ctx ) {
        return visit( ctx.expr() );
    }

    @Override
    public List< String > visitPrintLn( MinefieldParser.PrintLnContext ctx) {
        return new ArrayList< String >();
    }

    @Override
    public List< String > visitImmInt( MinefieldParser.ImmIntContext ctx ) {
        return new ArrayList< String >();
    }

    @Override
    public List< String > visitImmStr( MinefieldParser.ImmStrContext ctx ) {
        return new ArrayList< String >();
    }

    @Override
    public List< String > visitArithGroup( MinefieldParser.ArithGroupContext ctx ) {
        return visit( ctx.arithExpr() );
    }

    private List< String > merge( List< String > first, List< String > second ) {
        for( var e : second ) {
            if( !first.contains( e ) ) {
                first.add( e );
            }
        }

        return first;
    }

    private List< String >visitLeftRight( MinefieldParser.ArithExprContext leftCtx,
                                          MinefieldParser.ArithExprContext rightCtx ) {
        var left = visit( leftCtx );
        var right = visit( rightCtx );
        
        return merge( left, right );
    }

    private List< String >visitLeftRight( MinefieldParser.CompExprContext leftCtx,
                                          MinefieldParser.CompExprContext rightCtx ) {
        var left = visit( leftCtx );
        var right = visit( rightCtx );
        
        return merge( left, right );
    }

    @Override
    public List< String > visitPower( MinefieldParser.PowerContext ctx ) {
        return visitLeftRight( ctx.left, ctx.right );
    }

    @Override
    public List< String > visitMulti( MinefieldParser.MultiContext ctx ) {
        return visitLeftRight( ctx.left, ctx.right );
    }

    @Override
    public List< String > visitAddi( MinefieldParser.AddiContext ctx ) {
        return visitLeftRight( ctx.left, ctx.right );
    }

    @Override
    public List< String > visitCompGroup( MinefieldParser.CompGroupContext ctx ) {
        return visit( ctx.compExpr() );
    }
    
    @Override
    public List< String > visitCompOp( MinefieldParser.CompOpContext ctx ) {
        return visitLeftRight( ctx.left, ctx.right );
    }

    @Override
    public List< String > visitCompInt( MinefieldParser.CompIntContext ctx ) {
        return new ArrayList< String >();
    }

    @Override
    public List< String > visitCompStr( MinefieldParser.CompStrContext ctx ) {
        return new ArrayList< String >();
    }

    @Override
    public List< String > visitIfExpr( MinefieldParser.IfExprContext ctx ) {
        var test = visit( ctx.compExpr() );
        var consq = visit( ctx.expr( 0 ) );
        test = merge( test, consq );
        
        var alt = visit( ctx.expr( 1 ) );
        return merge( test, alt );
    }

    @Override
    public List< String > visitLetExp( MinefieldParser.LetExpContext ctx ) {
        stack.addFirst( new ArrayList< String >() );
        var st = stack.peekFirst();

        for( var id : ctx.ID() ) {
            st.add( id.getText() );
        }

        var masterList = new ArrayList< String >();

        for( var e : ctx.expr() ) {
            var free = visit( e );
            masterList = ( ArrayList< String >)merge( masterList, free );
        }

        stack.removeFirst();

        return masterList;
    }

    @Override
    public List< String > visitIdExpr( MinefieldParser.IdExprContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public List< String > visitArithId( MinefieldParser.ArithIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public List< String > visitCompId( MinefieldParser.CompIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    private boolean free( String id ) {
        for( var t : stack ) {
            if( t.contains( id ) ) {
                return false;
            }
        }
        return true;
    }

    private List< String > visitId( String id ) {
        var rtn = new ArrayList< String >();

        if( free( id ) ) {
            rtn.add( id );
        }

        return rtn;
    }

    private Deque< List< String > > stack = new ArrayDeque< List< String > >();
}
