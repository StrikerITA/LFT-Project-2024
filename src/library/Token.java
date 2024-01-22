package library;

public class Token {
    public final int tag;
    public static final Token not = new Token(33);
    public static final Token lpt = new Token(40);
    public static final Token rpt = new Token(41);
    public static final Token lpq = new Token(91);
    public static final Token rpq = new Token(93);
    public static final Token lpg = new Token(123);
    public static final Token rpg = new Token(125);
    public static final Token plus = new Token(43);
    public static final Token minus = new Token(45);
    public static final Token mult = new Token(42);
    public static final Token div = new Token(47);
    public static final Token semicolon = new Token(59);
    public static final Token comma = new Token(44);

    public Token(int t) {
        this.tag = t;
    }

    public String toString() {
        return "<" + this.tag + ">";
    }
}
