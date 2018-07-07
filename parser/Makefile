    # The minefield programming language
    # Copyright 2018 Eric J. Deiman

    # This file is part of the minefield programming language.

    # The minefield programming language is free software: you can redistribute it
    # and/ormodify it under the terms of the GNU General Public License as published by the
    # Free Software Foundation, either version 3 of the License, or (at your option) any
    # later version.

    # The minefield programming language is distributed in the hope that it will be useful,
    # but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    # FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    # You should have received a copy of the GNU General Public License along with the
    # minefield programming language. If not, see <https://www.gnu.org/licenses/>

ANTLR4:=java org.antlr.v4.Tool
ANTLR4FLAGS:=-visitor -no-listener -package parser

GRAMMAR:=$(wildcard *.g4)
PROJECTNAME:=$(GRAMMAR:.g4=)
JARNAME:=$(PROJECTNAME).jar

JAVAC:=javac
JAVACFLAGS:=-Xlint

JAR=jar
JARFLAGS=cf

.DEFAULT : $(JARNAME)

$(JARNAME) : $(GRAMMAR)
	$(ANTLR4) $(ANTLR4FLAGS) $<
	$(JAVAC) $(JAVACFLAGS) *.java
	$(JAR) $(JARFLAGS) $(JARNAME) ../parser/*.class
	cp $(JARNAME) ../jars/.

.PHONY : clean scrub

clean :
	rm -f *.class
	rm -f *.java
	rm -f *.tokens

scrub : clean
	rm -f $(JARNAME)
