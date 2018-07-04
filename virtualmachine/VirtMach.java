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

import java.io.InputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;

import common.ByteCodes;
import common.VMImageBuffer;
import common.DisAsm;
import common.Version;

public class VirtMach implements Version {

    public VirtMach( InputStream in, PrintStream os, Trace trace ) {
        code = new VMImageBuffer();
        code.readFrom( in  );
        disAsm = new DisAsm( code );
        stack = new ArrayDeque< Integer >();
        byteCodesCache = ByteCodes.Codes.values();
        this.trace = trace;
        this.os = os;

        // Check signature
        for( var b : name.getBytes() ) {
            if( code.readByte() != b ) {
                throw new Error( "input file is not a minefield object file" );
            }
        }

        // Check build verison
        var build = code.readInteger();
        if( build != Version.build ) {
            throw new Error( "cannot run build version " + build + " object files" );
        }
        
        code.setPointer( sizeOfHeader );
    }

    public Integer go() {

        trace.preProgram( disAsm, stack );

        loop:
        while( true ) {
            trace.preInstruction( disAsm, stack );
            switch( byteCodesCache[ code.readByte() ] ) {
            case Halt:
                break loop;
            case Push:
                leftValue = code.readInteger();
                stack.push( leftValue );
                break;
            case Pop:
                stack.pop();  // pop the value
                break;
            case PrintI:
                leftValue = stack.pop();
                os.print( leftValue );
                break;
            case PrintS:
                leftValue = stack.pop();
                os.print( getString( leftValue ) );
                break;
            case PrintLn:
                os.println();
                break;
            default:
                break;
            }

            trace.postInstruction( disAsm, stack );
        }

        trace.postProgram( disAsm, stack );
        return stack.size();
    }

    private String getString( Integer location ) {
        StringBuilder sb = new StringBuilder();
        while( code.getByte( location ) != 0 ) {
            sb.append( (char)code.getByte( location++ ) );
        }
        return sb.toString();
    }

    private VMImageBuffer code;
    private Deque< Integer > stack;
    private Integer leftValue;
    private Integer rightValue;
    private ByteCodes.Codes[] byteCodesCache;
    private Trace trace;
    private PrintStream os;
    private DisAsm disAsm;
}
