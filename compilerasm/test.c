
#include <stdio.h>

#include "minefieldrt.h"

int main( int argc, char **argv ) {
  stack *s = mkStack( 1024 );

  push( s, 4 );
  push( s, iInteger );
  push( s, 3 );
  push( s, iInteger );
  push( s, 2 );
  push( s, iInteger );

  meflPow( s );
  meflPow( s );

  printf( "4 ^ 3 ^ 2 is " );
  printTos( s );
  printf( "\n" );
   

  return 0;
}
