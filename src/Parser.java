import library.Tag;
import library.Token;

import java.io.*;
public class Parser {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer l, BufferedReader br) {
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

    public void prog() {
        if (look.tag != Tag.RELOP && look.tag != ')' && look.tag != ']' && look.tag != '}' && look.tag != ';' && look.tag!=',' && look.tag != Tag.NUM && look.tag != Tag.ID){
            statlist();
            match(Tag.EOF);
        }else {
            error("Error in prog");
        }
    }

    private void statlist() {
        stat();
        statlistp();
    }

    private void statlistp() {
        switch (look.tag){
            case ';':
                match(Token.semicolon.tag);
                stat();
                statlistp();
                break;
            case ')':
            case ']':
            case '}':
            case ',':
            case -1:
                break;
            default:
                error("Error in statlistp");
                break;
        }
    }

    private void stat() {
        switch (look.tag) {
            case Tag.ASSIGN: {
                match(Tag.ASSIGN);
                assignlist();
                break;
            }

            case Tag.PRINT:{
                move();
                match(Token.lpt.tag); // aperta tonda
                exprlist();
                match(Token.rpt.tag); // tonda chiusa
                break;
            }

            case Tag.READ:{
                move();
                if (look.tag == Token.lpt.tag) {
                    move();
                    idlist();
                    if (look.tag == Token.rpt.tag) {
                        move();
                        break;
                    } else {
                        error("Close bracket missing");
                    }
                }
                break;
            }

            case Tag.FOR:{
                move();
                match(Token.lpt.tag); // bracket open
                forp();
                match(Tag.DO);
                stat();
                move();
                break;
            }

            case Tag.IF:{
                move();
                match(Token.lpt.tag);
                bexpr(); // 5<3
                if (look.tag == Token.rpt.tag) { // )
                    move();
                    stat();
                    ifp();
                    if (look.tag == Tag.END){
                        move();
                    }
                } else {
                    error("Close bracket missing");
                }
                break;
            }

            case '{': {
                move();
                statlist();
                if (look.tag == '}') {
                    move();
                    break;
                } else {
                    error("Close bracket missing");
                }
                break;
            }

            default:{
                break;
            }
        }
    }

    private void ifp(){
        switch (look.tag){
            case Tag.ELSE -> {
                move();
                stat();
            }
            case ')'-> {}
            default -> {
                error("Error in IFP");
            }
        }
    }

    private void forp(){
        switch (look.tag){
            case Tag.ID -> {
                match(Tag.ID);
                match(Tag.INIT);
                expr();
                match(Token.semicolon.tag);
                bexpr();
                match(Token.rpt.tag); // bracket closed ?
            }
            case Tag.RELOP -> {
                bexpr();
                match(Token.rpt.tag); // bracket closed ?
            }
            default -> error("Error in FORP");
        }

    }

    private void assignlistp() {
        switch (look.tag){
            case '[':{
                match(Token.lpq.tag);
                expr();
                match(Tag.TO);
                idlist();
                match(Token.rpq.tag);
                assignlistp();
            }
            case ';':
            case '}':
            case Tag.END:
            case Tag.ELSE:
            case Tag.EOF:
                break;
            default:
                error("Error in assignlistp");
        }
    }

    private void assignlist() {
        if (look.tag == Token.lpq.tag){
            match(Token.lpq.tag);
            expr();
            match(Tag.TO);
            idlist();
            match(Token.rpq.tag);
            assignlistp();
        } else {
            error("Error assignList");
        }
    }

    private void idlist() {
        match(Tag.ID);
        idlistp();
    }

    private void idlistp() {
        switch (look.tag){
            case ',':
                move();
                match(Tag.ID);
                idlistp();
                break;
            case ')':
            case ']':
            default:
                break;
        }
    }

    private void bexpr() {
        match(Tag.RELOP);
        expr();
        expr();
    }

    private void exprlist() {
        switch (look.tag){
            case '+':
            case '-':
            case '*':
            case '/':
            case Tag.NUM:
            case Tag.ID:
                expr();
                exprlistp();
                break;
            default:
                error("Error in exprlist");
        }

    }

    private void exprlistp() {
        switch (look.tag){
            case ',':
                match(Token.comma.tag);
                expr();
                exprlistp();
                break;
            case ')':
                break;
            default:
                error("error in exprlistp");
                break;
        }
    }
    
    private void expr() {
        switch (look.tag){
            case '+' -> {
                match(Token.plus.tag);
                match(Token.lpt.tag);
                exprlist();
                match(Token.rpt.tag);
            }
            case '-' -> {
                match(Token.minus.tag);
                expr();
                expr();
            }
            case '*' -> {
                match(Token.mult.tag);
                match(Token.lpt.tag);
                exprlist();
                match(Token.rpt.tag);
            }
            case '/' -> {
                match(Token.div.tag);
                expr();
                expr();
            }
            case Tag.NUM -> {
                match(Tag.NUM);
            }
            case Tag.ID -> {
                match(Tag.ID);
            }
            default -> error("Error in expr");
        }
    }

    public static void main (String[]args){
        Lexer lex = new Lexer();
        String path = "src/code.txt"; // file path
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
