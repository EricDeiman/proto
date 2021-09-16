/*

  The minefield programming language
  Copyright 2018 Eric J. Deiman

  This file is part of the minefield programming language.

  The minefield programming language is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License, or (at your option) any later version.

  The minefield programming language is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with the minefield
  programming language. If not, see <https://www.gnu.org/licenses/>

*/

import static java.lang.Math.pow;

public class InterpInt implements InterpValue {
    public InterpInt( int value ) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf( value );
    }

    @Override
    public Comp getComp() {
        return new CompInt();
    }

    public int get() {
        return value;
    }

    public InterpInt math( String op, InterpInt operand ) {
        int result = 0;

        switch( op ) {
        case "+":
            result = value + operand.get();
            break;
        case "-":
            result = value - operand.get();
            break;
        case "*":
            result = value * operand.get();
            break;
        case "/":
            result = value / operand.get();
            break;
        case "%":
            result = value % operand.get();
            break;
        case "^":
            result = ( int )pow( value, operand.get() );
            break;
        }

        return new InterpInt( result );
    }

    private int value;
}
