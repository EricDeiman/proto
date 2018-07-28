#! /usr/bin/env perl

# The minefield programming language
# Copyright 2018 Eric J. Deiman

# This file is part of the minefield programming language.

# The minefield programming language is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any later
# version.

# The minefield programming language is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

# You should have received a copy of the GNU General Public License along with the
# minefield programming language. If not, see <https://www.gnu.org/licenses/>

# I thought a script to randomly develop programs would be a good way to stress test
# the compiler, but now I'm not so sure.  The programs don't make any sense, and I'm not
# sure I'd understand what triggered any bugs.  This'll stay for now.

use strict;

my $true = 1;
my $false = !$true;

my $inLet = $false;
my $randomPrint = 0.75;
my $indentMark = " " x 3;

srand();

my $result = genProg( 0 );

print "$result\n";

sub genProg() {
    my $indentLevel = shift;

    # how many expressions to generate
    my $exprCount = int( rand( 25 ) + 1 );
    my $code = "";

    foreach my $i ( 1 .. $exprCount ) {
        $code .= genExpr( $indentLevel ) . "\n";
    }

    return $code;
}

my @idStack;

sub mkIndent($) {
    my $level = shift;

    return $indentMark x $level;
}

sub genArith() {
    my @ops = ( "^", "*", "/", "%", "+", "-" );

    my $left = genInt( 0 );
    my $op = "";
    my $right = "";
    if( rand() < 0.4 ) {
        $op = $ops[ rand( scalar @ops ) ];
        $right = genArith();        
    }

    return "$left $op $right";
}

sub genComp($) {
    my $canPrint = shift or $true;
    my @ops = ( "<", "<=", "?=", "!=", ">=", ">" );
    my @subExpr = ( \&genInt, \&genString );

    my $whichSub = $subExpr[ rand( scalar @subExpr ) ];

    my $left = &$whichSub( 0, $canPrint );
    my $op = $ops[ rand( scalar @ops ) ];
    my $right = &$whichSub( 0, $canPrint );

    return "$left $op $right";
}

sub genIf() {
    my $indentLevel = shift;

    my $comp = mkIndent( $indentLevel ) . "if " . genComp( $false ) . "\n";
    my $subExpr = getSubExprSub();
    my $then_ = mkIndent( $indentLevel ) . "then " . &$subExpr . "\n";
    my $else_ = mkIndent( $indentLevel ) . "else " . &$subExpr . "\n";
    return "$comp$then_$else_";
}

sub genLet() {
    my $indentLevel = shift;

    # how many lets
    my $oldInLet = $inLet;
    $inLet = $true;

    my $num = int( rand( 2 ) + 1 );

    my @ids;
    my @values;

    foreach my $i ( 0 .. $num ) {
        push @ids, mkId();
    }

    foreach my $i ( 0 .. $num ) {
        my $exp = genSubExpr( 0 );
        push @values, $exp;
    }

    push @idStack, \@ids;
    $inLet = $oldInLet;

    my $body = genExpr( $indentLevel + 1 );
    my $result = "";

    foreach my $i ( 0 .. $num ) {
        $result .= mkIndent( $indentLevel ) . "let $ids[$i] = $values[$i]\n";
    }
    $result .= mkIndent( $indentLevel ) . "in\n$body\n";
    $result .= mkIndent( $indentLevel ) . "end\n";

    pop @idStack;

    return $result;
}

sub genString() {
    my $indentLevel = shift or 0;
    my $canPrint = shift or $false;

    my $num = int( rand( 99 ) );
    my $result = "\"string$num\"";

    if( !$inLet && $canPrint && rand() < $randomPrint ) {
        $result = mkIndent( $indentLevel ) . "print $result println\n";
    }

    return $result;
}

sub genInt() {
    my $indentLevel = shift or 0;
    my $canPrint = shift or $false;

    my $num = int( rand( 999 ) );
    my $numStr = "$num";
    if( rand() < 0.25 ) {
        $numStr = join '_', split //, $numStr;        
    }

    if( !$inLet && $canPrint && rand() < $randomPrint ) {
        $numStr = mkIndent( $indentLevel ) . "print $numStr println\n";
    }

    return $numStr;
}

sub mkId() {
    if( rand() < 0.4 ) {
        my @symbols = ( '!' , '@' , '#' , '%' , '^' , '&' , '*' , '-' , '_' , '=' ,
                        '+', ':', '/');
        my $limit = rand( 4 ) + 3;
        my $sym = "";
        foreach my $i ( 0 .. $limit) {
            $sym .= $symbols[ rand( scalar @symbols ) ];
        }
        return $sym;
    }
    my $num = int( rand( 999999 ) + 3250 );
    return "id$num";
}

sub genId() {
    my $indentLevel = shift or 0;
    my $canPrint = shift or $false;

    if( ( scalar @idStack ) == 0 ) {
        return genInt();
    }

    my $num = int( rand( scalar @idStack ) );
    my $ids = $idStack[ $num ];

    $num = int( rand( scalar @$ids ) );

    my $id = $ids->[ $num ];

    if( !$inLet && $canPrint && rand() < $randomPrint ) {
        $id = mkIndent( $indentLevel ) . "print $id println\n";
    }

    return $id;
}

sub genPrint() {
    my $indentLevel = shift;

    my $result = genSubExpr( 0 );
    return mkIndent( $indentLevel ) . "print $result println\n";
}

sub getSubExprSub {
    my @exprs = ( \&genArith, \&genComp, \&genIf, \&genLet, \&genString, \&genId,
                  \&genInt );
    
    # what kind of expr to generate
    my $exprType = int( rand( scalar @exprs ) );

    my $subref = $exprs[ $exprType ];

    return $subref;
}

sub genSubExpr() {
    my $indentLevel = shift;

    my $ref = getSubExprSub();
    return &$ref( $indentLevel );
}

sub genExpr() {
    my $indentLevel = shift;

    my @exprs = ( \&genArith, \&genComp, \&genIf, \&genLet, \&genString, \&genId,
                  \&genPrint, \&genPrint, \&genInt );

    # what kind of expr to generate
    my $exprType = int( rand( scalar @exprs ) );

    my $subref = $exprs[ $exprType ];

    return &$subref( $indentLevel );
}
