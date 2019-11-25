Authors: Grant Nasution, Jenson Tran, Branden Hitt
Syntax analyzer and Lexical Analyzer for a Toy programming language using CUP and JLex

Instructions to compile and run:
Assuming Windows environment and that JLex and CUP folders are in the same directory, in terminal type
Java JLex.Main LexAnalyzer.lex
(optional if Yylex.java exist) del Yylex.java
ren LexAnalyzer.lex.java Yylex.java
Java -jar \CUP\java-cup-11b.jar Toy.cup
Javac -cp ".;.\CUP\java-cup-11b-runtime.jar" Main.java
Java -cp ".;.\CUP\java-cup-11b-runtime.jar" Main <PathToTestFile>.txt
