#! /usr/bin/env perl
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
