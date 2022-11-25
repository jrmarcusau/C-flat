import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Parser takes in tokens and analyzes their syntax, creating abstract syntax trees for each statement to be easily evaluated and executed by <code>Interpreter</code>.
 * 
 * @author Kevin
 * @version 1.5
 */
public class Parser {
    // Sentinel class to signal an error and enter Panic Mode
    private static class ParseError extends RuntimeException {};

    private Tokenizer tokenizer;
    private Token current;
    private Token lookahead;

    /**
     * Constructor with one parameter.
     * 
     * @param tokenizer Tokenizer that feeds the <code>Parser</code> a list of <code>Tokens</code>
     */
    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        lookahead = tokenizer.next();
    }

    // Increments cursor in tokenizer, updating current and lookahead tokens
    private Token advance() {
        if (endOfFile()) return null;
        current = lookahead;
        lookahead = tokenizer.next();
        return current;
    }
    
    // Advances cursor and returns true if lookahead token type matches any in parameters
    private boolean match(Type... types) {
        if (endOfFile()) return false;
        for (Type type : types) {
            if (lookahead.type() == type) {
                advance();
                return true;
            } 
        } return false;
        
    }

    // Overloaded method to match string values and advance
    private boolean match(Type type, String s, String... ss) {
        if (lookahead.type() != type) return false;
        if (s.equals(lookahead.value())) {
            advance();
            return true;
        }
        for (String str : ss) {
            if (str.equals(lookahead.value())) {
                advance();
                return true;
            }
        } return false;
    }

    // Blatantly obvious
    private boolean endOfFile() {
        return lookahead.type() == Type.EOF;
    }
    
    // Voraciously consumes an expected token, but cookie monster gets mad if the token is not the right cookie
    private Token expect(Type type, String error) {
        if (!match(type)) throw error(lookahead, error);
        return current;
    }

    private ParseError error(Token token, String message) {
        Cflat.parsetimeError(token, message);
        Cflat.hasError = true;
        return new ParseError();
    }

    ////////////////////////// Recursive Descent //////////////////////////

    /**
     * Parses.
     * 
     * @return a <code>List</code> of <code>Statement</code>s for the interpreter
     */
    public List<Statement> parse() {
        LinkedList<Statement> statements = new LinkedList<>();
        while (!endOfFile()) {
            statements.add(statement());
        }
        return statements;
    }

    ////////////////////////// Statement //////////////////////////

    // Entry into all Statements
    private Statement statement() {
        return declaration();
    }

    /* Declarations are non-nesting statements.
    declarations can be in a block but cannot be an statement subsequent to a branch or loop
    declaration := functionDeclaration | variableDeclaration | statement */
    private Statement declaration() {
        try {
            switch (lookahead.type()) {
                case IMPORT:    { advance(); return importDeclaration(); }
                case FUNC:      { advance(); return functionDeclaration(true); }
                case VOID:      { advance(); return functionDeclaration(false); }
                case VAR:       { advance(); return variableDeclaration(); }
                case ARR:       { advance(); return arrayDeclaration(); }

                default: { return notDeclaration(); }
            }
        } catch (ParseError e) {
            rerail();  
            return null;
        }
    }

    // All other nesting statements
    private Statement notDeclaration() {
        try {
            switch (lookahead.type()) {
                case LBRACE:    { advance(); return new Statement.Block(block()); }
                case IF:        { advance(); return ifStatement(); }
                case WHILE:     { advance(); return whileStatement(); }
                case FOR:       { advance(); return forStatement(); }
                case SWITCH:    { advance(); return switchStatement(); }
                case RETURN:    { advance(); return returnStatement(); }
                case BREAK:     { advance(); return breakStatement(); }
                
                default:        { return exprStatement(); }
            }
        } catch (ParseError e) {
            rerail();
            return null;
        }
    }
    
    // Constructs a block as a list of statements
    private List<Statement> block() {
        LinkedList<Statement> statements = new LinkedList<>(); 
        while (lookahead.type() != Type.RBRACE && !endOfFile()) {
            statements.add(statement());
        }

        expect(Type.RBRACE, "expect '}' after block");
        return statements;
    }

    private Statement.ImportDeclaration importDeclaration() {
        List<Token> imports = new LinkedList<>();
        do {
            imports.add(expect(Type.IDENTIFIER, "expect import name"));
        } while (match(Type.COMMA));
        expect(Type.SEMICOLON, "expect ';' after import statement");
        return new Statement.ImportDeclaration(imports);
    }

    private Statement.FunctionDeclaration functionDeclaration(boolean returns) {
        Token name = expect(Type.IDENTIFIER, "expect function name");
        expect(Type.LPAREN, "expect '(' after function name");
        List<Token> params = new LinkedList<>();
        if (lookahead.type() != Type.RPAREN) {
            do {
                if (params.size() > 63) { error(lookahead, "can't have more than 63 parameters"); }
                params.add(expect(Type.IDENTIFIER, "expect parameter name"));
            } while (match(Type.COMMA));
        }
        expect(Type.RPAREN, "expect ')' after parameters");
        expect(Type.LBRACE, "expect '{' before function body");
        List<Statement> body = block();
        return new Statement.FunctionDeclaration(name, params, body, returns);
    }

    // Constructs a variable declaration statement AST node
    private Statement variableDeclaration() {
        expect(Type.IDENTIFIER, "expect variable name");
        Token name = current;
        Expression initializer = null;
        if (match(Type.OPERATOR, "=")) {
            initializer = expression();
        }
        expect(Type.SEMICOLON, "expect ';' after variable declaration");
        return new Statement.VariableDeclaration(name, initializer);
    }

    // Constructs an array declaration statement AST node
    private Statement arrayDeclaration() {
        expect(Type.IDENTIFIER, "expect array name");
        Token name = current;
        ArrayList<Expression> array = new ArrayList<>();;
        if (match(Type.OPERATOR, "=")) {
            expect(Type.LBRACE, "expect '{' to begin array literal");
            if (lookahead.type() != Type.RBRACE) {
                do {
                    array.add(expression());
                } while (match(Type.COMMA));
            }
            expect(Type.RBRACE, "expect '}' after elements");
        }
        expect(Type.SEMICOLON, "expect ';' after array declaration");
        return new Statement.ArrayDeclaration(name, array);
    }

    // Constructs a return statement AST node
    private Statement returnStatement() {
        Token keyword = current;
        Expression value = null;
        if (lookahead.type() != Type.SEMICOLON) {
            value = expression();
        }
        expect(Type.SEMICOLON, "expect ';' after return value");
        return new Statement.Return(keyword, value);
    }

    // Constructs a break statement AST node
    private Statement breakStatement() {
        Token keyword = current;
        Expression value = null;
        if (lookahead.type() != Type.SEMICOLON) {
            value = expression();
        }
        expect(Type.SEMICOLON, "expect ';' after break value");
        return new Statement.Break(keyword, value);
    }

    // Constructs an "if" statement AST node
    private Statement ifStatement() {
        expect(Type.LPAREN, "expect '(' after 'if'");
        Expression condition = expression();
        expect(Type.RPAREN, "expect ')' after condition");
        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(Type.ELSE)) {
                elseBranch = statement();
        }
        return new Statement.If(condition, thenBranch, elseBranch);
    }

    // Constructs an "while" statement AST node
    private Statement whileStatement() {
        expect(Type.LPAREN, "expect '(' after 'while'");
        Expression condition = expression();
        expect(Type.RPAREN, "expect ')' after condition");
        Statement body = statement();
        return new Statement.While(condition, body);
    }

    // Constructs a "for" statement AST node
    private Statement forStatement() { //check
        expect(Type.LPAREN, "expect '(' after 'for'");
        Statement initializer;
        if (match(Type.SEMICOLON)) {
            initializer = null;
        } else if (match(Type.VAR)) {
            initializer = variableDeclaration();
        } else {
            initializer = exprStatement();
        }
        Expression condition = null;
        if (!match(Type.SEMICOLON)) {
            condition = expression();
        }
        expect(Type.SEMICOLON, "expect ';' after loop condition");
        Expression increment = null;
        if (!match(Type.RPAREN)) {
            increment = expression();
            expect(Type.RPAREN, "expect ')' after for clause'");
        }

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(Arrays.asList(body, new Statement.Expr(increment)));
        }
        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);
        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }
        return body;
    }

    // Constructs a "switch" statement AST node
    private Statement switchStatement() {
        expect(Type.LPAREN, "expect '(' after 'switch'");
        Expression switcher = expression();
        expect(Type.RPAREN, "expect ')' after switched value");
        expect(Type.LBRACE, "expect '{' after switch header");
        List<Expression> switchees = new LinkedList<>();
        List<Statement> cases = new LinkedList<>();
        Statement defaultStmt;
        while (match(Type.CASE)) {
            switchees.add(expression());
            expect(Type.COLON, "expect ':' after 'case'");
            cases.add(statement());
        }
        if (match(Type.DEFAULT)) {
            expect(Type.COLON, "expect ':' after 'default'");
            defaultStmt = statement();
        } else { defaultStmt = null; }
        expect(Type.RBRACE, "expect '}' after switch body");
        return new Statement.Switch(switcher, switchees, cases, defaultStmt);
    }

    // Constructs an expr wrapped as a statement AST node
    private Statement exprStatement() {
        Expression expr = expression();
        expect(Type.SEMICOLON, "expect ';' after expression statement");
        return new Statement.Expr(expr);
    }

    ////////////////////////// Expression //////////////////////////

    // Entry into all Expressions
    private Expression expression() { //DONE
        return assignment();
    }
 
    // assignment := IDENTIFIER "=" assignment | ternary
    private Expression assignment() {
        Expression expr = ternary();
        if (lookahead.type() != Type.OPERATOR) return expr;
        String operator = (String) lookahead.value();
        Token op = null;
        switch (operator) {
            case "+=":
            case "-=":
            case "*=":
            case "/=":
            case "%=":
            case "&=":
            case "|=":
            case "^=":
            case "<<=":
            case ">>=":
            case ">>>=": {
                op = new Token(Type.OPERATOR, operator.substring(0,operator.indexOf("=")), 
                               lookahead.line(), lookahead.file());
            }
            case "=": {
                advance();
                Expression value = assignment();
                if (expr instanceof Expression.Variable) {
                    if (!operator.equals("=")) value = new Expression.Binary(expr, op, value);
                    Token target = ((Expression.Variable) expr).name;
                    return new Expression.Assignment(target, value);
                } else if (expr instanceof Expression.Index) {
                    if (!(operator.equals("=") || operator.equals("+="))) value = new Expression.Binary(expr, op, value);
                    Expression.Index target = (Expression.Index) expr;
                    if (target.colon != null) error(target.colon, "cannot do multiple assignment");
                    return new Expression.AssignAt(target.name, target.array, operator, target.start, value);
                } else {
                    error(lookahead, "invalid assignment target");
                    return null;
                }
            }
        }
        return expr;
    }

    // ternary := logicalOR "?" ternary ":" ternary
    private Expression ternary() { //DONE
        Expression expr = logicalOR();
        if (match(Type.OPERATOR, "?")) {
            Token op1 = current;
            Expression middle = ternary();
            expect(Type.COLON, "expect ':' in ternary expression");
            Token op2 = current;
            Expression right = ternary();
            expr = new Expression.Ternary(expr, op1, middle, op2, right);
        } return expr;

    }

    // logicalOR := logicalAND ( "||" logicalAND )*
    private Expression logicalOR() { //DONE
        Expression expr = logicalAND();
        while (match(Type.OPERATOR, "||")) {
            Token operator = current;
            Expression right = logicalAND();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    // logicalAND := bitwiseOR ( "&&" bitwiseOR )*
    private Expression logicalAND() { //DONE
        Expression expr = bitwiseOR();
        while (match(Type.OPERATOR, "&&")) {
            Token operator = current;
            Expression right = bitwiseOR();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    // bitwiseOR := bitwiseXOR ( "|" bitwiseXOR )*
    private Expression bitwiseOR() { //DONE
        Expression expr = bitwiseXOR();
        while (match(Type.OPERATOR, "|")) {
            Token operator = current;
            Expression right = bitwiseXOR();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    // bitwiseXOR := bitwiseAND ( "^" bitwiseAND )*
    private Expression bitwiseXOR() { //DONE
        Expression expr = bitwiseAND();
        while (match(Type.OPERATOR, "^")) {
            Token operator = current;
            Expression right = bitwiseAND();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    // bitwiseAND := equality ( "&" equality )*
    private Expression bitwiseAND() { //DONE
        Expression expr = equality();
        while (match(Type.OPERATOR, "&")) {
            Token operator = current;
            Expression right = equality();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    // equality := relational ( ( "!=" | "==" ) relational )*
    private Expression equality() { //DONE
        Expression expr = relational();
        while (match(Type.OPERATOR, "==", "!=")) {
            Token operator = current;
            Expression right = relational();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    // relational := bitshift ( ( ">" | ">=" | "<" | "<=" ) bitshift )*
    private Expression relational() { //DONE
        Expression expr = bitshift();

        while (match(Type.OPERATOR, "<", ">", "<=", ">=")) {
            Token operator = current;
            Expression right = bitshift();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    // bitshift := term ( ( "<<" | ">>" | ">>>" ) term )*
    private Expression bitshift() { //DONE
        Expression expr = term();

        while (match(Type.OPERATOR, "<<", ">>", ">>>")) {
            Token operator = current;
            Expression right = bitshift();
            expr = new Expression.Binary(expr, operator, right);
        }
        return expr;
    }

    // term := factor ( ( "-" | "+" ) factor )*
    private Expression term() { //DONE
        Expression expr = factor();
    
        while (match(Type.OPERATOR, "+", "-")) {
            Token operator = current;
            Expression right = factor(); 
            expr = new Expression.Binary(expr, operator, right);
        }
    
        return expr;
      }

    // factor := unary ( ( "/" | "*" ) unary )*
    private Expression factor() { //DONE
        Expression expr = unary();

        while (match(Type.OPERATOR, "*", "/","%")) {
            Token operator = current;
            Expression right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    // unary := ( "!" | "-" ) unary | postfix
    private Expression unary() { //DONE
        if (!match(Type.OPERATOR, "+","-","!","~")) return postfix();

        Token operator = current;
        Expression right = unary();
        return new Expression.Unary(operator, right);
    
    }

    // postfix := call ( "++" | "--" )*
    private Expression postfix() { //DONE
        Expression expr = call();
        while (match(Type.OPERATOR, "++", "--")) {
            Token op = current;
            expr = new Expression.Postfix(expr, op);
        }
        return expr;
    }

    // call := primary "(" args? ")"
    // args := expr ( "," expr )*
    private Expression call() {
        Expression expr = primary();
        if (match(Type.LPAREN)) {
            LinkedList<Expression> args = new LinkedList<>();

            if (lookahead.type() != Type.RPAREN) {
                do {
                    if (args.size() > 63) {
                        error(lookahead, "can't have more than 127 arguments");
                    }
                    args.add(expression());
                } while (match(Type.COMMA));
            }

            expect(Type.RPAREN, "expect ')' after arguments");
            Token paren = current;
            return new Expression.Call(((Expression.Variable) expr).name, paren, args);
        } 
        return expr;
    }
    
    // primary := LITERAL | variable | "true" | "false" | "null" | "(" expr ")"
    private Expression primary() {
        switch (lookahead.type()){
            case NULL: { advance(); return new Expression.Literal(null); }
            case TRUE: { advance(); return new Expression.Literal(true); }
            case FALSE: { advance(); return new Expression.Literal(false); }
            case IDENTIFIER: {advance(); return variable(); }
            case LPAREN: {
                advance();
                // type casting
                if (lookahead.type() == Type.IDENTIFIER) {
                    switch ((String) lookahead.value()) {
                        case "bln":
                        case "int":
                        case "flt":
                        case "str": {
                            Token type = lookahead;
                            advance();
                            expect(Type.RPAREN, "expect ')' after type cast");
                            Expression expr = unary();
                            return new Expression.TypeCast(type, expr);
                        }
                    }
                }
                Expression expr = expression();
                expect(Type.RPAREN, "expect ')' after expr");
                return new Expression.Grouping(expr); 
            }
            case LNG_LITERAL: {
                advance();
                return new Expression.Literal((int) current.value());
            }
            case DBL_LITERAL: {
                advance();
                return new Expression.Literal((double) current.value());
            }
            case STR_LITERAL: {
                advance();
                return new Expression.Literal((String) current.value());
            }
            default: throw error(lookahead, "expect expr");
        }
    }

    // variable := IDENTIFIER ( "[" expression? ":"? expression? "]" )*
    private Expression variable() {
        Token name = current;
        Expression expr = new Expression.Variable(current);
        while (match(Type.LBRACKET)) {
            Token lbrack = current;
            Expression start = null, end = null;
            Token colon = null;
            if (lookahead.type() != Type.COLON) start = expression();
            if (lookahead.type() != Type.RBRACKET) {
                expect(Type.COLON, "expect ':' in sliced index");
                colon = current;
            }
            if (lookahead.type() != Type.RBRACKET) end = expression();
            expect(Type.RBRACKET, "expect ']' after index expression");
            expr = new Expression.Index(name, expr, lbrack, start, colon, end);
        } return expr;
    }
    
    // Re-rails the Parser after detecting an error; continues with normal parsing at the next line
    private void rerail() {
        while (!endOfFile()) {
            advance();
            if (lookahead.line() != current.line()) return;
        }
    }
}
