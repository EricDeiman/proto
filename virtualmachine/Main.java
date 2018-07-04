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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main {

    public static void usage() {
        System.out.println( "VirtMach [-trace] <minefield object file>" );
    }

    public static void main( String[] args ) throws Exception {
        if( args.length != 1 && args.length != 2 ) {
            usage();
            return;
        }

        var inputFileArg = 0;
        var trace = new EmptyTrace();

        if( args[ 0 ].equals( "-trace" ) ) {
            inputFileArg = 1;
            var outFileName = args[ 1 ].replace( ".mo", ".log" );
            trace = new PrintTrace( new PrintStream( outFileName ) );
        }

        var is = new FileInputStream( args[ inputFileArg ] );

        var mach = new VirtMach( is, System.out, trace );
        mach.go();

        is.close();
    }
}
