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
        if (look.tag==Tag.ASSIGN || look.tag==Tag.PRINT || look.tag==Tag.READ || look.tag==Tag.FOR ||look.tag==Tag.IF || look.tag=='{') {
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
        int label_true, label_false;
        switch(look.tag) {
            case Tag.READ: {
                match(Tag.READ);
                code.emit(OpCode.invokestatic, 0);
                match('(');
                idlist();
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
                match(Token.lpt.tag); // aperta tonda
                exprlist();
                match(Token.rpt.tag); // tonda chiusa
                code.emit(OpCode.invokestatic, 1);
                break;
            }
            case Tag.FOR:{
                match(Tag.FOR);
                match(Token.lpt.tag); // bracket open

                int label = code.newLabel();
                label_true = code.newLabel();
                label_false = code.newLabel();
                forp(label, label_true, label_false);

                match(Token.rpt.tag);
                match(Tag.DO);
                code.emitLabel(label_true);
                stat(lnext_prog);
                code.emit(OpCode.GOto, label);
                code.emitLabel(label_false);
                break;
            }
            case Tag.IF:{
                match(Tag.IF);
                match(Token.lpt.tag);
                label_true = code.newLabel();
                label_false = code.newLabel();
                bexpr(label_true, label_false); // 5<3
                if (look.tag == Token.rpt.tag) { // )
                    match(Token.rpt.tag);
                    code.emitLabel(label_true);
                    stat(lnext_prog);
                    ifp(label_false);
                    match(Tag.END);
                } else {
                    error("Close bracket missing");
                }
                break;
            }
            case '{': {
                match(Token.lpg.tag);
                int nextl = code.newLabel();
                statlist(nextl);
                code.emitLabel(nextl);
                if (look.tag == '}') {
                    move();
                    break;
                } else {
                    error("Close bracket missing");
                }
                break;
            }
            default:{
                error("Error in stat");
                break;
            }
        }
    }

    private void statlist(int lnext_prog) {
        if(look.tag==Tag.ASSIGN || look.tag==Tag.PRINT || look.tag==Tag.READ || look.tag==Tag.FOR ||look.tag==Tag.IF || look.tag=='{') {
            int lnext = code.newLabel();
            stat(lnext);

            code.emit(OpCode.GOto, lnext); // GOTO label
            code.emitLabel(lnext); // Lx
            statlistp(lnext_prog);
            code.emit(OpCode.GOto, lnext_prog);
        }else{
            error("Error in statlist");
        }
    }

    private void statlistp(int lnext_prog) {
        switch (look.tag){
            case ';':
                match(Token.semicolon.tag);
                int lnext = code.newLabel();
                stat(lnext);
                code.emit(OpCode.GOto,lnext);
                code.emitLabel(lnext);
                statlistp(lnext_prog);
                break;
            case '}':
            case -1:
                break;
            default:
                error("Error in statlistp");
                break;
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

    private void ifp(int lnext_false){
        switch (look.tag){
            case Tag.ELSE: {
                move();
                code.emitLabel(lnext_false);
                stat(lnext_false);
                break;
            }
            case Tag.ASSIGN:
            case Tag.PRINT:
            case Tag.READ:
            case Tag.FOR:
            case Tag.IF:
            case '{':
            case Tag.END:
                break;
            default: {
                error("Error in IFP");
                break;
            }
        }
    }

    private void forp(int label, int label_true, int label_false){
        switch (look.tag){
            case Tag.ID -> {
                int id_addr = st.lookupAddress(((Word) look).lexeme); // <ID, var> -> return Address(var)
                if (id_addr == -1) { // address not found
                    id_addr = count;
                    st.insert(((Word) look).lexeme, count++); // create new addr in a SymbolTable
                }
                match(Tag.ID);
                match(Tag.INIT);
                expr();

                code.emit(OpCode.istore, id_addr);
                match(Token.semicolon.tag); //; of a := 6;

                code.emit(OpCode.GOto, label); //for label
                code.emitLabel(label);
                bexpr(label_true, label_false);
            }
            case Tag.RELOP -> {
                code.emit(OpCode.GOto, label);
                code.emitLabel(label);
                bexpr(label_true, label_false);
            }
            default -> error("Error in FORP");
        }

    }

    private void idlist() {
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
                code.emit(OpCode.istore, id_addr);
                code.emit(OpCode.pop);
            }else {
                code.emit(OpCode.istore, id_addr); // save val in id_addr
            }
            idlistp();
        }else{
            error("Error in idlist");
        }

    }

    private void idlistp() {
        switch (look.tag){
            case ',':
                move();

                int id_addr = st.lookupAddress(((Word) look).lexeme); // <ID, var> -> return Address(var)
                if (id_addr == -1) { // address not found
                    id_addr = count;
                    st.insert(((Word) look).lexeme, count++); // create new addr in a SymbolTable
                }
                match(Tag.ID);
                // if tag = , ID <idlistp> allora dup
                if (look.tag == Token.comma.tag) {
                    code.emit(OpCode.dup);
                    code.emit(OpCode.istore, id_addr);
                    code.emit(OpCode.pop);
                }else {
                    code.emit(OpCode.istore, id_addr); // save val in id_addr
                }

                match(Tag.ID);
                idlistp();
                break;
            case ')':
            case ']':
            default:
                break;
        }
    }

    private void bexpr(int label_true, int label_false) {
        String op =((Word) look).lexeme;
        match(Tag.RELOP);
        expr();
        expr();
        switch (op){
            case "<":{
                code.emit(OpCode.if_icmplt, label_true);
                break;
            }
            case ">":{
                //code.emitLabel(code.label);
                code.emit(OpCode.if_icmpgt, label_true);
                break;
            }
            case "<=":{
                code.emit(OpCode.if_icmple, label_true);
                break;
            }
            case ">=":{
                code.emit(OpCode.if_icmpge, label_true);
                break;
            }
            case "<>":{
                code.emit(OpCode.if_icmpne, label_true);
                break;
            }
            case "==":{
                code.emit(OpCode.if_icmpeq, label_true);
                break;
            }
            default: error("Error in BEXPR");
        }
        code.emit(OpCode.GOto, label_false);
    }

    private void expr() {
        switch (look.tag){
            case '+' -> {
                match(Token.plus.tag);
                match(Token.lpt.tag);
                exprlist();
                code.emit(OpCode.iadd);
                match(Token.rpt.tag);
            }
            case '-' -> {
                match(Token.lpt.tag);
                expr();
                expr();
                code.emit(OpCode.isub);
            }
            case '*' -> {
                match(Token.mult.tag);
                match(Token.lpt.tag);
                exprlist();
                code.emit(OpCode.imul);
                match(Token.rpt.tag);
            }
            case '/' -> {
                match(Token.div.tag);
                expr();
                expr();
                code.emit(OpCode.idiv);
            }
            case Tag.NUM -> {
                code.emit(OpCode.ldc, ((NumberTok)look).lex);
                match(Tag.NUM);
            }
            case Tag.ID -> {
                int id_addr = st.lookupAddress(((Word) look).lexeme); // <ID, var> -> return Address(var)
                if (id_addr == -1) { // address not found
                    id_addr = count;
                    st.insert(((Word) look).lexeme, count++); // create new addr in a SymbolTable
                }
                code.emit(OpCode.iload, id_addr);
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
