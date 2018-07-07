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

#include <stdlib.h>
#include <stdio.h>
#include "minefieldrt.h"

stack *mkStack( int elements ) {
  stack *ptr = ( stack * )malloc( sizeof( stack ) + elements * sizeof( long ) );
  ptr->size = elements;
  ptr->top = 0;

  return ptr;
}

void push( stack *s, long value ) {
  if( s->top < s->size ) {
    s->memory[ (s->top)++ ] = value;
  }
  else {
    printf( "error: pushing a value on a full stack\n" );
    exit( -1 );
  }
}

long pop( stack *s ) {
  if( 0 <= s->top ) {
    return s->memory[ --(s->top) ];
  }

  printf( "error: poping a value from an empty stack\n" );
  exit( -1 );

  return -1;
}

void printTos( stack *s ) {
  long type = pop( s );
  long value = pop( s );

  switch( type ) {
  case iInteger:
    printf( "%1ld", value );
    break;

  case iString:
    printf( "%s", ( char * )value );
    break;
  }
}
