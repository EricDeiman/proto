
#include <stdio.h>

#include "minefieldrt.h"

int main( int argc, char **argv ) {
  stack *s = mkStack( 1024 );

  push( s, 42 );
  push( s, iInteger );

  printf( "the value is " );
  printTos( s );
  printf( "\n" );

  char *message = "fourty-two";

  push( s, ( long )message );
  push( s, iString );

  printf( "the value is " );
  printTos( s );
  printf( "\n" );

  return 0;
}
