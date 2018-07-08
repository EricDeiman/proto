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

import java.io.InputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Stack;

public class DisAsm implements Version {

    public DisAsm( InputStream is, PrintStream os ) {
        code = new VMImageBuffer();
        code.readFrom( is );
        this.os = os;
        byteCodesCache = ByteCodes.Codes.values();
    }

    public DisAsm( VMImageBuffer buffer ) {
        code = buffer;
        byteCodesCache = ByteCodes.Codes.values();
    }

    public void go() throws Exception {
        dumpHeader();
        dumpInstructions();
        dumpConstantPool();
    }

    public void dumpHeader() throws Exception {
        code.setPointer( 0 );

        // Read the signature
        for( var b : name.getBytes() ) {
            if( b != code.readByte() ) {
                throw new Exception( "Input file is not a minefield object file" );
            }
        }

        os.println( "Minefield Object File" );

        // Read the build version
        var build = code.readInteger();
        if( build != Version.build ) {
            throw new Exception( "cannot disassemble a build version " + build + " file" );
        }

        os.println( "Build version: " + build );

        // Read the constant pool offset
        this.constantPoolOffset = code.readInteger();

        os.println( String.format( "Constant Pool Offset: 0x%x",
                                    this.constantPoolOffset  ) );
        os.println();
    }

    public String dumpInstruction() {
        return dumpInstruction( true );
    }

    public String dumpInstruction( boolean advancePointer ) {
        var buffer = new StringBuilder();

        var position = code.getPointer();
        ByteCodes.Codes opCode;
        if( advancePointer ) {
            opCode = byteCodesCache[ code.readByte() ];
        }
        else {
            opCode = byteCodesCache[ code.getByte( position ) ];
        }
        var operand = 0;
        var hasOperand = ByteCodes.HasOperand[ opCode.ordinal() ];

        var operandDStr = "";

        if( hasOperand ) {
            if( advancePointer ) {
                operand = code.readInteger();
            }
            else {
                operand = code.getInteger( position + 1 );
            }
            operandDStr = String.format( "0x%-6x", operand );
        }

        String operandXStr = hasOperand ? String.format("%08x", operand) : "";
        buffer.append( String.format( "%04x:  %02x %8s   %-7s %-10s",
                                      position,
                                      opCode.ordinal(),
                                      operandXStr,
                                      opCode.name(),
                                      operandDStr ) );

        return buffer.toString();
    }

    public void dumpInstructions() {
        code.setPointer( sizeOfHeader );

        while( ByteCodes.Codes.Halt.ordinal() != code.getByte( code.getPointer() ) ) {
            os.println( dumpInstruction() );
        }

        return;
    }

    public void dumpConstantPool() {
        code.setPointer( constantPoolOffset );
        os.println();
        while( code.getPointer() < code.size() ) {
            Integer position = code.getPointer();

            StringBuilder sb = new StringBuilder();
            while( code.getByte( code.getPointer() ) != 0 ) {
                sb.append( (char)code.readByte() );
            }
            code.readByte(); // ignore the terminating zero byte

            os.println( String.format( "%04x:  %s", position, sb.toString() ) );
        }
    }

    private VMImageBuffer code;
    private PrintStream os;
    private InputStream is;
    private ByteCodes.Codes[] byteCodesCache;
    private int constantPoolOffset;
}
