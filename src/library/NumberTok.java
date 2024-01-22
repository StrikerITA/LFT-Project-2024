package library;

public class NumberTok extends Token {
    public int lex;

    public NumberTok(int tag, int s) {
        super(tag);
        this.lex = s;
    }

    public String toString() {
        return "<" + this.tag + ", " + this.lex + ">";
    }
}
