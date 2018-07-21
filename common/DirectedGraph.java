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

import java.util.ArrayList;
import java.util.List;

public class DirectedGraph< Node > {
    public DirectedGraph() {
        graph = new ArrayList< Pair< Node, List< Node > > >();
    }

    public void addNode( Node node ) {
        var nodes = getNodes();

        if( node != null ) {
            if( !nodes.contains( node ) ) {
                graph.add( mkPair( node ) );
            }
        }

        return;
    }

    public void edgeFromTo( Node from, Node to ) {
        var nodes = getNodes();

        addNode( to );

        if( from != null && to != null ) {
            for( var n : graph ) {
                if( n.getFirst() == to ) {
                    if( !n.getSecond().contains( from ) ) {
                        n.getSecond().add( from );
                    }
                }
            }
        }

        return;
    }

    /**
       Based on Kahn's algorithm from as described at
       https://en.wikipedia.org/wiki/Topological_sorting
     */
    public List< Node > tsort() {
        var s = getNoFanIn( graph );
        var g = getFanIn( graph );
        var e = new ArrayList< Node >();

        while( 0 < s.size() ) {
            var node = s.remove( 0 );
            var n = node.getFirst();
            e.add( n );
            for( var m : g ) {
                if( m.getSecond().contains( n ) ) {
                    m.getSecond().remove( n );
                }
            }
            var s1 = getNoFanIn( g );
            g = getFanIn( g ); 
            s.addAll( s1 );
        }

        if( g.size() != 0 ) {
            return new ArrayList< Node >();
        }

        return e;
    }

    public String toString() {
        return String.format( "%s", graph );
    }

    private List< Pair< Node, List< Node > > >
    getNoFanIn( List< Pair< Node, List< Node > > > g ) {
        return g.stream().filter( v -> v.getSecond().size() == 0 )
            .collect( ArrayList< Pair< Node, List< Node > > >::new,
                      ArrayList< Pair< Node, List< Node > > >::add,
                      ArrayList< Pair< Node, List< Node > > >::addAll );
    }

    private List< Pair< Node, List< Node > > >
    getFanIn( List< Pair< Node, List< Node > > > g ) {
        return g.stream().filter( v -> v.getSecond().size() != 0 )
            .collect( ArrayList< Pair< Node, List< Node > > >::new,
                      ArrayList< Pair< Node, List< Node > > >::add,
                      ArrayList< Pair< Node, List< Node > > >::addAll );
    }

    private List< Node >getNodes() {
        return graph.stream().map( v -> v.getFirst() ).collect( ArrayList< Node >::new,
                                                                ArrayList< Node >::add,
                                                                ArrayList< Node >::addAll
                                                              );
    }

    private Pair< Node, List< Node > > mkPair( Node n ) {
        return new Pair< Node, List< Node > >( n, new ArrayList< Node >() );
    }

    private List< Pair< Node, List< Node > > > graph;
}
