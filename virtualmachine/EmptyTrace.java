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

import java.util.Deque;

import common.DisAsm;

public class EmptyTrace implements Trace {
    @Override
    public void preProgram( DisAsm code, Deque< Integer > stack ) {
        return;
    }

    @Override
    public void preInstruction( DisAsm code, Deque< Integer > stack ) {
        return;
    }

    @Override
    public void postInstruction( DisAsm code, Deque< Integer > stack ) {
        return;
    }

    @Override
    public void postProgram( DisAsm code, Deque< Integer > stack ) {
        return;
    }

    @Override
    public void io( String data ) {
        return;
    }
}
