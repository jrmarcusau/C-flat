import java.util.List;

/**
 * Framework abstract class to encapsulate all <code>Statement</code> syntax nodes
 */
public abstract class Statement {

    /**
     * Implemented by classes that traverse the syntax tree
     */
    public interface Visitor<R> {
        /**
         * executes a block statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitBlockStatement(Block statement);
        /**
         * executes an expression statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitExprStatement(Expr statement);
        /**
         * executes a import statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitImportDeclarationStatement(ImportDeclaration statement);
        /**
         * executes a function statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitFunctionDeclarationStatement(FunctionDeclaration statement);
        /**
         * executes a variable statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitVariableDeclarationStatement(VariableDeclaration statement);
        /**
         * executes an array declaration statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitArrayDeclarationStatement(ArrayDeclaration statement);
        /**
         * executes an if statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitIfStatement(If statement);
        /**
         * executes a while statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitWhileStatement(While statement);
        /**
         * executes a switch statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitSwitchStatement(Switch statement);
        /**
         * executes a return statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitReturnStatement(Return statement);
        /**
         * executes a break statement
         * @param statement the statement currently being visited
         * @return any type
         */
        R visitBreakStatement(Break statement);
    }
    /**
     * Syntax node to represent a <code>Block</code>
     */
    public static class Block extends Statement {
        public final List<Statement> statements;

        Block(List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }
    /**
     * Syntax node to wrap an <code>Expression</code> as a Statement
     */
    public static class Expr extends Statement {
        public final Expression expr;

        Expr(Expression expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }
    }
    /**
     * Syntax node to represent an import
     */
    public static class ImportDeclaration extends Statement {
        public final List<Token> imports;

        ImportDeclaration(List<Token> imports) {
            this.imports = imports;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitImportDeclarationStatement(this);
        }
    }
    /**
     * Syntax node to represent a function
     */
    public static class FunctionDeclaration extends Statement {
        public final Token name;
        public final List<Token> params;
        public final List<Statement> body;
        public final boolean returns;

        FunctionDeclaration(Token name, List<Token> params, List<Statement> body, boolean returns) {
            this.name = name;
            this.params = params;
            this.body = body;
            this.returns = returns;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionDeclarationStatement(this);
        }
    }
    /**
     * Syntaax node to represent a variable
     */
    public static class VariableDeclaration extends Statement {
        public final Token name;
        public final Expression initializer;

        VariableDeclaration(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableDeclarationStatement(this);
        }
    }
    /**
     * Syntax node to represent an array
     */
    public static class ArrayDeclaration extends Statement {
        public final Token name;
        public final List<Expression> initializer;

        ArrayDeclaration(Token name, List<Expression> initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayDeclarationStatement(this);
        }
    }
    /**
     * Syntax node to represent an if statement
     */
    public static class If extends Statement {
        public final Expression condition;
        public final Statement thenBranch;
        public final Statement elseBranch;

        If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }
    /**
     * Syntax node to represent a while loop
     */
    public static class While extends Statement {
        public final Expression condition;
        public final Statement body;

        While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }
    /**
     * Syntax node to represent a switch statement
     */
    public static class Switch extends Statement {
        public final Expression switcher;
        public final List<Expression> switchees;
        public final List<Statement> cases;
        public final Statement defaultStmt;

        Switch(Expression switcher, List<Expression> switchees, List<Statement> cases, Statement defaultStmt) {
            this.switcher = switcher;
            this.switchees = switchees;
            this.cases = cases;
            this.defaultStmt = defaultStmt;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStatement(this);
        }
    }
    /**
     * Syntax node to represent a return statement
     */
    public static class Return extends Statement {
        public final Token keyword;
        public final Expression value;

        Return(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }
    /**
     * Syntax node to represent a break statement
     */
    public static class Break extends Statement {
        public final Token keyword;
        public final Expression value;

        Break(Token keyword, Expression value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStatement(this);
        }
    }

    /**
     * Directs the Visitor to the correct method corresponding to which type of syntax tree node it is evaluating
     * @param <R>
     * @param visitor
     * @return a generic type
     */
    abstract <R> R accept(Visitor<R> visitor);
}
