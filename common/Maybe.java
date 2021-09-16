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

public class Maybe< T > {
    public Maybe() {
        hasElement = false;
    }

    public Maybe( T t ) {
        hasElement = true;
        element = t;
    }

    public boolean hasElement() {
        return hasElement;
    }

    public T getElement() {
        if( !hasElement ) {
            throw new Error( "maybe has no element" );
        }
        return element;
    }

    private boolean hasElement;
    private T element;
}
