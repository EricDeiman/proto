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

import java.io.FileOutputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.Labeller;
import common.Version;

import parser.MinefieldBaseVisitor;
import parser.MinefieldParser;

public class Compile extends MinefieldBaseVisitor< Object >
                     implements Version {
    public Compile() {
        code = new StringBuffer();
        constantPool = new HashMap<String, String>();
        labelMaker = new Labeller( ".string" );

        percentD = labelMaker.make();
        constantPool.put( percentD, "%d".intern() );
    }

    @Override
    public Object visitProg( MinefieldParser.ProgContext ctx ) {

        code.append( String.format( "\t.ident \"%s build %d\"\n", name, build ) );
        code.append( "\t.text\n\t.globl main\n\nmain:\n" );

        for( var ectx : ctx.expr() ) {
            visit( ectx );
        }

        code.append( "\tret" );

        // Dump the string pool past the end of the executable code
        code.append( "\n\n\t.section .rodata\n" );
        for( var key : constantPool.keySet() ) {
            var s = constantPool.get( key );
            if( s == "\n" || s == "\r" ) {
                code.append( String.format ("%s:\n\t.byte 0x%x\n",
                                            key,
                                            (byte)s.charAt( 0 ) ) );
            }
            else {
                code.append( key + ":\n\t.string \"" + s + "\"\n" );
            }
        }

        return null;
    }

    @Override
    public Object visitPrintInt(MinefieldParser.PrintIntContext ctx) {
        var value = ctx.INTEGER().getText().replace( "_", "" );

        code.append( "\tmovl $" + value.toString() + ", %esi\n" )
            .append( "\tleaq " + percentD + "(%rip), %rdi\n" )
            .append( "\tmovl $0, %eax\n" )
            .append( "\tcall printf@PLT\n" );

        return null;
    }

    @Override
    public Object visitPrintStr(MinefieldParser.PrintStrContext ctx) {
        var value = ctx.STRING().getText();
        value = value.substring( 1 );
        value = value.substring( 0, value.length() - 1 );

        var label = constantLookUp( value );

        code.append( "\tleaq " + label + "(%rip), %rdi\n" )
            .append( "\tmovl $0, %eax\n" )
            .append( "\tcall printf@PLT\n" );

        return null;
    }

    @Override
    public Object visitPrintLn(MinefieldParser.PrintLnContext ctx) {
        code.append( "\tmovl $10, %edi\n" )
            .append( "\tcall putchar@PLT\n" );
        return null;
    }

    public void writeCodeTo(String fileName) {
        try {
            FileOutputStream fo = new FileOutputStream(fileName);
            fo.write( code.toString().getBytes() );
            fo.close();
            return;
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    private String constantLookUp( String value ) {
        value = value.intern();
        String label = null;

        if( !constantPool.containsValue( value ) ) {
            label = labelMaker.make();
            constantPool.put( label, value );
        }
        else {
            for( var key : constantPool.keySet() ) {
                if( constantPool.get( key ) == value ) {
                    label = key;
                }
            }
        }

        return label;
    }

    private StringBuffer code;
    private HashMap<String, String> constantPool;
    private Labeller labelMaker;
    private String percentD;
}
