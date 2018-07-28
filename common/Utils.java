/*

  The minefield programming language
  Copyright 2018 Eric J. Deiman

  This file is part of the minefield programming language.

  The minefield programming language is free software: you can redistribute it and/or
  modify it under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any later
  version.

  The minefield programming language is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with the
  minefield programming language. If not, see <https://www.gnu.org/licenses/>

*/

package common;

import java.util.ArrayList;
import java.util.List;

import parser.MinefieldParser;

/**
   Just about every project has one of these catch-all places to put methods that are
   shared but don't really fit someplace specific.
 */
public class Utils {
    private Utils() {
        
    }

    private static List< String > filterAllowed( List< String > ids, List< String > allowed ) {
        return ids.stream().filter( i -> allowed.contains( i ) )
            .collect( ArrayList< String >::new,
                      ArrayList< String >::add,
                      ArrayList< String >::addAll );
    }

    public static DirectedGraph< String > dependency( MinefieldParser.LetExpContext ctx ) {
        var freeIn = new FreeInVisitor();
        var graph = new DirectedGraph< String >();
        var theseIds = new ArrayList< String >();
        for( var s : ctx.ID() ) {
            theseIds.add( s.getText() );
        }

        for( int i = 0; i < ctx.ID().size(); i++ ) {
            var id = ctx.ID( i ).getText();
            var free = freeIn.visit( ctx.expr( i ) );
            free = filterAllowed( free, theseIds );
            if( 0 < free.size() ) {
                for( var n : free ) {
                    graph.edgeFromTo( n, id );                        
                }
            }
            else {
                graph.addNode( id );
            }
        }

        return graph;
    }
}
