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

import common.DisAsm;

public class Main {

    public static void usage() {
        System.out.println( "DisAsm <minefield object file> [<test file>]" );
    }

    public static void main( String[] args ) throws Exception {
        if( args.length != 1 && args.length != 2 ) {
            usage();
            return;
        }

        var is = new FileInputStream( args[ 0 ] );
        PrintStream os;
        if( args.length == 2 ) {
            os = new PrintStream( new FileOutputStream( args [ 1 ] ) );
        }
        else {
            os = System.out;
        }

        var disAsm = new DisAsm( is, os );
        disAsm.go();

        is.close();
        os.close();
    }
}
