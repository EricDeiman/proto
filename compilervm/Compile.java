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
import common.ImmInt;
import common.ImmStr;
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

        backPatches.doBackPatches(where, code);

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
        var value = Integer.parseInt( ctx.INTEGER().getText().replace( "_", "" ) );
        code.writeByte( ByteCodes.Codes.Push )
            .writeInteger( value )
            .writeByte( ByteCodes.Codes.Push )
            .writeInteger( RunTimeTypes.iInteger.ordinal() );
        return null;
    }

    @Override
    public Object visitImmStr( MinefieldParser.ImmStrContext ctx ) {
        var value = ctx.STRING().getText();
        value = value.substring( 1, value.length() - 1 ).intern();

        String label = null;
        if( !constantPool.containsValue( value ) ) {
            label = labelMaker.make( "string" );
            constantPool.put( label, value );
        }
        else {
            for( var key : constantPool.keySet() ) {
                if( constantPool.get( key ) == value ) {
                    label = key;
                }
            }
        }

        code.writeByte( ByteCodes.Codes.Push );
        backPatches.addBackPatch( label, code.getPointer() );
        code.writeInteger( 0 )
            .writeByte( ByteCodes.Codes.Push )
            .writeInteger( RunTimeTypes.iString.ordinal() );

        return null;
    }

    @Override
    public Object visitPrintLn(MinefieldParser.PrintLnContext ctx) {
        code.writeByte( ByteCodes.Codes.PrintLn );

        return null;
    }

    public void writeCodeTo(String fileName) {
        code.writeTo(fileName);
    }

    private VMImageBuffer code;
    private HashMap<String, Integer> where;
    private BackPatch backPatches;
    private HashMap<String, String> constantPool;
    private Labeller labelMaker;
}
