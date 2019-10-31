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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Environment() {
        env = new ArrayDeque< HashMap< String, Integer > >();
    }

    public void enter() {
        env.push( new HashMap< String, Integer >() );
    }

    public void set( String id, int offset ) {
        var map = env.peek();
        map.put( id, offset );
    }

    public boolean containsKey( String id ) {
        for( var m : env ) {
            if( m.containsKey( id ) ) {
                return true;
            }
        }
        return false;
    }

    public Maybe< Coord< Integer, Integer > > lookUp( String id ) {
        var count = 0;

        for( var m : env ) {
            if( m.containsKey( id ) ) {
                return new Maybe< Coord< Integer, Integer > >
                    ( new Coord< Integer, Integer >( count, m.get( id ) ) );
            }
            count++;
        }

        return new Maybe< Coord< Integer, Integer > >();
    }

    public void leave() {
        env.pop();
    }

    private Deque< HashMap< String, Integer > > env;
}
