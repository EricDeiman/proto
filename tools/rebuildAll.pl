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

# scrub all the subsystem directories and rebuild everything

use strict;

my $dh;
opendir $dh, "." or die "cannot open current directory";

my @entries = readdir $dh;
my @dirs;

foreach my $e ( @entries ) {
    if( $e =~ m/^\./ ) {
        next;
    }

    if( !( -d $e ) ) {
        next;
    }

    if( -s "$e/Makefile" ) {
        if( $e =~ m/parser/ ) {
            push @dirs, "1$e";
        }
        elsif( $e =~ m/common/ ) {
            push @dirs, "2$e";
        }
        else {
            push @dirs, $e;                    
        }
    }
}

@dirs = sort @dirs;

foreach my $d ( @dirs ) {
    $d =~ s/^[0-9]//;
    print "ENTERING $d\n";
    system "cd $d; make scrub && make; cd ..";
}
