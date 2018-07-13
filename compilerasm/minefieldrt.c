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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <tgmath.h>

#include "minefieldrt.h"

typedef int ( *comparer )( void *, char *, void * );

typedef struct {
  char *name;
  comparer fn;
} entry;

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

  case iBoolean:
    printf( "%s", value == 0 ? "false" : "true" );
    break;
  }
}

char *message = "arithmatic only works on integer types";

void meflPow( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = ( int )pow( leftValue, rightValue );

  push( s, leftValue );
  push( s, leftType );
}

void meflMul( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = leftValue * rightValue;

  push( s, leftValue );
  push( s, leftType );
}

void meflDiv( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = leftValue / rightValue;

  push( s, leftValue );
  push( s, leftType );
}

void meflRem( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = leftValue % rightValue;

  push( s, leftValue );
  push( s, leftType );
}

void meflAdd( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = leftValue + rightValue;

  push( s, leftValue );
  push( s, leftType );
}

void meflSub( stack *s ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType || leftType != iInteger ) {
    printf( "%s", message );
    exit( -1 );
  }

  leftValue = leftValue - rightValue;

  push( s, leftValue );
  push( s, leftType );
}

int meflCompareInt( void *left, char *op, void *right ) {
  int _left = *( int * )left;
  int _right = *( int * )right;

  if( strcmp( op, "<" ) == 0 ) {
    return _left < _right;
  }
  else if( strcmp( op, "<=" ) == 0 ) {
    return _left <= _right;
  }
  else if( strcmp( op, "?=" ) == 0 ) {
    return _left == _right;
  }
  else if( strcmp( op, "!=" ) == 0 ) {
    return _left != _right;
  }
  else if( strcmp( op, ">=" ) == 0 ) {
    return _left >= _right;
  }
  else if( strcmp( op, ">" ) == 0 ) {
    return _left > _right;
  }

  return 0;
}

int meflCompareStr( void *left, char *op, void *right ) {
  char *_left = ( char * )left;
  char *_right = ( char * )right;

  if( strcmp( op, "<" ) == 0 ) {
    return strcmp( _left, _right ) < 0;
  }
  else if( strcmp( op, "<=" ) == 0 ) {
    return strcmp( _left, _right ) <= 0;
  }
  else if( strcmp( op, "?=" ) == 0 ) {
    return strcmp( _left, _right ) == 0;
  }
  else if( strcmp( op, "!=" ) == 0 ) {
    return strcmp( _left, _right ) != 0;
  }
  else if( strcmp( op, ">=" ) == 0 ) {
    return strcmp( _left, _right ) >= 0;
  }
  else if( strcmp( op, ">" ) == 0 ) {
    return strcmp( _left, _right ) > 0;
  }

  return 0;
}

int meflCompareBool( void *left, char *op, void *right ) {
  int _left = *( int * )left;
  int _right = *( int * )right;

  if( strcmp( op, "?=" ) == 0 ) {
    return _left == _right;
  }
  else if( strcmp( op, "!=" ) == 0 ) {
    return _left != _right;
  }

  printf( "cannot use %s on booleans", op );
  exit( -1 );
}

char *typeName[] = {
  "no type",
  "integer",
  "string",
  "boolean"
};

entry ops[] = {
  {
    "integer<integer",
    meflCompareInt
  },
  {
    "integer<=integer",
    meflCompareInt
  },
  {
    "integer?=integer",
    meflCompareInt
  },
  {
    "integer!=integer",
    meflCompareInt
  },
  {
    "integer>=integer",
    meflCompareInt
  },
  {
    "integer>integer",
    meflCompareInt
  },
  {
    "string<string",
    meflCompareStr
  },
  {
    "string<=string",
    meflCompareStr
  },
  {
    "string?=string",
    meflCompareStr
  },
  {
    "string!=string",
    meflCompareStr
  },
  {
    "string>=string",
    meflCompareStr
  },
  {
    "string>string",
    meflCompareStr
  },
  {
    "boolean?=boolean",
    meflCompareBool
  },
  {
    "boolean!=boolean",
    meflCompareBool
  }
};

int opsCount = sizeof( ops ) / sizeof( entry );

void meflCompare( stack *s, char *op ) {
  long rightType = pop( s );
  long rightValue = pop( s );
  long leftType = pop( s );
  long leftValue = pop( s );

  if( leftType != rightType ) {
    printf( "cannot apply %s to types %s and %s", op, typeName[ leftType ],
            typeName[ rightType ] );
    exit( -1 );
  }

  comparer fn;
  char name[ 256 ];
  sprintf( name, "%s%s%s", typeName[ leftType ], op, typeName[ rightType ] );

  int i;
  for( i = 0; i < opsCount; i++ ) {
    if( strcmp( ops[ i ].name, name ) == 0 ) {
      fn = ops[ i ].fn;
      break;
    }
  }

  if( i == opsCount ) {
    printf( "cannot find %s\n", name );
    exit( -1 );
  }

  if( leftType == 1 || leftType == 3 ) {
    leftValue = fn( &leftValue, op, &rightValue );    
  }
  else {
    leftValue = fn( ( char * )leftValue, op, ( char * )rightValue );
  }

  push( s, leftValue );
  push( s, iBoolean );
}
