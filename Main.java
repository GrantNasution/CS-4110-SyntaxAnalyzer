import java.lang.System;
import java.io.FileReader;

public class Main {
    public static void main(String argv[]) throws java.io.IOException, Exception{
        parser p = new parser(new Yylex(new FileReader(argv[0])));
        p.parse();
    }
}