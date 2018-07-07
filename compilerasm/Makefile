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

JAVAC=javac
JARS:=../jars/
JAVACFLAGS:=-cp $(CLASSPATH)../jars/Minefield.jar:../jars/common.jar -Xlint
JAR=jar
JARFLAGS=cfm
CP:= cp

PROJECTNAME:=$(shell basename `pwd`)
SOURCE:=$(wildcard *.java)
CLASSES:=$(SOURCE:.java=.class)
JARNAME:=$(PROJECTNAME).jar

%.class:%.java
	$(JAVAC) $(JAVACFLAGS) $^

.DEFAULT : $(JARNAME)

$(JARNAME) : $(CLASSES) Manifest.txt
	$(JAR) $(JARFLAGS) $@ Manifest.txt *.class
	$(CP) $(JARNAME) $(JARS)

.PHONY : clean scrub

clean :
	rm -f *.class

scrub : clean
	rm -f $(JARNAME)
