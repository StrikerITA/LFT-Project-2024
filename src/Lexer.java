import library.NumberTok;
import library.Tag;
import library.Token;
import library.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {
    public static int line = 1;
    private boolean comment = false;
    private char peek = ' ';

    private boolean checkVar(String s) {
        int state = 0;
        int i = 0;

        while (state >= 0 && i < s.length()) {
            final char ch = s.charAt(i++);

            switch (state) {
                case 0 -> {
                    if (Character.isDigit(ch)){
                        state = 1;
                    }else if (ch == '_'){
                        state = 3;
                    } else if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')){
                        state = 2;
                    } else{
                        state = -1; //pozzo
                    }
                }
                case 1 -> { //stato pozzo
                    if (Character.isDigit(ch)){
                        state = 1;
                    } else if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')) {
                        state = 1;
                    }else if (ch == '_'){
                        state = 1;
                    } else{
                        state = -1;
                    }
                }
                case 2 -> {
                    if (Character.isDigit(ch)){
                        state = 2;
                    } else if (ch == '_') {
                        state = 2;
                    } else if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z')) {
                        state = 2;
                    } else {
                        state = -1;
                    }
                }
                case 3 -> {
                    if (Character.isDigit(ch))
                        state = 2;
                    else if ((ch >= 97 && ch <= 122) || (ch >= 65 && ch <= 90))
                        state = 2;
                    else if (ch == '_')
                        state = 3;
                    else
                        state = -1;
                }

            }
        }
        return state == 2;
    }

    private void readch(BufferedReader br) {
        try {
            peek = (char)br.read();
        } catch (IOException var3) {
            peek = (char) -1;
        }
    }

    public Token lexical_scan(BufferedReader br) {
        for(; peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r'; readch(br)) {
            if (peek == '\n') {
                ++line;
            }
        }

        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                }

                System.err.println("Erroneous character after '&' : " + peek);
                return null;
            case '(':
                peek = ' ';
                return Token.lpt;
            case ')':
                peek = ' ';
                return Token.rpt;
            case '*':
                peek = ' ';
                return Token.mult;

            case '+':
                peek = ' ';
                return Token.plus;
            case ',':
                peek = ' ';
                return Token.comma;
            case '-':
                peek = ' ';
                return Token.minus;
            case '/':
                readch(br);
                if(Character.isLetterOrDigit(peek)){
                    return Token.div;
                }
                else {
                    if(peek == '/'){
                        while(peek != '\n' && peek != (char)-1){
                            readch(br);
                        }
                    } else if(peek == '*'){
                        do {
                            do {
                                readch(br);
                            }
                            while(peek!='*');
                            readch(br);
                            while(peek == '*'){
                                readch(br);
                            }
                        }
                        while(peek != '/');
                    } else {
                        System.err.println("Error, comment was not closed before end of file");
                        return null;
                    }
                    readch(br);
                    return lexical_scan(br);
                }

            case ':':
                readch(br);
                if (peek == '='){
                    peek = ' ';
                    return Word.init;
                }

                System.err.println("Erroneous character after ':' : " + peek);
                return null;
            case ';':
                peek = ' ';
                return Token.semicolon;
            case '<':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.le;
                } else {
                    if (peek == '>') {
                        peek = ' ';
                        return Word.ne;
                    }

                    return Word.lt;
                }
            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                }

                System.err.println("Erroneous character after '=' : " + peek);
                return null;

            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                }

                return Word.gt;
            case '[':
                peek = ' ';
                return Token.lpq;
            case ']':
                peek = ' ';
                return Token.rpq;
            case '{':
                peek = ' ';
                return Token.lpg;
            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                }

                System.err.println("Erroneous character after '|' : " + peek);
                return null;
            case '}':
                peek = ' ';
                return Token.rpg;

            case '\uffff':
                return new Token(-1);

            default:
                String s;
                if (Character.isLetter(peek) || peek == '_' || Character.isDigit(peek)) {
                    s = "";

                    while(Character.isLetter(peek) || peek == '_' || Character.isDigit(peek)) {
                        s = s + peek;
                        readch(br);
                    }
                    if (s.matches("-?\\d+")){
                        return new NumberTok(Tag.NUM, Integer.parseInt(s));
                    } else {
                        return switch (s) {
                            case "assign" -> Word.assign;
                            case "to" -> Word.to;
                            case "if" -> Word.iftok;
                            case "else" -> Word.elsetok;
                            case "do" -> Word.dotok;
                            case "for" -> Word.fortok;
                            case "begin" -> Word.begin;
                            case "end" -> Word.end;
                            case "print" -> Word.print;
                            case "read" -> Word.read;
                            default -> {
                                if (checkVar(s)){
                                    //is a variable?
                                    yield new Word(Tag.ID, s);
                                }else {
                                    // no comments, no variable
                                    System.err.println("Erroneous character: " + peek);
                                    yield null;
                                }
                            }
                        };
                    }
                } else if (Character.isDigit(peek) || peek == '_'){
                    s = "" + peek;
                    readch(br);

                    while(Character.isDigit(peek) || peek == '_' || Character.isLetter(peek)) {
                        s = s + peek;
                        readch(br);
                    }

                    try {
                        return new NumberTok(256, Integer.parseInt(s));
                    } catch (NumberFormatException var5) {
                        var5.printStackTrace();
                        return new Token(Tag.EOF);
                    }
                } else {
                    System.err.println("Erroneous character: " + peek);
                    return null;
                }
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "src/code.txt";

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br); // take token
                System.out.println("Scan: " + tok);
            } while(tok.tag != -1);

            br.close();
        } catch (IOException ioEXC) {
            ioEXC.printStackTrace();
        }

    }
}
