ANTLR4:=java org.antlr.v4.Tool
ANTLR4FLAGS:=-visitor -listener -package parser

GRAMMAR:=$(wildcard *.g4)
PROJECTNAME:=$(GRAMMAR:.g4=)
JARNAME:=$(PROJECTNAME).jar

JAVAC=javac
JAVACFLAGS=

JAR=jar
JARFLAGS=cf

.DEFAULT : $(JARNAME)

$(JARNAME) : $(GRAMMAR)
	$(ANTLR4) $(ANTLR4FLAGS) $<
	$(JAVAC) $(JAVACFLAGS) *.java
	$(JAR) $(JARFLAGS) $(JARNAME) ../parser/*.class
	cp $(JARNAME) ../lib/.

.PHONY : clean scrub

clean :
	rm -f *.class
	rm -f *.java
	rm -f *.tokens

scrub : clean
	rm -f $(JARNAME)
