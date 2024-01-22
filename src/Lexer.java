import library.NumberTok;
import library.Tag;
import library.Token;
import library.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {
    public static int line = 1;
    private boolean comOpen = false;
    private boolean comment = false;
    private char peek = ' ';

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
                if (peek == '*') {
                    peek = ' ';
                    comOpen = true;
                    while(comOpen){
                        readch(br);
                        if (peek == '*'){
                            readch(br);
                            if (peek == '/'){
                                peek = ' ';
                                comOpen = false;
                            } else if (peek == (char) -1) {
                                break;
                            }
                        } else if (peek == (char) -1) {
                            break;
                        }
                    }
                    return lexical_scan(br);
                } else if (peek == '/') {
                    peek = ' ';
                    comment = true;
                    return lexical_scan(br);
                }else{
                    if (comOpen || comment){
                        System.err.println("Comment not closed");
                        return null;
                    }else {
                        return Token.div;
                    }

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
                if (Character.isLetter(peek) || peek == '_') {
                    s = "" + peek;
                    readch(br);

                    while(Character.isLetter(peek) || peek == '_' || Character.isDigit(peek)) {
                        s = s + peek;
                        readch(br);
                    }

                    return switch (s) {
                        case "assign" -> {
                            yield Word.assign;
                        }
                        case "to" -> {
                            yield Word.to;
                        }
                        case "if" -> {
                            yield Word.iftok;
                        }
                        case "else" -> {
                            yield Word.elsetok;
                        }
                        case "do" -> {
                            yield Word.dotok;
                        }
                        case "for" -> {
                            yield Word.fortok;
                        }
                        case "begin" -> {
                            yield Word.begin;
                        }
                        case "end" -> {
                            yield Word.end;
                        }
                        case "print" -> {
                            yield Word.print;
                        }
                        case "read" -> {
                            yield Word.read;
                        }
                        default -> {
                            if (comOpen || comment) {
                                /* it's a comment?
                                   tag 0 is not printed */
                                yield lexical_scan(br);
                            }else if (checkVar(s)){
                                //is a variable?
                                yield new Word(Tag.ID, s);
                            }else {
                                // no comments, no variable
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
                        if (comOpen || comment) {
                            // it's a comment?
                            
                            return lexical_scan(br);
                        }else{ //is a num?
                            return new NumberTok(256, num);
                        }

                    } catch (NumberFormatException var5) {
                        var5.printStackTrace();
                        return new Token(Tag.EOF);
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
                tok = lex.lexical_scan(br); // take token
                if (tok.tag != 0){ // is a comment?
                    if(tok.tag == -2){ // is a */ or not ?
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
