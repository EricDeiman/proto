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
DEPENDJARS:=$(JARS)Minefield.jar
JAVACFLAGS:=-cp $(CLASSPATH)$(DEPENDJARS) -Xlint
JAR=jar
JARFLAGS=cf
CP:=cp
TOOLS:=../tools/
PROCESSBYTECODES:=$(TOOLS)processByteCodes.pl

PROJECTNAME:=$(shell basename `pwd`)
SOURCE=$(wildcard *.java)
JARNAME:=$(PROJECTNAME).jar

.DEFAULT : $(JARNAME)

$(JARNAME) : $(SOURCE) ByteCodes.java
	$(JAVAC) $(JAVACFLAGS) *.java
	$(JAR) $(JARFLAGS) $@ ../common/*.class
	$(CP) $(JARNAME) $(JARS)

ByteCodes.java : ByteCodes.data ByteCodes.java.template
	$(PROCESSBYTECODES) ByteCodes.data ByteCodes.java.template

.PHONY : clean scrub

clean :
	rm -f *.class
	rm -f ByteCodes.java

scrub : clean
	rm -f $(JARNAME)
