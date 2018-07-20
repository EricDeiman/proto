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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Version;

import parser.MinefieldParser;
import parser.MinefieldBaseVisitor;

public class MinefieldInterpreter extends MinefieldBaseVisitor< InterpValue >
                                  implements Version {

    @Override
    public InterpValue visitProg( MinefieldParser.ProgContext ctx ) {

        for( var ectx : ctx.expr() ) {
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

    @Override
    public InterpValue visitIfExpr( MinefieldParser.IfExprContext ctx ) {
        var test = ( InterpBool )visit( ctx.compExpr() );
        var exprs = ctx.expr();

        if( test.get() == true ) {
            return visit( exprs.get( 0 ) );
        }
        else {
            return visit( exprs.get( 1 ) );
        }
    }

    private List< Pair< String, List< String > > >
    dependency( MinefieldParser.LetExpContext ctx ) {
        var freeIn = new FreeInVisitor();
        var map = new ArrayList< Pair< String, List< String > > >();

        for( int i = 0; i < ctx.ID().size(); i++ ) {
            var id = ctx.ID( i ).getText();
            var free = freeIn.visit( ctx.expr( i ) );
            map.add( new Pair< String, List< String > >( id, free ) );
        }

        return map;
    }

    private List< Pair< String, List< String > > >
    getNoFanIn( List< Pair< String, List< String > > > g ) {
        return g.stream().filter( v -> v.getSecond().size() == 0 )
            .collect( ArrayList< Pair< String, List< String > > >::new,
                      ArrayList< Pair< String, List< String > > >::add,
                      ArrayList< Pair< String, List< String > > >::addAll );
    }

    private List< Pair< String, List< String > > >
    getFanIn( List< Pair< String, List< String > > > g ) {
        return g.stream().filter( v -> v.getSecond().size() != 0 )
            .collect( ArrayList< Pair< String, List< String > > >::new,
                      ArrayList< Pair< String, List< String > > >::add,
                      ArrayList< Pair< String, List< String > > >::addAll );
    }

    private List< String > tsort( List< Pair< String, List< String > > > graph ) {
        var s = getNoFanIn( graph );
        var g = getFanIn( graph );
        var e = new ArrayList< String >();

        while( 0 < s.size() ) {
            var node = s.remove( 0 );
            var n = node.getFirst();
            e.add( n );
            for( var m : g ) {
                if( m.getSecond().contains( n ) ) {
                    m.getSecond().remove( n );
                }
            }
            var s1 = getNoFanIn( g );
            g = getFanIn( g ); 
            s.addAll( s1 );
        }

        if( g.size() != 0 ) {
            throw new Error( "cycle deteted in let expression initializers" );
        }

        return e;
    }

    @Override
    public InterpValue visitLetExp( MinefieldParser.LetExpContext ctx ) {
        var map = dependency( ctx );

        var theseIds = new ArrayList< String >();
        for( var s : ctx.ID() ) {
            theseIds.add( s.getText() );
        }

        var fromRemove = new ArrayList< Pair<String, String> >();
        for( var e : map ) {
            var edges = e.getSecond();
            for( var x : edges ) {
                if( !theseIds.contains( x ) ) {
                    fromRemove.add( new Pair< String, String >( e.getFirst(), x ) );
                }
            }
        }

        for( var d : fromRemove ) {
            for( var m : map ) {
                if( d.getFirst() == m.getFirst() ) {
                    m.getSecond().remove( d.getSecond() );
                }
            }
        }

        var initOrder = tsort( map );

        tables.push();
        var st = tables.peek();
        int idCount = ctx.ID().size();

        var lookup = new HashMap< String, MinefieldParser.ExprContext>();

        for( int i = 0; i < idCount; i++ ) {
            var name = ctx.ID( i ).getText();
            var value = ctx.expr( i );
            lookup.put( name, value );
        }

        for( var i : initOrder ) {
            var name = i;
            var value = visit( lookup.get( i ) );
            st.add( name, value );
        }

        InterpValue rtn = null;
        for( int j = idCount; j < ctx.expr().size(); j++ ) {
            rtn = visit( ctx.expr( j ) );
        }
        tables.pop();
        return rtn;
    }

    @Override
    public InterpValue visitIdExpr( MinefieldParser.IdExprContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public InterpValue visitArithId( MinefieldParser.ArithIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public InterpValue visitCompId( MinefieldParser.CompIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    private InterpValue visitId( String id ) {
        if( tables.containsKey( id ) ) {
            return tables.lookUp( id );
        }
        throw new Error( "cannot find symbol " + id );        
    }

    private String stripQuotes( String subject ) {
        return subject.substring( 1, subject.length() - 1 );
    }

    private SymbolTableStack tables = new SymbolTableStack();
}

class Pair< U, V > {
    public Pair( U f, V s) {
        first = f;
        second = s;
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    private U first;
    private V second;
}
