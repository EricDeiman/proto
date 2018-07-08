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

public class ByteCodes {
    public enum Codes {
        Halt,    // 0x00
        Push,    // 0x01
        Pop,     // 0x02
        Print,   // 0x03
        PrintLn, // 0x04
        Pow,     // 0x05
        Mul,     // 0x06
        Div,     // 0x07
        Rem,     // 0x08
        Add,     // 0x09
        Sub,     // 0x0a
    }

    public static final boolean HasOperand[] = {
        false,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
    };    
}
