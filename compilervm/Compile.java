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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.BackPatch;
import common.ByteCodes;
import common.Labeller;
import common.RunTimeTypes;
import common.VMImageBuffer;
import common.Version;

import parser.MinefieldBaseVisitor;
import parser.MinefieldParser;

public class Compile extends MinefieldBaseVisitor< Object >
                     implements Version {
    public Compile() {
        code = new VMImageBuffer();
        where = new HashMap<String, Integer>();
        backPatches = new BackPatch();
        constantPool = new HashMap<String, String>();
        labelMaker = new Labeller();
    }

    @Override
    public Object visitProg( MinefieldParser.ProgContext ctx ) {

        // signature
        for( var b : name.getBytes() ) {
            code.writeByte( b );
        }

        // build version
        code.writeInteger( build );

        String constants = labelMaker.make("ConstantPool");
        backPatches.addBackPatch(constants, code.getPointer());
        code.writeInteger(0);

        for( var ectx : ctx.specialForm() ) {
            visit( ectx );
        }
        code.writeByte(ByteCodes.Codes.Halt);

        // Dump the string pool past the end of the executable code
        where.put( constants, code.getPointer() );
        for( var key : constantPool.keySet() ) {
            where.put( key, code.getPointer() );
            code.writeString( constantPool.get( key ) );
            code.writeByte( 0 ); // zero terminate strings in the image
        }

        backPatches.doBackPatches( where, code );

        return null;
    }

    @Override
    public Object visitPrintExpr( MinefieldParser.PrintExprContext ctx ) {
        visit( ctx.expr() );
        code.writeByte( ByteCodes.Codes.Print );

        return null;
    }

    @Override
    public Object visitImmInt( MinefieldParser.ImmIntContext ctx ) {
        return visitInteger( ctx.INTEGER().getText() );
    }

    @Override
    public Object visitImmStr( MinefieldParser.ImmStrContext ctx ) {
        return visitString( ctx.STRING().getText() );
    }

    @Override
    public Object visitPrintLn(MinefieldParser.PrintLnContext ctx) {
        code.writeByte( ByteCodes.Codes.PrintLn );

        return null;
    }

    @Override
    public Object visitPower( MinefieldParser.PowerContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );
        code.writeByte( ByteCodes.Codes.Pow );

        return null;
    }

    @Override
    public Object visitMulti( MinefieldParser.MultiContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );
 
        switch( ctx.op.getText() ) {
        case "*":
            code.writeByte( ByteCodes.Codes.Mul );
            break;
        case "/":
            code.writeByte( ByteCodes.Codes.Div );
            break;
        case "%":
            code.writeByte( ByteCodes.Codes.Rem );
            break;
        }

        return null;
    }

    @Override
    public Object visitAddi( MinefieldParser.AddiContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        switch( ctx.op.getText() ) {
        case "+":
            code.writeByte( ByteCodes.Codes.Add );
            break;
        case "-":
            code.writeByte( ByteCodes.Codes.Sub );
            break;
        }

        return null;
    }

    @Override
    public Object visitCompOp( MinefieldParser.CompOpContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        switch( ctx.op.getText() ) {
        case "<":
            code.writeByte( ByteCodes.Codes.Lt );
            break;
        case "<=":
            code.writeByte( ByteCodes.Codes.Lte );
            break;
        case "?=":
            code.writeByte( ByteCodes.Codes.Eq );
            break;
        case "!=":
            code.writeByte( ByteCodes.Codes.Neq );
            break;
        case ">=":
            code.writeByte( ByteCodes.Codes.Gte );
            break;
        case ">":
            code.writeByte( ByteCodes.Codes.Gt );
            break;
        }

        return null;
    }

    @Override
    public Object visitCompInt( MinefieldParser.CompIntContext ctx ) {
        return visitInteger( ctx.INTEGER().getText() );
    }

    @Override
    public Object visitCompStr( MinefieldParser.CompStrContext ctx ) {
        return visitString( ctx.STRING().getText() );
    }

    @Override
    public Object visitIfExpr( MinefieldParser.IfExprContext ctx ) {
        visit( ctx.compExpr() );

        var branches = ctx.expr();

        var elseBranch = labelMaker.make( "elseBranch" );
        var endIf = labelMaker.make( "endIf" );

        code.writeByte( ByteCodes.Codes.JmpF );
        backPatches.addBackPatch( elseBranch, code.getPointer() );
        code.writeInteger( 0 );

        visit( branches.get( 0 ) );
        code.writeByte( ByteCodes.Codes.Jmp );
        backPatches.addBackPatch( endIf, code.getPointer() );
        code.writeInteger( 0 );

        where.put( elseBranch, code.getPointer() );

        visit( branches.get( 1 ) );

        where.put( endIf, code.getPointer() );

        return null;
    }

    public void writeCodeTo(String fileName) {
        code.writeTo(fileName);
    }

    // --------------------------------------------------------------------------------

    private Object visitInteger( String integer ) {
        var value = Integer.parseInt( integer.replace( "_", "" ) );
        code.writeByte( ByteCodes.Codes.Push )
            .writeInteger( value )
            .writeByte( ByteCodes.Codes.Push )
            .writeInteger( RunTimeTypes.iInteger.ordinal() );

        return null;
    }

    private Object visitString( String string ) {
        var label = stringLiteral( string );

        code.writeByte( ByteCodes.Codes.Push );
        backPatches.addBackPatch( label, code.getPointer() );
        code.writeInteger( 0 )
            .writeByte( ByteCodes.Codes.Push )
            .writeInteger( RunTimeTypes.iString.ordinal() );

        return null;
    }

    private String stringLiteral( String literal ) {
        literal = literal.substring( 1, literal.length() - 1 ).intern();
        String label = null;

        if( !constantPool.containsValue( literal ) ) {
            label = labelMaker.make( "string" );
            constantPool.put( label, literal );
        }
        else {
            for( var key : constantPool.keySet() ) {
                if( constantPool.get( key ) == literal ) {
                    label = key;
                }
            }
        }

        return label;
    }

    private VMImageBuffer code;
    private HashMap<String, Integer> where;
    private BackPatch backPatches;
    private HashMap<String, String> constantPool;
    private Labeller labelMaker;
}
