public class Word extends Token {
    public String lexeme = "";
    public static final Word assign = new Word(259, "assign");
    public static final Word to = new Word(260, "to");
    public static final Word iftok = new Word(261, "if");
    public static final Word elsetok = new Word(262, "else");
    public static final Word dotok = new Word(263, "do");
    public static final Word fortok = new Word(264, "for");
    public static final Word begin = new Word(265, "begin");
    public static final Word end = new Word(266, "end");
    public static final Word print = new Word(267, "print");
    public static final Word read = new Word(268, "read");
    public static final Word init = new Word(269, ":=");
    public static final Word or = new Word(270, "||");
    public static final Word and = new Word(271, "&&");
    public static final Word lt = new Word(258, "<");
    public static final Word gt = new Word(258, ">");
    public static final Word eq = new Word(258, "==");
    public static final Word le = new Word(258, "<=");
    public static final Word ne = new Word(258, "<>");
    public static final Word ge = new Word(258, ">=");

    public Word(int tag, String s) {
        super(tag);
        this.lexeme = s;
    }

    public String toString() {
        return "<" + this.tag + ", " + this.lexeme + ">";
    }
}
