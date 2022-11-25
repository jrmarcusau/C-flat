import java.util.List;

/**
 * Framework abstract class to encapsulate all <code>Expression</code> syntax nodes
 */
public abstract class Expression {
    /** 
     * Implemented by classes that traverse the syntax tree
    */
    public interface Visitor<R> {
        /**
         * does something to a Literal expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitLiteralExpression(Literal expression);
        /**
         * does something to a Variable expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitVariableExpression(Variable expression);
        /**
         * does something to a Grouping expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitGroupingExpression(Grouping expression);
        /**
         * does something to a Unary expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitUnaryExpression(Unary expression);
        /**
         * does something to a Type Cast expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitTypeCastExpression(TypeCast expression);
        /**
         * does something to a Postfix expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitPostfixExpression(Postfix expression);
        /**
         * does something to a Binary expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitBinaryExpression(Binary expression);
        /**
         * does something to a Ternary expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitTernaryExpression(Ternary expression);
        /**
         * does something to an Index expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitIndexExpression(Index expression);
        /**
         * does something to an Assign at Index expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitAssignAtExpression(AssignAt expression);
        /**
         * does something to a Call expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitCallExpression(Call expression);
        /**
         * does something to an assignment expression
         * @param expression the expression
         * @return any generic type (Object for Interpreter)
         */
        R visitAssignmentExpression(Assignment expression);
    }
    /**
     * Syntax node that wraps a litera's value
     */
    public static class Literal extends Expression {
        public final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }
    /**
     * Syntax node that represents a variable
     */
    public static class Variable extends Expression {
        public final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }
    /**
     * Syntax node that wraps another <code>Expression</code> in parentheses
     */
    public static class Grouping extends Expression {
        public final Expression expr;

        Grouping(Expression expr) {
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }
    /**
     * Syntax node tht represents a unary operation
     */
    public static class Unary extends Expression {
        public final Token op;
        public final Expression expr;

        Unary(Token op, Expression expr) {
            this.op = op;
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }
    /** 
     * Syntax node taht represents a type cast
     */
    public static class TypeCast extends Expression {
        public final Token type;
        public final Expression expr;

        TypeCast(Token type, Expression expr) {
            this.type = type;
            this.expr = expr;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTypeCastExpression(this);
        }
    }
    /** 
     * Syntax node that represents a postfix operation
     */
    public static class Postfix extends Expression {
        public final Expression expr;
        public final Token op;

        Postfix(Expression expr, Token op) {
            this.expr = expr;
            this.op = op;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPostfixExpression(this);
        }
    }
    /**
     * Syntax node that represents a binary operation
     */
    public static class Binary extends Expression {
        public final Expression l;
        public final Token op;
        public final Expression r;

        Binary(Expression l, Token op, Expression r) {
            this.l = l;
            this.op = op;
            this.r = r;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }
    /** 
     * Syntax node that represents a ternary operation
     */
    public static class Ternary extends Expression {
        public final Expression l;
        public final Token op1;
        public final Expression m;
        public final Token op2;
        public final Expression r;

        Ternary(Expression l, Token op1, Expression m, Token op2, Expression r) {
            this.l = l;
            this.op1 = op1;
            this.m = m;
            this.op2 = op2;
            this.r = r;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpression(this);
        }
    }
    /**
     * Syntax node that represents an indexing expression
     */
    public static class Index extends Expression {
        public final Token name;
        public final Expression array;
        public final Token brack;
        public final Expression start;
        public final Token colon;
        public final Expression end;

        Index(Token name, Expression array, Token brack, Expression start, Token colon, Expression end) {
            this.name = name;
            this.array = array;
            this.brack = brack;
            this.start = start;
            this.colon = colon;
            this.end = end;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIndexExpression(this);
        }
    }
    /** 
     * Syntax node that represents an assignment expression at a certain index
     */
    public static class AssignAt extends Expression {
        public final Token name;
        public final Expression array;
        public final String operation;
        public final Expression index;
        public final Expression value;

        AssignAt(Token name, Expression array, String operation, Expression index, Expression value) {
            this.name = name;
            this.array = array;
            this.operation = operation;
            this.index = index;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignAtExpression(this);
        }
    }
    /** 
     * Syntax node taht represents a call to a function
     */
    public static class Call extends Expression {
        public final Token callee;
        public final Token paren;
        public final List<Expression> args;

        Call(Token callee, Token paren, List<Expression> args) {
            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }
    }
    /**
     * Syntax node that represents an assignment of a value to a variable
     */
    public static class Assignment extends Expression {
        public final Token name;
        public final Expression value;

        Assignment(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignmentExpression(this);
        }
    }

    /**
     * Directs the visitor to the correct method corresponding to which type of syntax tree node it is evaluating
     * @param <R>
     * @param visitor 
     * @return a generic type
     */
    abstract <R> R accept(Visitor<R> visitor);
}
