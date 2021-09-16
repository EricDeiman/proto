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

import java.util.ArrayDeque;
import java.util.Deque;

public class SymbolTableStack {
    public SymbolTableStack() {
        stack = new ArrayDeque< SymbolTable >();
    }

    public void push() {
        stack.addFirst( new SymbolTable() );
    }

    public void push( SymbolTable st ) {
        stack.addFirst( st );
    }

    public SymbolTable peek() {
        return stack.peekFirst();
    }

    public SymbolTable pop() {
        return stack.removeFirst();
    }

    public boolean containsKey( String id ){
        for( var t : stack ) {
            if( t.containsKey( id ) ) {
                return true;
            }
        }
        return false;       
    }

    public InterpValue lookUp( String id ) {
        for( var t : stack ) {
            if( t.containsKey( id ) ) {
                return t.lookUp( id );
            }
        }
        throw new Error( "cannot find symbol " + id );
    }

    private ArrayDeque< SymbolTable > stack;
}
