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
import java_cup.runtime.*;
/*
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
*/
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


class Yylex implements java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

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
    public String NOT = "_not";
    public String EQUAL = "_equal";
    public String SEMICOLON = "_semicolon";
    public String COMMA = "_comma";
    public String PERIOD = "_period";
    public String LBRACE = "_lbrace";
    public String RBRACE = "_rbrace";
    public String LBRACKET = "_lbracket";
    public String RBRACKET = "_rbracket";
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	Yylex (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	Yylex (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Yylex () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int SINGLE_COMMENT = 2;
	private final int BLOCK_COMMENT = 1;
	private final int YYINITIAL = 0;
	private final int yy_state_dtrans[] = {
		0,
		57,
		59
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NOT_ACCEPT,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NOT_ACCEPT,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NOT_ACCEPT,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NOT_ACCEPT,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NOT_ACCEPT,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NOT_ACCEPT,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NOT_ACCEPT,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NOT_ACCEPT,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NOT_ACCEPT,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NOT_ACCEPT,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NOT_ACCEPT,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NOT_ACCEPT,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NO_ANCHOR,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NO_ANCHOR,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NO_ANCHOR,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NO_ANCHOR,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NO_ANCHOR,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NO_ANCHOR,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NO_ANCHOR,
		/* 126 */ YY_NO_ANCHOR,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NO_ANCHOR,
		/* 130 */ YY_NO_ANCHOR,
		/* 131 */ YY_NO_ANCHOR,
		/* 132 */ YY_NO_ANCHOR,
		/* 133 */ YY_NO_ANCHOR,
		/* 134 */ YY_NO_ANCHOR,
		/* 135 */ YY_NO_ANCHOR,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NO_ANCHOR,
		/* 138 */ YY_NO_ANCHOR,
		/* 139 */ YY_NO_ANCHOR,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NO_ANCHOR,
		/* 142 */ YY_NO_ANCHOR,
		/* 143 */ YY_NO_ANCHOR,
		/* 144 */ YY_NO_ANCHOR,
		/* 145 */ YY_NO_ANCHOR,
		/* 146 */ YY_NO_ANCHOR,
		/* 147 */ YY_NO_ANCHOR,
		/* 148 */ YY_NO_ANCHOR,
		/* 149 */ YY_NO_ANCHOR,
		/* 150 */ YY_NO_ANCHOR,
		/* 151 */ YY_NO_ANCHOR,
		/* 152 */ YY_NO_ANCHOR,
		/* 153 */ YY_NO_ANCHOR,
		/* 154 */ YY_NO_ANCHOR,
		/* 155 */ YY_NO_ANCHOR,
		/* 156 */ YY_NO_ANCHOR,
		/* 157 */ YY_NO_ANCHOR,
		/* 158 */ YY_NO_ANCHOR,
		/* 159 */ YY_NO_ANCHOR,
		/* 160 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"53:8,54:2,55,53:2,56,53:18,54,32,51,53:2,28,33,53,38,39,26,24,36,25,37,27,4" +
"8,46:9,53,35,29,30,31,53:2,50:4,47,50,44:17,49,44:2,40,52,41,53,45,53,5,1,9" +
",11,4,15,21,22,16,44,8,3,17,6,2,18,44,7,10,14,12,23,19,13,20,44,42,34,43,53" +
":2,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,161,
"0,1,2,1:3,3,1,4,5,6,7,1:9,8,1,9,1:8,10,1:2,11,9,12,13,9:18,14,1,15,1,16,17," +
"18,19,20,21,22,23,24,20,25,26,27,13,28,29,30,31,32,33,34,35,36,19,37,38,39," +
"40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64," +
"65,66,67,68,69,70,71,72,73,74,9,75,76,77,78,79,80,81,82,83,84,85,86,87,88,8" +
"9,90,91,92,93,94,95,96,97,98,99,100,101,9,102,103,104,105,106,107,108,109,1" +
"10")[0];

	private int yy_nxt[][] = unpackFromString(111,57,
"1,2,151:2,119,151,99,154,151,155,156,157,151:2,120,100,62,151,158,159,151:3" +
",121,3,4,5,6,7,8,9,10,11,61,68,12,13,14,15,16,17,18,19,20,151,-1,21,151,63," +
"151:2,70,-1:2,22:2,72,-1:58,151,160,151:4,122,151:16,-1:20,151,123:2,151,12" +
"3,151:2,-1:32,24,25,-1:59,26,-1:56,27,-1:56,28,-1:56,29,-1:63,32,-1:8,21,-1" +
",21,-1:9,151:23,-1:20,151,123:2,151,123,151:2,-1:10,78,-1:41,32,78,32,-1:9," +
"151:4,142,151:18,-1:20,151,123:2,151,123,151:2,-1:7,151:3,153,151:19,-1:20," +
"151,123:2,151,123,151:2,-1:7,38,-1:2,38:2,-1:3,38,-1,38,-1:3,38,-1:30,38:3," +
"-1,38,-1:6,1,66:25,86,-1,66:9,-1,66:19,1,67:54,60:2,-1:33,30,-1:24,151:5,73" +
",151:8,23,151,130,151:6,-1:20,151,123:2,151,123,151:2,-1:19,74,-1:23,32,-1:" +
"8,21,-1,21,74,-1:53,64,-1,64,-1:9,70:50,33,76,70:2,-1,70,-1,66:25,-1:2,66:9" +
",-1,66:19,-1,67:54,-1:36,31,-1:23,151:18,35,151:4,-1:20,151,123:2,151,123,1" +
"51:2,-1:7,151:6,36,151:16,-1:20,151,123:2,151,123,151:2,-1:61,34,-1:2,151:1" +
"3,37,151:9,-1:20,151,123:2,151,123,151:2,-1:7,151:3,39,151:19,-1:20,151,123" +
":2,151,123,151:2,-1:7,70:50,65,76,70,80,82,70,-1,151:3,40,151:19,-1:20,151," +
"123:2,151,123,151:2,-1:30,84:2,-1:20,64,-1,64,-1:9,151:9,41,151:13,-1:20,15" +
"1,123:2,151,123,151:2,-1:7,70:50,33,76,70,80,82,70,-1,151:10,42,151:12,-1:2" +
"0,151,123:2,151,123,151:2,-1:58,70,-1,82:2,-1:2,151:7,43,151:15,-1:20,151,1" +
"23:2,151,123,151:2,-1:7,151:9,44,151:13,-1:20,151,123:2,151,123,151:2,-1:33" +
",58,-1:30,151:3,45,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151:3,46,151:1" +
"9,-1:20,151,123:2,151,123,151:2,-1:7,151:5,47,151:17,-1:20,151,123:2,151,12" +
"3,151:2,-1:7,151:5,48,151:17,-1:20,151,123:2,151,123,151:2,-1:7,151:20,49,1" +
"51:2,-1:20,151,123:2,151,123,151:2,-1:7,151:3,50,151:19,-1:20,151,123:2,151" +
",123,151:2,-1:7,151:5,51,151:17,-1:20,151,123:2,151,123,151:2,-1:7,151:9,52" +
",151:13,-1:20,151,123:2,151,123,151:2,-1:7,151:5,53,151:17,-1:20,151,123:2," +
"151,123,151:2,-1:7,151:19,54,151:3,-1:20,151,123:2,151,123,151:2,-1:7,151:3" +
",55,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151:9,56,151:13,-1:20,151,123" +
":2,151,123,151:2,-1:7,151:3,69,151:19,-1:20,151,123:2,151,123,151:2,-1:7,15" +
"1,71,151:2,129,151:18,-1:20,151,123:2,151,123,151:2,-1:7,151:9,75,151:13,-1" +
":20,151,123:2,151,123,151:2,-1:7,151:11,77,151:11,-1:20,151,123:2,151,123,1" +
"51:2,-1:7,151:15,79,151:7,-1:20,151,123:2,151,123,151:2,-1:7,151:15,81,151:" +
"7,-1:20,151,123:2,151,123,151:2,-1:7,151:4,83,151:18,-1:20,151,123:2,151,12" +
"3,151:2,-1:7,151:9,85,151:13,-1:20,151,123:2,151,123,151:2,-1:7,151:9,87,15" +
"1:13,-1:20,151,123:2,151,123,151:2,-1:7,151:2,88,151:20,-1:20,151,123:2,151" +
",123,151:2,-1:7,151:2,89,151:20,-1:20,151,123:2,151,123,151:2,-1:7,151:6,90" +
",151:16,-1:20,151,123:2,151,123,151:2,-1:7,151:5,91,151:17,-1:20,151,123:2," +
"151,123,151:2,-1:7,151:2,92,151:20,-1:20,151,123:2,151,123,151:2,-1:7,151:4" +
",93,151:18,-1:20,151,123:2,151,123,151:2,-1:7,151:10,94,151:12,-1:20,151,12" +
"3:2,151,123,151:2,-1:7,151:2,95,151:20,-1:20,151,123:2,151,123,151:2,-1:7,1" +
"51:4,96,151:18,-1:20,151,123:2,151,123,151:2,-1:7,151:8,97,151:14,-1:20,151" +
",123:2,151,123,151:2,-1:7,151:13,98,151:9,-1:20,151,123:2,151,123,151:2,-1:" +
"7,151:2,101,151:9,124,151:10,-1:20,151,123:2,151,123,151:2,-1:7,151:6,102,1" +
"51:14,103,151,-1:20,151,123:2,151,123,151:2,-1:7,151,104,151:21,-1:20,151,1" +
"23:2,151,123,151:2,-1:7,151:3,105,151:19,-1:20,151,123:2,151,123,151:2,-1:7" +
",151:13,134,151:9,-1:20,151,123:2,151,123,151:2,-1:7,151:4,135,151:8,136,15" +
"1:9,-1:20,151,123:2,151,123,151:2,-1:7,151:4,106,151:18,-1:20,151,123:2,151" +
",123,151:2,-1:7,151:6,137,151:16,-1:20,151,123:2,151,123,151:2,-1:7,151:11," +
"138,151:11,-1:20,151,123:2,151,123,151:2,-1:7,151:2,107,151:20,-1:20,151,12" +
"3:2,151,123,151:2,-1:7,151:17,152,151:5,-1:20,151,123:2,151,123,151:2,-1:7," +
"151:15,139,151:7,-1:20,151,123:2,151,123,151:2,-1:7,151:15,108,151:7,-1:20," +
"151,123:2,151,123,151:2,-1:7,151:2,140,151:20,-1:20,151,123:2,151,123,151:2" +
",-1:7,151:3,141,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151:10,109,151:12" +
",-1:20,151,123:2,151,123,151:2,-1:7,151:11,110,151:11,-1:20,151,123:2,151,1" +
"23,151:2,-1:7,151:15,111,151:7,-1:20,151,123:2,151,123,151:2,-1:7,112,151:2" +
"2,-1:20,151,123:2,151,123,151:2,-1:7,151:5,144,151:17,-1:20,151,123:2,151,1" +
"23,151:2,-1:7,151:3,113,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151:5,114" +
",151:17,-1:20,151,123:2,151,123,151:2,-1:7,151:6,145,151:16,-1:20,151,123:2" +
",151,123,151:2,-1:7,151:3,147,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151" +
":13,115,151:9,-1:20,151,123:2,151,123,151:2,-1:7,151:6,116,151:16,-1:20,151" +
",123:2,151,123,151:2,-1:7,151:14,148,151:8,-1:20,151,123:2,151,123,151:2,-1" +
":7,151:16,149,151:6,-1:20,151,123:2,151,123,151:2,-1:7,151:4,117,151:18,-1:" +
"20,151,123:2,151,123,151:2,-1:7,151:3,150,151:19,-1:20,151,123:2,151,123,15" +
"1:2,-1:7,151:5,118,151:17,-1:20,151,123:2,151,123,151:2,-1:7,151:2,143,151:" +
"20,-1:20,151,123:2,151,123,151:2,-1:7,151:6,146,151:16,-1:20,151,123:2,151," +
"123,151:2,-1:7,151:3,125,151:19,-1:20,151,123:2,151,123,151:2,-1:7,151:2,12" +
"6,151:20,-1:20,151,123:2,151,123,151:2,-1:7,151:13,127,151:9,-1:20,151,123:" +
"2,151,123,151:2,-1:7,151,128,151:21,-1:20,151,123:2,151,123,151:2,-1:7,151:" +
"6,131,151:16,-1:20,151,123:2,151,123,151:2,-1:7,151:21,132,151,-1:20,151,12" +
"3:2,151,123,151:2,-1:7,151,133,151:21,-1:20,151,123:2,151,123,151:2,-1:6");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {
				return null;
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 1:
						
					case -2:
						break;
					case 2:
						{return (new Symbol(sym.ID));}
					case -3:
						break;
					case 3:
						{return (new Symbol(sym.PLUS));}
					case -4:
						break;
					case 4:
						{return (new Symbol(sym.MINUS));}
					case -5:
						break;
					case 5:
						{return (new Symbol(sym.MUL));}
					case -6:
						break;
					case 6:
						{return (new Symbol(sym.DIV));}
					case -7:
						break;
					case 7:
						{return (new Symbol(sym.MOD));}
					case -8:
						break;
					case 8:
						{return (new Symbol(sym.LT));}
					case -9:
						break;
					case 9:
						{return (new Symbol(sym.EQUAL));}
					case -10:
						break;
					case 10:
						{return (new Symbol(sym.GT));}
					case -11:
						break;
					case 11:
						{return (new Symbol(sym.NOT));}
					case -12:
						break;
					case 12:
						{return (new Symbol(sym.SEMICOLON));}
					case -13:
						break;
					case 13:
						{return (new Symbol(sym.COMMA));}
					case -14:
						break;
					case 14:
						{return (new Symbol(sym.PERIOD));}
					case -15:
						break;
					case 15:
						{return (new Symbol(sym.LPAREN));}
					case -16:
						break;
					case 16:
						{return (new Symbol(sym.RPAREN));}
					case -17:
						break;
					case 17:
						{return (new Symbol(sym.LBRACKET));}
					case -18:
						break;
					case 18:
						{return (new Symbol(sym.RBRACKET));}
					case -19:
						break;
					case 19:
						{return (new Symbol(sym.LBRACE));}
					case -20:
						break;
					case 20:
						{return (new Symbol(sym.RBRACE));}
					case -21:
						break;
					case 21:
						{return (new Symbol(sym.INTCONST));}
					case -22:
						break;
					case 22:
						{;}
					case -23:
						break;
					case 23:
						{return (new Symbol(sym.IF));}
					case -24:
						break;
					case 24:
						{yybegin(BLOCK_COMMENT);}
					case -25:
						break;
					case 25:
						{yybegin(SINGLE_COMMENT);}
					case -26:
						break;
					case 26:
						{return (new Symbol(sym.LTEQ));}
					case -27:
						break;
					case 27:
						{return (new Symbol(sym.COMP));}
					case -28:
						break;
					case 28:
						{return (new Symbol(sym.GTEQ));}
					case -29:
						break;
					case 29:
						{return (new Symbol(sym.DNE));}
					case -30:
						break;
					case 30:
						{return (new Symbol(sym.AND));}
					case -31:
						break;
					case 31:
						{return (new Symbol(sym.OR));}
					case -32:
						break;
					case 32:
						{return (new Symbol(sym.DOUBLECONST));}
					case -33:
						break;
					case 33:
						{String str =  yytext().substring(1,yytext().length() - 1);
                                                            Utility.ASSERT(str.length() == yytext().length() - 2);
                                                            return (new Symbol(sym.STRINGCONST));}
					case -34:
						break;
					case 34:
						{System.out.println();}
					case -35:
						break;
					case 35:
						{return (new Symbol(sym.NEW));}
					case -36:
						break;
					case 36:
						{return (new Symbol(sym.FOR));}
					case -37:
						break;
					case 37:
						{return (new Symbol(sym.INT));}
					case -38:
						break;
					case 38:
						{return (new Symbol(sym.INTCONST));}
					case -39:
						break;
					case 39:
						{return (new Symbol(sym.ELSE));}
					case -40:
						break;
					case 40:
						{return (new Symbol(sym.TRUE));}
					case -41:
						break;
					case 41:
						{return (new Symbol(sym.THIS));}
					case -42:
						break;
					case 42:
						{return (new Symbol(sym.VOID));}
					case -43:
						break;
					case 43:
						{return (new Symbol(sym.BREAK));}
					case -44:
						break;
					case 44:
						{return (new Symbol(sym.CLASS));}
					case -45:
						break;
					case 45:
						{return (new Symbol(sym.FALSE));}
					case -46:
						break;
					case 46:
						{return (new Symbol(sym.WHILE));}
					case -47:
						break;
					case 47:
						{return (new Symbol(sym.READLN));}
					case -48:
						break;
					case 48:
						{return (new Symbol(sym.RETURN));}
					case -49:
						break;
					case 49:
						{return (new Symbol(sym.STRING));}
					case -50:
						break;
					case 50:
						{return (new Symbol(sym.DOUBLE));}
					case -51:
						break;
					case 51:
						{return (new Symbol(sym.BOOL));}
					case -52:
						break;
					case 52:
						{return (new Symbol(sym.EXTENDS));}
					case -53:
						break;
					case 53:
						{return (new Symbol(sym.PRINTLN));}
					case -54:
						break;
					case 54:
						{return (new Symbol(sym.NEWARR));}
					case -55:
						break;
					case 55:
						{return (new Symbol(sym.INTERFACE));}
					case -56:
						break;
					case 56:
						{return (new Symbol(sym.IMPLEMENTS));}
					case -57:
						break;
					case 57:
						{;}
					case -58:
						break;
					case 58:
						{yybegin(YYINITIAL);}
					case -59:
						break;
					case 59:
						{;}
					case -60:
						break;
					case 60:
						{yybegin(YYINITIAL);}
					case -61:
						break;
					case 62:
						{return (new Symbol(sym.ID));}
					case -62:
						break;
					case 63:
						{return (new Symbol(sym.INTCONST));}
					case -63:
						break;
					case 64:
						{return (new Symbol(sym.DOUBLECONST));}
					case -64:
						break;
					case 65:
						{String str =  yytext().substring(1,yytext().length() - 1);
                                                            Utility.ASSERT(str.length() == yytext().length() - 2);
                                                            return (new Symbol(sym.STRINGCONST));}
					case -65:
						break;
					case 66:
						{;}
					case -66:
						break;
					case 67:
						{;}
					case -67:
						break;
					case 69:
						{return (new Symbol(sym.ID));}
					case -68:
						break;
					case 71:
						{return (new Symbol(sym.ID));}
					case -69:
						break;
					case 73:
						{return (new Symbol(sym.ID));}
					case -70:
						break;
					case 75:
						{return (new Symbol(sym.ID));}
					case -71:
						break;
					case 77:
						{return (new Symbol(sym.ID));}
					case -72:
						break;
					case 79:
						{return (new Symbol(sym.ID));}
					case -73:
						break;
					case 81:
						{return (new Symbol(sym.ID));}
					case -74:
						break;
					case 83:
						{return (new Symbol(sym.ID));}
					case -75:
						break;
					case 85:
						{return (new Symbol(sym.ID));}
					case -76:
						break;
					case 87:
						{return (new Symbol(sym.ID));}
					case -77:
						break;
					case 88:
						{return (new Symbol(sym.ID));}
					case -78:
						break;
					case 89:
						{return (new Symbol(sym.ID));}
					case -79:
						break;
					case 90:
						{return (new Symbol(sym.ID));}
					case -80:
						break;
					case 91:
						{return (new Symbol(sym.ID));}
					case -81:
						break;
					case 92:
						{return (new Symbol(sym.ID));}
					case -82:
						break;
					case 93:
						{return (new Symbol(sym.ID));}
					case -83:
						break;
					case 94:
						{return (new Symbol(sym.ID));}
					case -84:
						break;
					case 95:
						{return (new Symbol(sym.ID));}
					case -85:
						break;
					case 96:
						{return (new Symbol(sym.ID));}
					case -86:
						break;
					case 97:
						{return (new Symbol(sym.ID));}
					case -87:
						break;
					case 98:
						{return (new Symbol(sym.ID));}
					case -88:
						break;
					case 99:
						{return (new Symbol(sym.ID));}
					case -89:
						break;
					case 100:
						{return (new Symbol(sym.ID));}
					case -90:
						break;
					case 101:
						{return (new Symbol(sym.ID));}
					case -91:
						break;
					case 102:
						{return (new Symbol(sym.ID));}
					case -92:
						break;
					case 103:
						{return (new Symbol(sym.ID));}
					case -93:
						break;
					case 104:
						{return (new Symbol(sym.ID));}
					case -94:
						break;
					case 105:
						{return (new Symbol(sym.ID));}
					case -95:
						break;
					case 106:
						{return (new Symbol(sym.ID));}
					case -96:
						break;
					case 107:
						{return (new Symbol(sym.ID));}
					case -97:
						break;
					case 108:
						{return (new Symbol(sym.ID));}
					case -98:
						break;
					case 109:
						{return (new Symbol(sym.ID));}
					case -99:
						break;
					case 110:
						{return (new Symbol(sym.ID));}
					case -100:
						break;
					case 111:
						{return (new Symbol(sym.ID));}
					case -101:
						break;
					case 112:
						{return (new Symbol(sym.ID));}
					case -102:
						break;
					case 113:
						{return (new Symbol(sym.ID));}
					case -103:
						break;
					case 114:
						{return (new Symbol(sym.ID));}
					case -104:
						break;
					case 115:
						{return (new Symbol(sym.ID));}
					case -105:
						break;
					case 116:
						{return (new Symbol(sym.ID));}
					case -106:
						break;
					case 117:
						{return (new Symbol(sym.ID));}
					case -107:
						break;
					case 118:
						{return (new Symbol(sym.ID));}
					case -108:
						break;
					case 119:
						{return (new Symbol(sym.ID));}
					case -109:
						break;
					case 120:
						{return (new Symbol(sym.ID));}
					case -110:
						break;
					case 121:
						{return (new Symbol(sym.ID));}
					case -111:
						break;
					case 122:
						{return (new Symbol(sym.ID));}
					case -112:
						break;
					case 123:
						{return (new Symbol(sym.ID));}
					case -113:
						break;
					case 124:
						{return (new Symbol(sym.ID));}
					case -114:
						break;
					case 125:
						{return (new Symbol(sym.ID));}
					case -115:
						break;
					case 126:
						{return (new Symbol(sym.ID));}
					case -116:
						break;
					case 127:
						{return (new Symbol(sym.ID));}
					case -117:
						break;
					case 128:
						{return (new Symbol(sym.ID));}
					case -118:
						break;
					case 129:
						{return (new Symbol(sym.ID));}
					case -119:
						break;
					case 130:
						{return (new Symbol(sym.ID));}
					case -120:
						break;
					case 131:
						{return (new Symbol(sym.ID));}
					case -121:
						break;
					case 132:
						{return (new Symbol(sym.ID));}
					case -122:
						break;
					case 133:
						{return (new Symbol(sym.ID));}
					case -123:
						break;
					case 134:
						{return (new Symbol(sym.ID));}
					case -124:
						break;
					case 135:
						{return (new Symbol(sym.ID));}
					case -125:
						break;
					case 136:
						{return (new Symbol(sym.ID));}
					case -126:
						break;
					case 137:
						{return (new Symbol(sym.ID));}
					case -127:
						break;
					case 138:
						{return (new Symbol(sym.ID));}
					case -128:
						break;
					case 139:
						{return (new Symbol(sym.ID));}
					case -129:
						break;
					case 140:
						{return (new Symbol(sym.ID));}
					case -130:
						break;
					case 141:
						{return (new Symbol(sym.ID));}
					case -131:
						break;
					case 142:
						{return (new Symbol(sym.ID));}
					case -132:
						break;
					case 143:
						{return (new Symbol(sym.ID));}
					case -133:
						break;
					case 144:
						{return (new Symbol(sym.ID));}
					case -134:
						break;
					case 145:
						{return (new Symbol(sym.ID));}
					case -135:
						break;
					case 146:
						{return (new Symbol(sym.ID));}
					case -136:
						break;
					case 147:
						{return (new Symbol(sym.ID));}
					case -137:
						break;
					case 148:
						{return (new Symbol(sym.ID));}
					case -138:
						break;
					case 149:
						{return (new Symbol(sym.ID));}
					case -139:
						break;
					case 150:
						{return (new Symbol(sym.ID));}
					case -140:
						break;
					case 151:
						{return (new Symbol(sym.ID));}
					case -141:
						break;
					case 152:
						{return (new Symbol(sym.ID));}
					case -142:
						break;
					case 153:
						{return (new Symbol(sym.ID));}
					case -143:
						break;
					case 154:
						{return (new Symbol(sym.ID));}
					case -144:
						break;
					case 155:
						{return (new Symbol(sym.ID));}
					case -145:
						break;
					case 156:
						{return (new Symbol(sym.ID));}
					case -146:
						break;
					case 157:
						{return (new Symbol(sym.ID));}
					case -147:
						break;
					case 158:
						{return (new Symbol(sym.ID));}
					case -148:
						break;
					case 159:
						{return (new Symbol(sym.ID));}
					case -149:
						break;
					case 160:
						{return (new Symbol(sym.ID));}
					case -150:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
