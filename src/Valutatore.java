import library.NumberTok;
import library.Tag;
import library.Token;

import java.io.*;

public class Valutatore {
    private final Lexer lex;
    private final BufferedReader pbr;
    private Token look;

    public Valutatore(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF)
                move();
        } else {
            error("syntax error");
        }
    }

    public void start() {
        int a;
        if (look.tag != Tag.RELOP && look.tag != ')' && look.tag != ']' && look.tag != '}' && look.tag != ';' && look.tag!=',' && look.tag != Tag.ID){
            a = expr();
            System.out.println(a);
            match(Tag.EOF);
        }else {
            error("Error in start");
        }

    }

    private int expr() {
        if (look.tag != '+' && look.tag != '-' && look.tag != '*' && look.tag != '/'){
            return exprp(term());
        }else {
            error("Error in expr");
            return -1;
        }
    }

    private int exprp(int i) {
        int exprp = i;
        switch (look.tag) {
            case '+':
                match('+');
                exprp = exprp(i + term());
                break;

            case '-':
                match('-');
                exprp = exprp(i - term());
                break;

            case Tag.NUM:
            case '*':
            case '/':
            case ')':
            case ',':
            case ';':
            case Tag.RELOP:
            case Tag.TO:
            case Tag.EOF:
                break;

            default:
                error("Error in exprp");
                break;
        }
        return exprp;
    }

    private int term() {
        int term = 0;
        if (look.tag == Tag.NUM || look.tag == Tag.ID || look.tag == Token.lpt.tag){
            term = fact();
            if (look.tag == '*' || look.tag == '/') {
                term = termp(term);
            }
        }else {
            error("Error in TERM");
        }
        return term;
    }
    
    private int termp(int i) {
        int termp_k = i;
        switch (look.tag) {
            case '*':
                move();
                termp_k = i * fact();
                if (look.tag == '*' || look.tag == '/') {
                    termp_k = termp(termp_k);
                }
                break;

            case '/':
                move();
                termp_k = i / fact();
                if (look.tag == '*' || look.tag == '/') {
                    termp_k = termp(termp_k);
                }
                break;

            case Tag.EOF:
                System.out.println("Nothing after '*' or '/' ");
            default:
                break;
        }
        return termp_k;
    }

    private int fact() {
        int fact = 0;
        switch (look.tag) {
            case Tag.NUM: { // NUM
                fact = ((NumberTok)look).lex;
                move();
                break;
            }

            case Tag.ID: { // variable
                move();
                break;
            }

            case 40:{
                // Brackets
                move();
                if (look.tag == Token.rpt.tag) { // Empty brackets
                    System.err.println("Empty brackets");
                    move();
                    break;
                }

                fact = expr();
                if (look.tag != Token.rpt.tag) { // missing brackets
                    System.err.println("Close bracket missing");
                } else {
                    move();
                }
                break;
            }

            default:
                System.err.println("Error");
                break;
        }

        return fact;
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "src/code.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}
