/*
  The minefield programming language
  Copyright 2018 Eric J. Deiman

  This file is part of the minefield programming language.

  The minefield programming language is free software: you can redistribute it
  and/ormodify it under the terms of the GNU General Public License as published
  by the Free Software Foundation, either version 3 of the License, or (at your
  option) any later version.

  The minefield programming language is distributed in the hope that it will be
  useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  Public License for more details.

  You should have received a copy of the GNU General Public License along with
  the minefield programming language. If not, see
  <https://www.gnu.org/licenses/>
*/

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileOutputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.Environment;
import common.Labeller;
import common.RunTimeTypes;
import common.Utils;
import common.Version;

import parser.MinefieldBaseVisitor;
import parser.MinefieldParser;

public class Compile extends MinefieldBaseVisitor< Object >
                     implements Version {

    public Compile() {
        code = new StringBuffer();
        constantPool = new HashMap<String, String>();
        labelMaker = new Labeller( ".string" );
        environment = new Environment();

        percentD = labelMaker.make();
        constantPool.put( percentD, "%d".intern() );

        constantPool.put( labelMaker.make(), "<".intern() );
        constantPool.put( labelMaker.make(), "<=".intern() );
        constantPool.put( labelMaker.make(), "?=".intern() );
        constantPool.put( labelMaker.make(), "!=".intern() );
        constantPool.put( labelMaker.make(), ">=".intern() );
        constantPool.put( labelMaker.make(), ">".intern() );

        howManyRuntimeStacks = 2;
    }

    @Override
    public Object visitProg( MinefieldParser.ProgContext ctx ) {

        code.append( String.format( "\t.ident \"%s build %d\"\n",
                                    name,
                                    build ) );
        code.append( "\t.text\n\t.globl main\n\nmain:\n" );
        
        code.append( "\tpushq %rbp\n" )
            .append( "\tmovq %rsp, %rbp\n" )
            .append( "\tsubq $" + (8 + howManyRuntimeStacks * 8) + ", %rsp\n")
            .append( "\tmovl $1024, %edi\n" )
            .append( "\tcall mkStack@PLT\n")
            .append( "\tmovq %rax, " + valueStack + "\n\n" )
            .append( "\tmovl $1024, %edi\n" )
            .append( "\tcall mkStack@PLT\n")
            .append( "\tmovq %rax, " + frameBaseStack + "\n\n" );


        for( var ectx : ctx.expr() ) {
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

        code.append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tcall printTos@PLT\n\n" );

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
    public Object visitPrintLn( MinefieldParser.PrintLnContext ctx ) {
        code.append( "\tmovl $10, %edi\n" )
            .append( "\tcall putchar@PLT\n\n" );
        return null;
    }

    @Override
    public Object visitPower( MinefieldParser.PowerContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        code.append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tcall meflPow@PLT\n\n" );

        return null;
    }

    @Override
    public Object visitMulti( MinefieldParser.MultiContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        code.append( "\tmovq " + valueStack + ", %rdi\n" );

        switch( ctx.op.getText() ) {
        case "*":
            code.append( "\tcall meflMul@PLT\n\n" );
            break;
        case "/":
            code.append( "\tcall meflDiv@PLT\n\n" );
            break;
        case "%":
            code.append( "\tcall meflRem@PLT\n\n" );
            break;
        }

        return null;
    }

    @Override
    public Object visitAddi( MinefieldParser.AddiContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        code.append( "\tmovq " + valueStack + ", %rdi\n" );

        switch( ctx.op.getText() ) {
        case "+":
            code.append( "\tcall meflAdd@PLT\n\n" );
            break;
        case "-":
            code.append( "\tcall meflSub@PLT\n\n" );
            break;
        }

        return null;
    }

    @Override
    public Object visitCompOp( MinefieldParser.CompOpContext ctx ) {
        visit( ctx.left );
        visit( ctx.right );

        code.append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tleaq " + constantLookUp( ctx.op.getText() )  +
                     "(%rip), %rsi\n" )
            .append( "\tcall meflCompare@PLT\n\n" );

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

        var notBoolean = labelMaker.make( "notBoolean" );
        var endBooleanIf = labelMaker.make( "endBooleanIf" );

        var falseBlock = labelMaker.make( "falseBlock" );
        var endTest = labelMaker.make( "endTest" );

        // Check to see if top of stack is a boolean type
        code.append( "\tmovq " + valueStack + ", %rax\n" )
            .append( "\tmovl 4(%rax), %edx\n" )
            .append( "\tleal -1(%rdx), %edx\n")
            .append( "\tmovslq %edx, %rdx\n" )
            .append( "\tmovq 8(%rax,%rdx,8), %rax\n" )
            .append( "\tcmpq $3, %rax\n" )
            .append( "\tjne " + notBoolean + "\n" )
            // Check to see if top of stack value is true
            .append( "\tmovq " + valueStack + ", %rax\n" )
            .append( "\tmovl 4(%rax), %edx\n" )
            .append( "\tleal -2(%rdx), %edx\n")
            .append( "\tmovslq %edx, %rdx\n" )
            .append( "\tmovq " + valueStack + ", %rax\n" )
            .append( "\tmovq 8(%rax,%rdx,8), %rax\n" )
            .append( "\ttestq %rax, %rax\n" )
            .append( "\tje " + falseBlock + "\n" )
            .append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tcall pop@PLT\n" )
            .append( "\tcall pop@PLT\n" );

        visit( ctx.expr( 0 ) );

        code.append( "\tjmp " + endTest + "\n" )
            .append( falseBlock + ":\n" )
            .append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tcall pop@PLT\n" )
            .append( "\tcall pop@PLT\n" );

        visit( ctx.expr( 1 ) );

        code.append( endTest + ":\n" )
            .append( "\tjmp " + endBooleanIf + "\n" )
            .append( notBoolean + ":\n" )
            .append( "\tcall notBoolean@PLT\n" )
            .append( endBooleanIf + ":\n\n" );

        return null;
    }

    @Override
    public Object visitLetExp( MinefieldParser.LetExpContext ctx ) {
        var graph = Utils.dependency( ctx );
        var initOrder = graph.tsort();

        if( initOrder.size() == 0 ) {
            throw new Error( "cycle detected in let bindings" );
        }
        
        var idCount = initOrder.size();

        var lookup = new HashMap< String, MinefieldParser.ExprContext>();
        for( int i = 0; i < idCount; i++ ) {
            var name = ctx.ID( i ).getText();
            var value = ctx.expr( i );
            lookup.put( name, value );
        }

        // Push the index to the top of the value stack onto the framebase
        // stack
        code.append( "# let " + ctx.ID( 0 ).getText() + "\n" );
        code.append( "\tmovq	" + valueStack + ", %rax  # valueStack \n" )
            .append( "\tmovl	4(%rax), %eax\n" ) 
            .append( "\tmovslq %eax, %rdx\n" )
            .append( "\tmovq " + frameBaseStack + ", %rax  # frameBaseStack\n" )
            .append( "\tmovq	%rdx, %rsi\n" )
            .append( "\tmovq	%rax, %rdi\n" )
            .append( "\tcall push@PLT\n\n" );
        environment.enter();

        for( var i = 0; i < idCount; i++ ) {
            environment.set( initOrder.get( i ), i );
        }

        code.append( "# initializers\n" );
        for( var n : initOrder ) {
            code.append( "\n# " + n + "\n" );
            visit( lookup.get( n ) );
        }

        code.append( "# body\n" );
        for( int j = idCount; j < ctx.expr().size(); j++ ) {
            code.append( "\n# " + ctx.getText() + "\n" );
           visit( ctx.expr( j ) );
        }

        environment.leave();
        // Pop the framebase stack
        code.append( "\tmovq	" + frameBaseStack + ", %rdi  # frameBaseStack\n" )
            .append( "\tcall pop@PLT\n\n" );

        return null;
    }

    @Override
    public Object visitIdExpr( MinefieldParser.IdExprContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public Object visitArithId( MinefieldParser.ArithIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
    }

    @Override
    public Object visitCompId( MinefieldParser.CompIdContext ctx ) {
        var id = ctx.ID().getText();
        return visitId( id );
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

    // ----------------------------------------------------------------------

    private Object visitId( String id ) {
        if( environment.containsKey( id ) ) {
            var result = environment.lookUp( id );
            var coord = result.getElement();
            code.append( "# lookup " + id + " value\n" );
            code.append( "\tmovq " + frameBaseStack + ", %rax  # frameBaseStack\n")
                .append( "\tmovl 4(%rax), %eax\n")
                .append( "\tleal -" + (1 + coord.first()) + "(%rax), %edx # coord.first() = " + coord.first() + "\n")
                .append( "\tmovq " + frameBaseStack + ", %rax  # frameBaseStack\n")
                .append( "\tmovslq %edx, %rdx\n")
                .append( "\tmovq 8(%rax, %rdx, 8), %rax\n")
                .append( "\taddl $" + ( 2 * coord.second() ) + ",%eax  # coord.second() = " + coord.second() + " \n")
                .append( "\tmovl %eax, %edx\n")
                .append( "\tmovq " + valueStack + ",%rax  # valueStack\n")
                .append( "\tmovl %edx, %esi\n")
                .append( "\tmovq %rax, %rdi\n")
                .append( "\tcall get@PLT\n")
                .append( "\tmovq %rax, %rsi\n")
                .append( "\tmovq " + valueStack + ", %rdi  # valueStack\n" )
                .append( "\tcall push@PLT\n");

            code.append( "# lookup " + id + " type\n" );
            code.append( "\tmovq " + frameBaseStack + ", %rax  # frameBaseStack\n")
                .append( "\tmovl 4(%rax), %eax\n")
                .append( "\tleal -" + (1 + coord.first()) + "(%rax), %edx  # coord.first() = " + coord.first() + "\n")
                .append( "\tmovq " + frameBaseStack + ", %rax  # frameBaseStack\n")
                .append( "\tmovslq %edx, %rdx\n")
                .append( "\tmovq 8(%rax, %rdx, 8), %rax\n")
                .append( "\taddl $" + ( 2 * coord.second() + 1 ) + ",%eax  # coord.second() = " + coord.second() + " \n")
                .append( "\tmovl %eax, %edx\n")
                .append( "\tmovq " + valueStack + ",%rax  # valueStack\n")
                .append( "\tmovl %edx, %esi\n")
                .append( "\tmovq %rax, %rdi\n")
                .append( "\tcall get@PLT\n")
                .append( "\tmovq %rax, %rsi\n")
                .append( "\tmovq " + valueStack + ", %rdi  # valueStack\n" )
                .append( "\tcall push@PLT\n");
        }
        else {
            throw new Error( "cannot find symbol " + id );
        }

        return null;
    }

    private Object visitInteger( String integer ) {
        var value = Integer.parseInt( integer.replace( "_", "" ) );

        code.append( "\tmovq " + valueStack + ", %rdi\n" )
            // push the value
            .append( "\tmovq $" + value + ", %rsi\n" )
            .append( "\tcall push@PLT\n" )
            // push the type
            .append( "\tmovq $" + RunTimeTypes.iInteger.ordinal() + ", %rsi\n" )
            .append( "\tcall push@PLT\n\n" );

        return null;
    }

    private Object visitString( String string ) {
        var value = string.substring( 1, string.length() - 1 ).intern();
        var label = constantLookUp( value );

        code.append( "\tmovq " + valueStack + ", %rdi\n" )
            .append( "\tleaq " + label + "(%rip), %rsi\n" )
            .append( "\tcall push@PLT\n" )
            // push the type
            .append( "\tmovq $" + RunTimeTypes.iString.ordinal() + ", %rsi\n" )
            .append( "\tcall push@PLT\n\n" );

        return null;
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
    private int howManyRuntimeStacks;
    private String valueStack = "-8(%rbp)";
    private String frameBaseStack = "-16(%rbp)";
    private Environment environment;
}
