/*
* @author Branden Hitt, Jenson Tran, Grant Nasution
*
*   Instructions:
*   1. Install JLex ver 1.2.6  https://www.cs.princeton.edu/~appel/modern/java/JLex/current/README
*	    a. Download Link: https://www.cs.princeton.edu/~appel/modern/java/JLex/
*	b. JLex Installation Instructions: https://www.cs.princeton.edu/~appel/modern/java/JLex/current/README
*   2. Move tokenizer.lex and test files to a new folder
*   3. Move the folder containing JLex into the same folder as the tokenizer.lex and test files(class files and Main.java)
*   3. In terminal run the following commands:
*	    a. java JLex.Main LexAnalyzer.lex
*	    b. ren LexAnalyzer.lex.java LexAnalyzer.java
*	    c. javac LexAnalyzer.java
*	    d. java LexAnalyzer "path-to-src-file"
*/
import java.lang.System;
import java.io.Reader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class LexAnalyzer {
    public static void main(String argv[]) throws java.io.IOException {
		Reader reader = new FileReader(argv[0]);
        Yylex yy = new Yylex(reader);
		Yytoken t;
        Trie trie = new Trie();
		while ((t = yy.yylex()) != null) {
            if(t.getToken().equals("_id"))
			    trie.insert(t.getText(), trie.getDelim());
            System.out.print(t.getToken() + " ");
        }
        System.out.println();
        trie.printTrie();
    }
}

class Yytoken {
    private String text;
    private String token;
    Yytoken(String token, String text) {
        this.token = token;
        this.text = text;
    }

    //Gets
	public String toString() {return token;}
    public String getToken() {return token;}
    public String getText() {return text;}
}


class Utility {
    public static void ASSERT(boolean expr) { 
	    if (expr == false) {
	        throw (new Error("Error: Assertion failed."));
	    }
    }
  
    //List of error messages
    private static final String errorMsg[] = {
        "Error: Unclosed string.",
        "Error: Illegal character."
    };

    //Error message indices
    public static final int E_UNCLOSEDSTR = 0; 
    public static final int E_UNMATCHED = 1; 

    //Print error code
    public static void error(int code) {
	    System.out.println(errorMsg[code]);
    }
}

/*
 *      Insert: O(log52n)    O(log52n)
 * 
 *      Example: absolute, abs, ...
 *                          A   B   C  ...    Z   a  ...    z
 *      Switch:  array = { -1, -1, -1, ... , -1,  0, ... , -1}
 *
 *                          0   1   2   3   4   5   6   7   8
 *      Symbol:  array = {  b,  s,  o,  l,  u,  t,  e,  @,  @}
 *      Next  :  array = {   ,   ,  8, ...                   } 
 */

/**
 *
 * @author bhitt
 */

class Trie {
    //Properties
    private int maxTransition = 200;
    private final int SIZE = 52;
    private final char idDelimeter = '@';
    private final int switchDef = -1;
    private final char symbolDef = '#';
    private final int nextDef = -1;
    private char keywordDef = 0;
    // arrays for trie table
    private int[]  switchA;
    private char[] symbolA;
    private int[]  nextA;
    private final String[] keywordArr = { "boolean", "break", "class", "double", "else",
                                    "extends", "false", "for", "if", "implements", 
                                    "int", "interface", "new", "newarray", "null", 
                                    "println", "readln", "return", "string", "this",
                                    "true", "void", "while"};
    //indexing
    private int switchIndex;  //index of char after translated to int
    private int symbolIndex;  //next available slot in the symbol table
    
    //default constructor
    public Trie(){
        //initialize arrays
        switchA = new int[SIZE];
        symbolA = new char[maxTransition];
        nextA   = new int[maxTransition];
        //initialize all arrays
        initArrays();
        //intialize index values
        switchIndex = 0;
        symbolIndex = 0;
        insertKeywords(keywordArr);
    }
    
    // Insert method to insert a keyword/identifier into the table
    public void insert(String key, char delimeter){
        //check to see if arrays should be larger
        if(symbolIndex+key.length()>maxTransition-1){
            resize();
        }
        //temporary index
        int tempIndex = symbolIndex;
        //boolean for following
        boolean following = false;
        //get the index for the switch array
        switchIndex = charToInt(key);
        //check to see if first letter has been used yet
        if(switchA[switchIndex] == switchDef){ //first letter not used yet
            // point at the next available slot in the symbol table
            switchA[switchIndex] = symbolIndex;
            // follow that point and store the remaining symbols
            for(int i=1;i<key.length();i++){
                symbolA[symbolIndex] = key.charAt(i);
                symbolIndex++;
            }
            //add a delimeter
            symbolA[symbolIndex] = delimeter;
            symbolIndex++;
        }else{ //first letter has already been used, follow the value at index
            tempIndex = switchA[switchIndex];
            for(int i=1;i<key.length();i++){
                //check if the letters match
                if(symbolA[tempIndex] == key.charAt(i)){
                    tempIndex++;
                    if(i==key.length()-1 && symbolA[tempIndex+1] == delimeter) return;
                }else if(symbolA[tempIndex] == symbolDef){
                    symbolA[symbolIndex] = key.charAt(i);
                    tempIndex++;
                    symbolIndex++;
                }else{  //the letters don't match
                    following = true;
                    //follow path to next available slot
                    while(following){
                        //check if there is aleady a path in the next array
                        if(nextA[tempIndex] == nextDef){ //no path to follow, add new
                            nextA[tempIndex] = symbolIndex;
                            tempIndex = symbolIndex;
                            following = false;
                        }else{  //follow the path
                            tempIndex = nextA[tempIndex];
                        }
                    }
                    //put next letter in the slot
                    symbolA[tempIndex] = key.charAt(i);
                    tempIndex++;
                    symbolIndex++;
                }
            }
            //add new delimeter
            if(symbolA[tempIndex] == symbolDef){
                    symbolA[symbolIndex] = delimeter;
                    symbolIndex++;
            } else {
                following = true;
                while(following){
                    //check if there is aleady a path in the next array
                    if(nextA[tempIndex] == nextDef){ //no path to follow, add new
                        nextA[tempIndex] = symbolIndex;
                        following = false;
                    }else{  //follow the path
                        tempIndex = nextA[tempIndex];
                    }
                }
                //space is empty, add new delimeter
                symbolA[symbolIndex] = delimeter;
                symbolIndex++;
            }
        }
    }
    
    // Print out the contents of the Trie table
    public void printTrie(){
        //print switch
        printSwitch();
        //print symbol and next arrays
        printSymbolNext();
    }

    //Populating trie with keywords
    public void insertKeywords(String[] arr) {
        keywordDef--;
        for(int i = 0; i < arr.length; i++) {
            insert(arr[i], keywordDef);
            keywordDef--;
        }
    }
    
    public void printSwitch(){
        //print the table using beautiful formatting
        for(int i=0;i<52;i++){
            if(i == 0) {
                System.out.print("        ");
                for(char ch='A';ch<='T';ch++) {
                    //System.out.print(String.format("%3d",ch));
                    System.out.print(String.format("%4c",ch));
                }
                System.out.println();
                System.out.print("switch: ");
            }
            if(i == 20){
               System.out.print("        ");
                for(char ch='U';ch<='Z';ch++) {
                    //System.out.print(String.format("%3d",ch));
                    System.out.print(String.format("%4c",ch));
                }
                for(char ch='a';ch<='n';ch++) {
                    //System.out.print(String.format("%3d",ch));
                    System.out.print(String.format("%4c",ch));
                }
                System.out.println();
                System.out.print("switch: ");
            }
            if(i == 40) {
                System.out.print("        ");
                for(char ch='o';ch<='z';ch++) {
                    //System.out.print(String.format("%3d",ch));
                    System.out.print(String.format("%4c",ch));
                }
                System.out.println();
                System.out.print("switch: ");
            }
            System.out.print(String.format("%4d",switchA[i]));
            if((i+1)%20 == 0) {
                System.out.println();
                System.out.println();
            }
        }
        System.out.println();
        System.out.println();
    }
    
    public void printSymbolNext(){
        //print the other table with even more beautiful formatting
        boolean stop = false;
        int start = 0;
        int end = 20;
        while(!stop){
            if(end > symbolIndex){
                end = (symbolIndex%20)+start;
                stop = true;
            }
            System.out.print("        ");
            for(int i=start;i<end;i++){
                System.out.print(String.format("%4d",i));
            }
            System.out.println();
            System.out.print("symbol: ");
            for(int i=start;i<end;i++){
                System.out.print(String.format("%4c",symbolA[i]));
            }
            System.out.println();
            System.out.print("next:   ");
            for(int i=start;i<end;i++){
                if(nextA[i]==nextDef) System.out.print(String.format("%4c",' '));
                else System.out.print(String.format("%4d",nextA[i]));
            }
            System.out.println();
            System.out.println();
            start+=20;
            end+=20;
        }
    }
    
    // initialize array values to default
    public void initArrays(){
        for(int i=0;i<52;i++){
            switchA[i] = switchDef;
        }
        for(int i=0;i<symbolA.length;i++){
            symbolA[i] = symbolDef;
        }
        for(int i=0;i<nextA.length;i++){
            nextA[i] = nextDef;
        }
    }
    
    public void resize(){
        //create new arrays that are double the current size
        char[] temp = new char[2*maxTransition];
        int[] temp2 = new int[2*maxTransition];
        //copy the old ones over
        for(int i=0;i<maxTransition;i++){
            temp[i] = symbolA[i];
            temp2[i] = nextA[i];
        }
        //set the remaining values to the default
        for(int i=maxTransition;i<2*maxTransition;i++){
            temp[i] = symbolDef;
            temp2[i] = nextDef;
        }
        //point old pointers at new pointers
        symbolA = temp;
        nextA = temp2;
        //adjust the size
        maxTransition = 2*maxTransition;
        System.out.println("new size:"+maxTransition);
    }
    // translate a char to its integer equivalent in the trie table
    public int charToInt(String k){
        //get the first character in string
        int num = k.charAt(0);
        //check if char is lowercase
        if(num >= 97 && num <= 122) num -= 71;      //lower
        else num-= 65;                              //upper 
        //return integer after translating to table
        return num;
    }

    public char getDelim() {return idDelimeter;}
}

%%
%state BLOCK_COMMENT
%state SINGLE_COMMENT

%{
    //Tokens
    //Constants and identifiers 
	public String ID = "_id";
	public String INTCONST = "_intconst";
    public String DOUBLECONST = "_doubleconst";
    public String STRINGCONST = "_stringconst";

    //Keywords and operators
	public String BOOL = "_boolean";
    public String BREAK = "_break";
    public String CLASS = "_class";    
    public String DOUBLE = "_double";
    public String ELSE = "_else";
    public String EXTENDS = "_extends";
    public String FALSE= "_false";
    public String FOR = "_for";
    public String IF = "_if";
    public String IMPLEMENTS= "_implements";
    public String INT = "_int";
    public String INTERFACE = "_interface";
    public String NEW = "_new";
    public String NEWARR = "_newarray";
    public String NULL = "_null";
    public String PRINTLN= "_println";
    public String READLN = "_readln";
    public String RETURN = "_return";
    public String STRING = "_string";
    public String THIS = "_this";
    public String TRUE = "_true";
    public String VOID = "_void";
    public String WHILE = "_while";
    public String LPAREN= "_lparen";
    public String RPAREN = "_rparen";
    public String PLUS = "_plus";
    public String MINUS = "_minus";
    public String MUL = "_mul";
    public String DIV = "_div";
    public String MOD = "_mod";
    public String LT = "_lt";
    public String LTEQ = "_lteq";
    public String GT = "_gt";
    public String GTEQ = "_gteq";
    public String COMP = "_comp";
    public String DNE = "_dne";
    public String AND = "_and";
    public String OR = "_or";
    public String EXCL = "_excl";
    public String EQUAL = "_equal";
    public String SEMICOLON = "_semicolon";
    public String COMMA = "_comma";
    public String PERIOD = "_period";
    public String LBRACE = "_lbrace";
    public String RBRACE = "_rbrace";
    public String LBRACKET = "_lbracket";
    public String RBRACKET = "_rbracket";
%}
ALPHA=[A-Za-z]
DIGIT=[0-9]
WHITE_SPACE=[\ \t\b\012]
HEX=[0-9A-Fa-f]
NL=\r\n
NONNL_WHITE_SPACE_CHAR=[\ \t\b\012]
WHITE_SPACE_CHAR=[\n\ \t\b\012]
STRING_CHAR=(\\\"|[^\n\"]|\\{WHITE_SPACE_CHAR}+\\)

%%
<YYINITIAL> boolean				            {return (new Yytoken(BOOL, yytext()));}
<YYINITIAL> break                           {return (new Yytoken(BREAK, yytext()));}
<YYINITIAL> class                           {return (new Yytoken(CLASS, yytext()));}
<YYINITIAL> double                          {return (new Yytoken(DOUBLE, yytext()));}    
<YYINITIAL> else                            {return (new Yytoken(ELSE, yytext()));}    
<YYINITIAL> extends                         {return (new Yytoken(EXTENDS, yytext()));}   
<YYINITIAL> false                           {return (new Yytoken(FALSE, yytext()));}   
<YYINITIAL> for                             {return (new Yytoken(FOR, yytext()));}   
<YYINITIAL> if                              {return (new Yytoken(IF, yytext()));}    
<YYINITIAL> implements                      {return (new Yytoken(IMPLEMENTS, yytext()));}   
<YYINITIAL> int                             {return (new Yytoken(INT, yytext()));}   
<YYINITIAL> interface                       {return (new Yytoken(INTERFACE, yytext()));}   
<YYINITIAL> new                             {return (new Yytoken(NEW, yytext()));}
<YYINITIAL> newarray                        {return (new Yytoken(NEWARR, yytext()));}
<YYINITIAL> println                         {return (new Yytoken(PRINTLN, yytext()));}
<YYINITIAL> readln                          {return (new Yytoken(READLN, yytext()));}
<YYINITIAL> return                          {return (new Yytoken(RETURN, yytext()));}
<YYINITIAL> string                          {return (new Yytoken(STRING, yytext()));}
<YYINITIAL> this                            {return (new Yytoken(THIS, yytext()));}
<YYINITIAL> true                            {return (new Yytoken(TRUE, yytext()));}
<YYINITIAL> void                            {return (new Yytoken(VOID, yytext()));}
<YYINITIAL> while                           {return (new Yytoken(WHILE, yytext()));}
<YYINITIAL> \+                              {return (new Yytoken(PLUS, yytext()));}
<YYINITIAL> -                               {return (new Yytoken(MINUS, yytext()));}
<YYINITIAL> \*                              {return (new Yytoken(MUL, yytext()));}
<YYINITIAL> /                               {return (new Yytoken(DIV, yytext()));}
<YYINITIAL> %                               {return (new Yytoken(MOD, yytext()));}
<YYINITIAL> <                               {return (new Yytoken(LT, yytext()));}
<YYINITIAL> <=                              {return (new Yytoken(LTEQ, yytext()));}
<YYINITIAL> >                               {return (new Yytoken(GT, yytext()));}
<YYINITIAL> >=                              {return (new Yytoken(GTEQ, yytext()));}
<YYINITIAL> ==                              {return (new Yytoken(COMP, yytext()));}
<YYINITIAL> !=                              {return (new Yytoken(DNE, yytext()));}
<YYINITIAL> &&                              {return (new Yytoken(AND, yytext()));}
<YYINITIAL> "||"                            {return (new Yytoken(OR, yytext()));}
<YYINITIAL> !                               {return (new Yytoken(EXCL, yytext()));}
<YYINITIAL> =                               {return (new Yytoken(EQUAL, yytext()));}
<YYINITIAL> ;                               {return (new Yytoken(SEMICOLON, yytext()));}
<YYINITIAL> ,                               {return (new Yytoken(COMMA, yytext()));}
<YYINITIAL> \.                              {return (new Yytoken(PERIOD, yytext()));}
<YYINITIAL> \(                              {return (new Yytoken(LPAREN, yytext()));}
<YYINITIAL> \)                              {return (new Yytoken(RPAREN, yytext()));}
<YYINITIAL> \[                              {return (new Yytoken(LBRACE, yytext()));}
<YYINITIAL> \]                              {return (new Yytoken(RBRACE, yytext()));}
<YYINITIAL> \{                              {return (new Yytoken(LBRACKET, yytext()));}
<YYINITIAL> \}                              {return (new Yytoken(RBRACKET, yytext()));}

<YYINITIAL> {ALPHA}({ALPHA}|_|{DIGIT})* 	{return (new Yytoken(ID, yytext()));}
<YYINITIAL> {DIGIT}+"."{DIGIT}*((E|e)("+"|"-")?{DIGIT}+)?  {return (new Yytoken(DOUBLECONST, yytext()));}
<YYINITIAL> {DIGIT}+ 						{return (new Yytoken(INTCONST, yytext()));}
<YYINITIAL> (0x|0X){HEX}+ 					{return (new Yytoken(INTCONST, yytext()));}
<YYINITIAL> \"({STRING_CHAR})*\"            {String str =  yytext().substring(1,yytext().length() - 1);
                                                Utility.ASSERT(str.length() == yytext().length() - 2);
                                                return (new Yytoken(STRINGCONST, str));}

<YYINITIAL> {WHITE_SPACE_CHAR}				{;}
<YYINITIAL> {NL}                            {return (new Yytoken("\n","\n"));}

<YYINITIAL> /"*"                            {yybegin(BLOCK_COMMENT);}
<BLOCK_COMMENT> "*"/                        {yybegin(YYINITIAL);}
<BLOCK_COMMENT> [^"*/".]*                   {;}
<BLOCK_COMMENT> {WHITE_SPACE_CHAR}		    {;}

<YYINITIAL> //                              {yybegin(SINGLE_COMMENT);}
<SINGLE_COMMENT> [^{NL}]*                   {;}
<SINGLE_COMMENT> [{NL}\n]                   {yybegin(YYINITIAL);}