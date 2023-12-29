import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class Lexer {
    public static int line = 1;
    private boolean comOpen = false;
    private boolean comment = false;
    private char peek = ' ';

    public Lexer() {}

    private boolean checkVar(String s) {
        int state = 0;
        int i = 0;

        while (state != 1 && i < s.length()) {
            final char ch = s.charAt(i++);

            switch (state) {
                case 0 -> {
                    if (ch == '_')
                        state = 2;
                    else if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z'))
                        state = 1;
                    else
                        state = -1; //pozzo
                }
                case 2 -> {
                    if (ch == '_')
                        state = 2;
                    else if ((ch>='a' && ch<='z') || (ch>='A' && ch<='Z'))
                        state = 1;
                    else
                        state = -1;
                }
                default -> {
                    state = -1;
                }

            }
        }
        return state == 1;
    }

    private void readch(BufferedReader br) {
        try {
            peek = (char)br.read();
        } catch (IOException var3) {
            peek = '\uffff';
        }
    }

    public Token lexical_scan(BufferedReader br) {
        for(; peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r'; readch(br)) {
            if (peek == '\n') {
                ++line;
                comment = false; //reset the flag before changing line
                if (comOpen){
                    System.err.println("Comment not closed");
                    return null;
                }
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
                readch(br);
                if (peek == '/' && comOpen){
                    peek = ' ';
                    comOpen = false;
                    return new Token(0);
                }else if(peek == '/'){
                    peek = ' ';
                    return new Token(-2); // return token mult e div
                }

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
                peek = ' ';
                readch(br);
                if (peek == '*') {
                    peek = ' ';
                    comOpen = true;
                    return new Token(0);
                    //tag 0 is not printed
                } else if (peek == '/') {
                    peek = ' ';
                    comment = true;
                    return new Token(0);
                    //tag 0 is not printed
                }else{
                    return Token.div;
                }

            case ':':
                readch(br);
                if (peek == '=') {
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

                    peek = ' ';
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

                peek = ' ';
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
                if (Character.isLetter(peek) || peek == '_') {
                    s = "" + peek;
                    readch(br);

                    while(Character.isLetter(peek)) {
                        s = s + peek;
                        readch(br);
                    }

                    return switch (s) {
                        case "assign" -> {
                            peek = ' ';
                            yield Word.assign;
                        }
                        case "to" -> {
                            peek = ' ';
                            yield Word.to;
                        }
                        case "if" -> {
                            peek = ' ';
                            yield Word.iftok;
                        }
                        case "else" -> {
                            peek = ' ';
                            yield Word.elsetok;
                        }
                        case "do" -> {
                            peek = ' ';
                            yield Word.dotok;
                        }
                        case "for" -> {
                            peek = ' ';
                            yield Word.fortok;
                        }
                        case "begin" -> {
                            peek = ' ';
                            yield Word.begin;
                        }
                        case "end" -> {
                            peek = ' ';
                            yield Word.end;
                        }
                        case "print" -> {
                            peek = ' ';
                            yield Word.print;
                        }
                        case "read" -> {
                            peek = ' ';
                            yield Word.read;
                        }
                        default -> {
                            if (comOpen || comment) { // it's a comment?
                                //tag 0 is not printed
                                yield new Token(0);
                            }else if (checkVar(s)){ //is a variable?
                                yield new Word(257, s);
                            }else { // no comments, no variable
                                System.err.println("Erroneous character: " + peek);
                                yield null;
                            }

                        }
                    };
                } else if (!Character.isDigit(peek)) {
                    System.err.println("Erroneous character: " + peek);
                    return null;
                } else {
                    s = "" + peek;
                    readch(br);

                    while(Character.isDigit(peek)) {
                        s = s + peek;
                        readch(br);
                    }

                    try {
                        int num = Integer.parseInt(s);
                        if (comOpen || comment) { // it's a comment?
                            //tag 0 is not printed
                            return new Token(0);
                        }else{ //is a variable?
                            return new NumberTok(256, num);
                        }

                    } catch (NumberFormatException var5) {
                        var5.printStackTrace();
                        return new NumberTok(-1, -1);
                    }
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
                tok = lex.lexical_scan(br);
                if (tok.tag != 0){
                    if(tok.tag == -2){
                        System.out.println("Scan: "+ Token.mult);
                        System.out.println("Scan: " + Token.div);
                    }else{
                        System.out.println("Scan: " + tok);
                    }
                }
            } while(tok.tag != -1);

            br.close();
        } catch (IOException ioEXC) {
            ioEXC.printStackTrace();
        }

    }
}
