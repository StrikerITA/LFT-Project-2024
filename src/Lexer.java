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
            this.peek = (char)br.read();
        } catch (IOException var3) {
            this.peek = '\uffff';
        }

    }

    public Token lexical_scan(BufferedReader br) {
        for(; this.peek == ' ' || this.peek == '\t' || this.peek == '\n' || this.peek == '\r'; this.readch(br)) {
            if (this.peek == '\n') {
                ++line;
            }
        }

        switch (this.peek) {
            case '!':
                this.peek = ' ';
                return Token.not;
            case '&':
                this.readch(br);
                if (this.peek == '&') {
                    this.peek = ' ';
                    return Word.and;
                }

                System.err.println("Erroneous character after & : " + this.peek);
                return null;
            case '(':
                this.peek = ' ';
                return Token.lpt;
            case ')':
                this.peek = ' ';
                return Token.rpt;
            case '*':
                this.peek = ' ';
                return Token.mult;
            case '+':
                this.peek = ' ';
                return Token.plus;
            case ',':
                this.peek = ' ';
                return Token.comma;
            case '-':
                this.peek = ' ';
                return Token.minus;
            case '/':
                this.peek = ' ';
                return Token.div;
            case ':':
                this.readch(br);
                if (this.peek == '=') {
                    this.peek = ' ';
                    return Word.init;
                }

                System.err.println("Erroneous character after & : " + this.peek);
                return null;
            case ';':
                this.peek = ' ';
                return Token.semicolon;
            case '<':
                this.readch(br);
                if (this.peek == '=') {
                    this.peek = ' ';
                    return Word.le;
                } else {
                    if (this.peek == '>') {
                        this.peek = ' ';
                        return Word.ne;
                    }

                    this.peek = ' ';
                    return Word.lt;
                }
            case '=':
                this.readch(br);
                if (this.peek == '=') {
                    this.peek = ' ';
                    return Word.eq;
                }

                System.err.println("Erroneous character after & : " + this.peek);
                return null;
            case '>':
                this.readch(br);
                if (this.peek == '=') {
                    this.peek = ' ';
                    return Word.ge;
                }

                this.peek = ' ';
                return Word.gt;
            case '[':
                this.peek = ' ';
                return Token.lpq;
            case ']':
                this.peek = ' ';
                return Token.rpq;
            case '{':
                this.peek = ' ';
                return Token.lpg;
            case '|':
                this.readch(br);
                if (this.peek == '|') {
                    this.peek = ' ';
                    return Word.or;
                }

                System.err.println("Erroneous character after & : " + this.peek);
                return null;
            case '}':
                this.peek = ' ';
                return Token.rpg;
            case '\uffff':
                return new Token(-1);
            default:
                String s;
                if (Character.isLetter(this.peek)) {
                    s = "" + this.peek;
                    this.readch(br);

                    while(Character.isLetter(this.peek)) {
                        s = s + this.peek;
                        this.readch(br);
                    }

                    switch (s) {
                        case "assign":
                            this.peek = ' ';
                            return Word.assign;
                        case "to":
                            this.peek = ' ';
                            return Word.to;
                        case "if":
                            this.peek = ' ';
                            return Word.iftok;
                        case "else":
                            this.peek = ' ';
                            return Word.elsetok;
                        case "do":
                            this.peek = ' ';
                            return Word.dotok;
                        case "for":
                            this.peek = ' ';
                            return Word.fortok;
                        case "begin":
                            this.peek = ' ';
                            return Word.begin;
                        case "end":
                            this.peek = ' ';
                            return Word.end;
                        case "print":
                            this.peek = ' ';
                            return Word.print;
                        case "read":
                            this.peek = ' ';
                            return Word.read;
                        default:
                            return (Token)(this.checkVar(s) ? new Word(257, s) : new Token(-1));
                    }
                } else if (!Character.isDigit(this.peek)) {
                    System.err.println("Erroneous character: " + this.peek);
                    return null;
                } else {
                    s = "" + this.peek;
                    this.readch(br);

                    while(Character.isDigit(this.peek)) {
                        s = s + this.peek;
                        this.readch(br);
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
                System.out.println("Scan: " + String.valueOf(tok));
            } while(tok.tag != -1);

            br.close();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
