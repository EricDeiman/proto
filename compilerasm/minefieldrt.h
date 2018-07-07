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

#ifndef MINEFIELD_H
#define MINEFIELD_H

typedef struct {
  int size;
  int top;
  long memory[];
} stack;

enum RunTimeTypes {
  iUnknown,
  iInteger,
  iString,
};

stack *mkStack( int );
void push( stack *, long );
long pop( stack * );

void printTos( stack * );

#endif
