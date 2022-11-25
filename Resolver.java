import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Resolves variable references to their declarations. Puts resolutions into the interpreter's <code>locals</code> HashMap for lookup.
 */
public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<HashMap<String,Boolean>> scopes = new Stack<>();
    
    /**
     * Constructor with one parameter.
     * 
     * @param interpreter the master <code>Interpreter</code> this <code>Resolver</code> is the sidekick to
     */
    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void resolveLocal(Expression expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey((String) name.value())) {
                interpreter.resolve(expr, scopes.size() - i - 1);
                return;
            }
        }
    }

    private void resolveFunction(Statement.FunctionDeclaration meth) {
        beginScope();
        for (Token param : meth.params) {
            declare(param);
            define(param);
        }
        resolveStmts(meth.body);
        endScope();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put((String) name.value(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put((String) name.value(), true);
    }

    /**
     * Called by <code>Interpreter</code> to resolve the main executable and <code>Importer</code> to resolve function parameters.
     * @param stmts a list of statements to resolve
     */
    public void resolveStmts(List<Statement> stmts) {
        for (Statement stmt : stmts) {
            resolve(stmt);
        }
    }

    /**
     * Called by <code>Interpreter</code> to resolve the main executable and <code>Importer</code> to resolve function parameters.
     * @param stmts statements to resolve
     */
    public void resolve(Statement... stmts) {
        for (Statement stmt : stmts) {
            stmt.accept(this);
        }
    }

    private void resolveExprs(List<Expression> exprs) {
            for (Expression expr : exprs ) {
                resolve(expr);
            }
        }

    private void resolve(Expression... exprs) {
        for (Expression expr : exprs) {
            expr.accept(this);
        }
    }

    ////////////////////////// Resolve Specialization //////////////////////////

    /* 
     * Statement.Block, Statement.VariableDeclaration, 
     * Expression.Assignment, Expression.Variable, Statement.Method
     */

    @Override
    public Void visitBlockStatement(Statement.Block stmt) {
        beginScope();
        resolveStmts(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitArrayDeclarationStatement(Statement.ArrayDeclaration stmt)
    {
        declare(stmt.name);
        if (stmt.initializer.size() != 0) {
            for (Expression e : stmt.initializer) {
                resolve(e);
            }
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(Expression.Assignment expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignAtExpression(Expression.AssignAt expr)
    {
        resolve(expr.index, expr.value, expr.array);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expr) {
        if (!scopes.isEmpty() && 
             scopes.peek().get((String) expr.name.value()) == Boolean.FALSE) {
                Cflat.parsetimeError(expr.name, "can't read local variable in its own initializer");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitIndexExpression(Expression.Index expr)
    {
        if (expr.start != null) resolve(expr.start);
        if (expr.end != null) resolve(expr.end);
        resolve(expr.array);
        return null;
    }

    @Override
    public Void visitFunctionDeclarationStatement(Statement.FunctionDeclaration stmt) {
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt);
        return null;
    }

    ////////////////////////// Tree-Walk //////////////////////////

    @Override
    public Void visitReturnStatement(Statement.Return stmt) {
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break stmt) {
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expr) {
        
        for (Expression arg : expr.args) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitTernaryExpression(Expression.Ternary expr) {
        resolve(expr.l, expr.m, expr.r);
        return null;
    }
    @Override
    public Void visitBinaryExpression(Expression.Binary expr) {
        resolve(expr.l, expr.r);
        return null;
    }

    @Override
    public Void visitTypeCastExpression(Expression.TypeCast expr)
    {
        resolve(expr.expr);
        return null;
    }
    
    @Override
    public Void visitUnaryExpression(Expression.Unary expr) {
        resolve(expr.expr);
        return null;
    }

    @Override
    public Void visitPostfixExpression(Expression.Postfix expr) {
        resolve(expr.expr);
        return null;
    }
    
    @Override
    public Void visitGroupingExpression(Expression.Grouping expr) {
        resolve(expr.expr);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expr) { return null; }

    @Override
    public Void visitExprStatement(Statement.Expr stmt)
    {
        resolve(stmt.expr);
        return null;
    }

    @Override
    public Void visitSwitchStatement(Statement.Switch stmt)
    {
        resolve(stmt.switcher);
        resolveExprs(stmt.switchees);
        resolveStmts(stmt.cases);
        if (stmt.defaultStmt != null) resolve(stmt.defaultStmt);
        return null;
    }

    @Override @Deprecated // Handled by Importer
    public Void visitImportDeclarationStatement(Statement.ImportDeclaration stmt)
    {
        return null;
    }

    

    

    

}
