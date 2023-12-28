import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';

    public Lexer() {}

    private boolean checkVar(String s) {
        int state = 48;
        int i = 0;

        while(state != 1 && i < s.length()) {
            char ch = s.charAt(i++);
            switch (state) {
                case 2:
                    if (ch == '_') {
                        break;
                    }

                    if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                        state = -1;
                        break;
                    }

                    state = 1;
                    break;
                case 48:
                    if (ch == '_') {
                        state = 2;
                    } else if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                        state = -1;
                    } else {
                        state = 1;
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
                peek = ' ';
                return Token.div;
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
                if (Character.isLetter(peek)) {
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
                        default -> (Token) (checkVar(s) ? new Word(257, s) : new Token(-1));
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
                        return new NumberTok(256, num);
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
                System.out.println("Scan: " + tok);
            } while(tok.tag != -1);

            br.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
