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
import common.RunTimeTypes;
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
        
        // We use register r9 to hold the value stack 
        code.append( "\tpushq %rbp\n" )
            .append( "\tmovq %rsp, %rbp\n" )
            .append( "\tsubq $8, %rsp\n")
            .append( "\tmovl	$1024, %edi\n" )
            .append( "\tcall	mkStack@PLT\n")
            .append( "\tmovq	%rax, -8(%rbp)\n\n" );

        for( var ectx : ctx.specialForm() ) {
            visit( ectx );
        }

        code.append( "\tleaveq\n" )
            .append( "\tretq\n" );

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
    public Object visitPrintExpr( MinefieldParser.PrintExprContext ctx ) {
        visit( ctx.expr() );
        code.append( "\tmovq -8(%rbp), %rdi\n" )
            .append( "\tcall printTos@PLT\n\n" );

        return null;
    }

    @Override
    public Object visitImmInt( MinefieldParser.ImmIntContext ctx ) {
        var value = Integer.parseInt( ctx.INTEGER().getText().replace( "_", "" ) );
        code.append( "\tmovq -8(%rbp), %rdi\n" )
            .append( "\tmovq $" + value + ", %rsi\n" ) // push the value
            .append( "\tcall push@PLT\n" )
            .append( "\tmovq $" + RunTimeTypes.iInteger.ordinal() + ", %rsi\n" )  // push the type
            .append( "\tcall push@PLT\n\n" );

        return null;
    }

    @Override
    public Object visitImmStr(MinefieldParser.ImmStrContext ctx) {
        var value = ctx.STRING().getText();
        value = value.substring( 1, value.length() - 1 ).intern();

        var label = constantLookUp( value );

        code.append( "\tmovq -8(%rbp), %rdi\n" )
            .append( "\tleaq " + label + "(%rip), %rsi\n" )
            .append( "\tcall push@PLT\n" )
            .append( "\tmovq $" + RunTimeTypes.iString.ordinal() + ", %rsi\n" )  // push the type
            .append( "\tcall push@PLT\n\n" );

        return null;
    }

    @Override
    public Object visitPrintLn(MinefieldParser.PrintLnContext ctx) {
        code.append( "\tmovq $0, %rax\n" )
            .append( "\tmovl $10, %edi\n" )
            .append( "\tcall putchar@PLT\n\n" );
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
