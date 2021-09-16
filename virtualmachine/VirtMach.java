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

import java.util.Deque;
import java.util.LinkedList;

import static java.lang.Math.pow;

import common.ByteCodes;
import common.DisAsm;
import common.RunTimeTypes;
import common.VMImageBuffer;
import common.Version;


/**
   Our stack discipline is to push the value first and then push the type of the value.
*/
public class VirtMach implements Version {

    public VirtMach( InputStream in, PrintStream os, Trace trace ) {
        code = new VMImageBuffer();
        code.readFrom( in  );
        disAsm = new DisAsm( code );
        stack = new LinkedList< Integer >();
        byteCodesCache = ByteCodes.Codes.values();
        runTimeTypesCache = RunTimeTypes.values();
        this.trace = trace;
        this.os = os;
        frameBase = 0;

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
        RunTimeTypes leftType;
        Integer leftValue;
        RunTimeTypes rightType;
        Integer rightValue;

        trace.preProgram( disAsm, stack );

        loop:
        while( true ) {
            trace.preInstruction( disAsm, stack, frameBase );
            ByteCodes.Codes op = byteCodesCache[ code.readByte() ];
            switch( op ) {
            case Halt:
                break loop;
            case Push:
                leftValue = code.readInteger();
                stack.push( leftValue );
                break;
            case Pop:
                stack.pop();  // pop the type
                stack.pop();  // pop the value
                break;
            case Print:
                leftType = runTimeTypesCache[ stack.pop() ];
                leftValue = stack.pop();
                print( leftType, leftValue );
                break;
            case PrintLn:
                os.println();
                break;
            case Pow:
            case Mul:
            case Div:
            case Rem:
            case Add:
            case Sub:
                rightType = runTimeTypesCache[ stack.pop() ];
                rightValue = stack.pop();
                leftType = runTimeTypesCache[ stack.pop() ];
                leftValue = stack.pop();
                stack.push( math( leftType, leftValue,
                                  op,
                                  rightType, rightValue ) );
                stack.push( RunTimeTypes.iInteger.ordinal() );
                break;
            case Lt:
            case Lte:
            case Eq:
            case Neq:
            case Gte:
            case Gt:
                rightType = runTimeTypesCache[ stack.pop() ];
                rightValue = stack.pop();
                leftType = runTimeTypesCache[ stack.pop() ];
                leftValue = stack.pop();
                stack.push( compare( leftType, leftValue,
                                     op,
                                     rightType, rightValue ) );
                stack.push( RunTimeTypes.iBoolean.ordinal() );
                break;
            case JmpT:
                leftType = runTimeTypesCache[ stack.pop() ];
                leftValue = stack.pop();
                var destination = code.readInteger();
                if( leftType != RunTimeTypes.iBoolean ) {
                    throw new Error( "attempt to use " + leftType +
                                     " as a boolean" );
                }
                if( leftValue != 0 ) {
                    code.setPointer( destination );
                }
                break;
            case JmpF:
                leftType = runTimeTypesCache[ stack.pop() ];
                leftValue = stack.pop();
                destination = code.readInteger();
                if( leftType != RunTimeTypes.iBoolean ) {
                    throw new Error( "attempt to use " + leftType +
                                     " as a boolean" );
                }
                if( leftValue == 0 ) {
                    code.setPointer( destination );
                }
                break;
            case Jmp:
                destination = code.readInteger();
                code.setPointer( destination );
                break;
            case Enter:
                stack.push( frameBase );
                frameBase = stack.size();
                break;
            case Leave:
                while( stack.size() > frameBase ) {
                    stack.pop();
                }
                frameBase = stack.pop();
                break;
            case Get:
                {
                    int frameOffset = code.readInteger();
                    int stackOffset = code.readInteger();
                    int oldFrameBase = frameBase;
                    while( frameOffset > 0 ) {
                        // System.err.println( String.format( "In Get: frameOffset = %d, " +
                        //                                    " stack depth = %d" +
                        //                                    " oldFrameBase = %d",
                        //                                    frameOffset,
                        //                                    stack.size(),
                        //                                    oldFrameBase ) );
                        oldFrameBase = stack.get( stack.size() -
                                                  oldFrameBase /* + 1 */ );
                        frameOffset--;
                    }
                    int frameBaseIndex = stack.size() - 1 - oldFrameBase;
                    int value, type;
                    value = stack.get( frameBaseIndex - stackOffset * 2 );
                    type = stack.get( frameBaseIndex - stackOffset * 2 - 1 );
                    stack.push( value );
                    stack.push( type );
                }
                break;

            default:
                break;
            }

            trace.postInstruction( disAsm, stack );
        }

        trace.postProgram( disAsm, stack );
        return stack.size();
    }

    private int compare( RunTimeTypes leftType, int left,
                         ByteCodes.Codes op,
                         RunTimeTypes rightType, int right ) {
        if( leftType != rightType ) {
            throw new Error( "cannot compare " + leftType +
                             " with " + rightType );
        }

        int result = 0;
        switch( leftType ) {
        case iInteger:
            result  = compareInteger( left, op, right );
            break;
        case iString:
            result =  compareString( left, op, right );
            break;
        case iBoolean:
            result = compareBoolean( left, op, right );
            break;
        }

        return result;
    }

    private int compareInteger( int left, ByteCodes.Codes op, int right ) {
        int result = 0;

        switch( op ) {
        case Lt:
            result = left < right ? 1 : 0;
            break;
        case Lte:
            result = left <= right ? 1 : 0;
            break;
        case Eq:
            result = left == right ? 1 : 0;
            break;
        case Neq:
            result = left != right ? 1 : 0;
            break;
        case Gte:
            result = left >= right ? 1 : 0;
            break;
        case Gt:
            result = left > right ? 1 : 0;
            break;
        }

        return result;
    }

    private int compareBoolean( int left, ByteCodes.Codes op, int right ) {
        int result = 0;

        switch( op ) {
        case Eq:
            result = left == right ? 1 : 0;
            break;
        case Neq:
            result = left != right ? 1 : 0;
            break;
        default:
            throw new Error( "cannot use " + op + " on booleans");
        }

        return result;
    }

    private int compareString( int left, ByteCodes.Codes op, int right ) {
        String sLeft = getString( left );
        String sRight = getString( right );
        int result = 0;

        switch( op ) {
        case Lt:
            result = sLeft.compareTo(sRight) < 0 ? 1 : 0;
            break;
        case Lte:
            result = sLeft.compareTo(sRight) <= 0 ? 1 : 0;
            break;
        case Eq:
            result = sLeft.compareTo(sRight) == 0 ? 1 : 0;
            break;
        case Neq:
            result = sLeft.compareTo(sRight) != 0 ? 1 : 0;
            break;
        case Gte:
            result = sLeft.compareTo(sRight) >= 0 ? 1 : 0;
            break;
        case Gt:
            result = sLeft.compareTo(sRight) > 0 ? 1 : 0;
            break;
        }

        return result;
    }

    private int math( RunTimeTypes leftType, int left,
                      ByteCodes.Codes op,
                      RunTimeTypes rightType,int right ) {
        int result = 0;

        if( leftType != rightType || leftType != RunTimeTypes.iInteger ) {
            throw new Error( "arithmatic only works on 2 integer values" );
        }

        switch( op ) {
        case Pow:
            result = ( int )pow( left, right );
            break;
        case Mul:
            result = left * right;
            break;
        case Div:
            result = left / right;
            break;
        case Rem:
            result = left % right;
            break;
        case Add:
            result = left + right;
            break;
        case Sub:
            result = left - right;
            break;
        }

        return result;
    }

    private void print( RunTimeTypes type, Integer value ) {
        switch( type ) {
        case iInteger:
            os.print( value );
            trace.io( value.toString() );
            break;
        case iString:
            os.print( getString( value ) );
            trace.io( getString( value ) );
            break;
        case iBoolean: {
            String result;
            if( value != 0 ) {
                result = "true";
            }
            else {
                result = "false";
            }

            os.print( result );
            trace.io( result );

        }
            break;
        }
    }

    private String getString( Integer location ) {
        StringBuilder sb = new StringBuilder();
        while( code.getByte( location ) != 0 ) {
            sb.append( (char)code.getByte( location++ ) );
        }
        return sb.toString();
    }

    private VMImageBuffer code;
    private LinkedList< Integer > stack;
    private ByteCodes.Codes[] byteCodesCache;
    private RunTimeTypes[] runTimeTypesCache;
    private Trace trace;
    private PrintStream os;
    private DisAsm disAsm;
    private Integer frameBase;
}
