import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
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
        if (look.tag != Tag.RELOP && look.tag != ')' && look.tag != ']' && look.tag != '}' && look.tag != ';' && look.tag!=',' && look.tag != Tag.NUM && look.tag != Tag.ID) {
            int lnext_prog = code.newLabel();
            statlist(lnext_prog);
            code.emitLabel(lnext_prog);
            match(Tag.EOF);
            try {
                code.toJasmin();
            } catch (java.io.IOException e) {
                System.out.println("IO error\n");
            }
        }else {
            error("Error in prog");
        }
    }

    public void stat(int lnext_prog) {
        switch(look.tag) {
            case Tag.READ: {
                match(Tag.READ);
                code.emit(OpCode.invokestatic, 0);
                match('(');
                idlist(false);
                match(')');
                break;
            }
            case Tag.ASSIGN: {
                match(Tag.ASSIGN);
                assignlist();
                break;
            }
            case Tag.PRINT: {
                match(Tag.PRINT);
                code.emit(OpCode.invokestatic, 1);
                match(Token.lpt.tag); // aperta tonda
                exprlist();
                match(Token.rpt.tag); // tonda chiusa
                break;
            }
            case Tag.FOR:{
                move();
                match(Token.lpt.tag); // bracket open
                forp();
                match(Tag.DO);
                stat(lnext_prog);
                move();
                break;
            }
            case Tag.IF:{
                move();
                match(Token.lpt.tag);
                bexpr(); // 5<3
                if (look.tag == Token.rpt.tag) { // )
                    move();
                    stat(lnext_prog);
                    ifp(lnext_prog);
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
                statlist(lnext_prog);
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

    private void statlist(int lnext_prog) {
        stat(lnext_prog);
        statlistp(lnext_prog);
    }

    private void statlistp(int lnext_prog) {
        switch (look.tag){
            case ';':
                match(Token.semicolon.tag);
                code.emit(OpCode.GOto, lnext_prog);
                stat(lnext_prog);
                statlistp(lnext_prog);
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

    private void assignlistp() {
        switch (look.tag){
            case '[':{
                match(Token.lpq.tag);
                expr();
                match(Tag.TO);
                idlist(true);
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
            idlist(true);
            match(Token.rpq.tag);
            assignlistp();
        } else {
            error("Error assignList");
        }
    }

    private void ifp(int lnext_prog){
        switch (look.tag){
            case Tag.ELSE -> {
                move();
                stat(lnext_prog);
            }
            case ')'-> {}
            default -> error("Error in IFP");
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

    private void idlist(boolean cmd) {
        // use cmd to call assign or read
        if (look.tag == Tag.ID) {
            int id_addr = st.lookupAddress(((Word) look).lexeme); // <ID, var> -> return Address(var)
            if (id_addr == -1) { // address not found
                id_addr = count;
                st.insert(((Word) look).lexeme, count++); // create new addr in a SymbolTable
            }
            match(Tag.ID);
            // if tag = , ID <idlistp> allora dup
            if (look.tag == Token.comma.tag) {
                code.emit(OpCode.dup);
            }
            code.emit(OpCode.istore, id_addr); // save val in id_addr
            idlistp();
        }else{
            error("Error in idlist");
        }

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

    private void expr() {
        switch (look.tag){
            case '+' -> {
                match(Token.plus.tag);
                match(Token.lpt.tag);
                exprlist();
                match(Token.rpt.tag);
                code.emit(OpCode.iadd);
            }
            case '-' -> {
                match(Token.minus.tag);
                expr();
                expr();
                code.emit(OpCode.isub);
            }
            case '*' -> {
                match(Token.mult.tag);
                match(Token.lpt.tag);
                exprlist();
                match(Token.rpt.tag);
                code.emit(OpCode.imul);
            }
            case '/' -> {
                match(Token.div.tag);
                expr();
                expr();
                code.emit(OpCode.idiv);
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

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "src/code.txt"; // file path
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
