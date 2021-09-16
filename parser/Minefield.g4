/*

The minefield programming language
Copyright 2018 Eric J. Deiman

This file is part of the minefield programming language.

The minefield programming language is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later version.

The minefield programming language is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with the minefield
programming language. If not, see <https://www.gnu.org/licenses/>

*/

grammar Minefield;

prog : expr+ EOF ;

expr : arithExpr #intArith
     | compExpr #compareExpr
     | 'if' compExpr 'then' expr 'else' expr #ifExpr
     | ( 'let' ID '=' expr )+ 'in' expr+ 'end' #letExp
     | STRING  #immStr
     | ID #idExpr
     | 'print' expr #printExpr
     | 'println' #printLn
     ;

arithExpr : '(' arithExpr ')'  #arithGroup
          | <assoc=right> left=arithExpr op='^' right=arithExpr #power
            // The multiplicative operators
          | left=arithExpr op=('*' | '/' | '%') right=arithExpr #multi
            // The additive operators
          | left=arithExpr op=('+' | '-') right=arithExpr #addi
          | INTEGER #immInt
          | ID #arithId
          ;

compExpr : '(' compExpr ')' #compGroup
         | left=compExpr op=( '<' | '<=' | '?=' | '!=' | '>=' | '>' ) right=compExpr #compOp
         | INTEGER #compInt
         | STRING  #compStr
         | ID #compId
         ;

INTEGER : '-'? DIGIT(DIGIT|'_')* ;

STRING : '"' (ESC|.)*? '"' ;

DIGIT : [0-9] ;

CHAR : [a-z]|[A-Z] ;
SYMBOL : '!' | '@' | '#' | '%' | '^' | '&' | '*' | '-' | '_' | '=' | '+' | ':' | '/' ;
ID : (CHAR | DIGIT | SYMBOL)+ ;

fragment
ESC : '\\"' | '\\\\' ; // The 2 character escape symbols \" and \\

WS : [ \t\r\n]+ -> skip ;
