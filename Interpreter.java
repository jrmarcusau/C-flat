import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
   The interpreter "visits" each statement and expression, evaluating them within <code>Environment</code>

   @author  William
   @version 1.2
   @author  Sources - Kevin
 */
public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    public final Runtime runtime;
    private final HashMap<Expression, Integer> locals;
    private final Importer importer;
    private final Resolver resolver;
    private Environment environment;

    /**
     * Constructs an instance of an Interpreter using no parameters.
     */
    public Interpreter() {
        runtime  = new Runtime();
        locals = new HashMap<>();
        resolver = new Resolver(this);
        importer = new Importer(this, resolver);
        environment = runtime;
    }

    /**
     * Interprets.
     * 
     * @param statements
     */
    public void interpret(List<Statement> statements) {
        importer.importFunctions(statements);
        resolver.resolveStmts(statements);
        for (Statement stmt : statements) {
            execute(stmt);
        }
    }

    /**
     * Called by <code>Resolver</code> to put variable resolutions into Interpreter's HashMap
     * @param expr
     * @param depth
     */
    public void resolve(Expression expr, int depth) {
        locals.put(expr, depth);
    }

    /**
     * @return the current environment Interpreter is in the scope of
     */
    public Environment getEnvironment() { return environment; }
    
    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    private void execute(Statement stmt) {
        stmt.accept(this);
    }
    
    ////////////////////////// Interpret Specialization //////////////////////////

    @Override
    public Object visitCallExpression(Expression.Call expr) {
        // only non-void functions
        LinkedList<Object> args = new LinkedList<>();
        for (Expression arg : expr.args) {
            args.add(evaluate(arg));
        }
        Callable function = runtime.getFunc(expr.callee, args.size());
        return function.call(this, args);
    }


    @Override
    public Object visitLiteralExpression(Expression.Literal expr) {
        return expr.value;
    }

    @Override
    public Void visitExprStatement(Statement.Expr stmt) {
        if (stmt.expr instanceof Expression.Call) { // both func and void
            Expression.Call call = (Expression.Call) stmt.expr;
            LinkedList<Object> args = new LinkedList<>();
            for (Expression arg : call.args) {
                args.add(evaluate(arg));
            }
            Callable function = runtime.getVoid(call.callee, args.size());
            if (args.size() != function.arity()) {
                throw new CflatRuntimeException(call.paren, 
                    "function " + call.callee.value() + " is not defined with " + args.size() + " arguments");
            }
            function.call(this, args);
        } else evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitVariableDeclarationStatement(Statement.VariableDeclaration statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }
        environment.define(statement.name, value);
        return null;
    }

    @Override
	public Void visitArrayDeclarationStatement(Statement.ArrayDeclaration stmt)
	{
		ArrayList<Object> value = new ArrayList<>();
        for (Expression e : stmt.initializer) {
            value.add(evaluate(e));
        }
        environment.define(stmt.name, value);
		return null;
	}

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    /**
     * Executes a list of block statements
     * 
     * @param statements list of statements
     * @param newEnvironment input environment
     */
    public void executeBlock(List<Statement> statements, Environment newEnvironment) {
        Environment previous = this.environment;
        try {
            this.environment = newEnvironment;
            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value != null) value = evaluate(statement.value);
        throw new Return(value);
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        Object value = 1;
        if (statement.value != null) value = evaluate(statement.value);
        throw new Break(castToInteger(value));
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expr)
    {
        Integer dist = locals.get(expr);
        if (dist != null) {
            return environment.getAt(dist, (String) expr.name.value());
        } else {
            return runtime.getVar(expr.name);
        }
    }

    @Override
    public Object visitIndexExpression(Expression.Index expr)
    {
        Object array = evaluate(expr.array);
        Integer start = expr.start != null ? castToInteger(evaluate(expr.start)) : null;
        Integer end = expr.end != null ? castToInteger(evaluate(expr.end)) : null;
        if (array instanceof String) {
            String str = (String) array;
            // try {
                if (expr.colon == null) { return str.substring(start, start + 1); }
                if (start == null) return str.substring(0, end);
                if (end == null) return str.substring(start, str.length());
                return str.substring(start,end);
            // } catch (StringIndexOutOfBoundsException e) {
            //     Cflat.runtimeError(expr.brack, "index out of range");
            //     return null;
            // }
        } else if (array instanceof List<?>) {
            List<Object> arr = (List<Object>) array;
            try {
                if (expr.colon == null) { return arr.get(start); }
                if (start == null) return arr.subList(0, end);
                if (end == null) return arr.subList(start, arr.size());
                return arr.subList(start, end);
            } catch (ArrayIndexOutOfBoundsException e) {
                Cflat.runtimeError(expr.brack, "index out of range");
                return null;
            }
        } else {
            Cflat.runtimeError(expr.brack, "variable is not indexable");
            return null;
        }
    }

    @Override
    public Object visitAssignmentExpression(Expression.Assignment expr)
    {
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            runtime.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Object visitAssignAtExpression(Expression.AssignAt expr)
    {
        Object var = evaluate(expr.array); // either List, String
        Object value = evaluate(expr.value);
        int index = castToInteger(evaluate(expr.index));
        if (var instanceof List<?>) {
            List<Object> array = (List<Object>) var;
            switch (expr.operation) {
                case "+=": array.add(index, value); break;
                case "=": array.set(index, value); break;
            }
            return array;
        } else if (var instanceof String) {
            if (!(value instanceof String)) {
                Cflat.runtimeError(expr.name, "can only assign substrings to string");
                return null;
            }
            String string = (String) var;
            switch (expr.operation) {
                case "+=": string = string.substring(0, index) + castToString(value) + string.substring(index); break;
                case "=": string = string.substring(0, index) + castToString(value) + string.substring(index + 1); break;
            }
            var = string;
            Integer distance = locals.get(expr);
            if (distance != null) {
                environment.assignAt(distance, expr.name, var);
            } else {
                runtime.assign(expr.name, var);
            }
            return var;
        } else {
            Cflat.runtimeError(expr.name, "variable is not indexable");
            return null;
        }
        
    }

    @Override
    public Void visitIfStatement(Statement.If stmt)
    {
        if (castToBoolean(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While stmt)
    {
        try {
            while (castToBoolean(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (Break e) {
            if (e.decrement() > 0) throw e;
        }
        return null;
    }

    @Override
    public Void visitSwitchStatement(Statement.Switch stmt)
    {
        Object switcher = evaluate(stmt.switcher);
        int i = 0;
        for (; i < stmt.switchees.size(); i++) {
            if (switcher.equals(evaluate(stmt.switchees.get(i)))) {
                break;
            }
        }
        try {
            for (; i < stmt.cases.size(); i++) {
                execute(stmt.cases.get(i));
            }
            execute(stmt.defaultStmt);
        } catch (Break e) {
            if (e.decrement() > 0) throw e;
        }
        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expr)
    {
        return evaluate(expr.expr);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression)
    {
        Object value = evaluate(expression.expr);
        switch((String) expression.op.value()) {
            case "-": {
                if (value instanceof Integer) return - (int) value;
                else if (value instanceof Double) return - (double) value;
            }
            case "!": return !castToBoolean(value);
            case "~": return ~castToInteger(value);
        }

        return null;
    }

    @Override
    public Object visitPostfixExpression(Expression.Postfix expr)
    {
        if (expr.expr instanceof Expression.Variable) {
            Expression.Variable var = (Expression.Variable) expr.expr;
            int offset = 0;
            switch ((String) expr.op.value()) {
                case "++": offset = 1; break;
                case "--": offset = -1; break;
            }
            Integer distance = locals.get(var);
            Integer value;
            if (distance != null) {
                value = (Integer) environment.getAt(distance, (String) var.name.value());
                environment.assignAt(distance, var.name, value + offset);
            } else {
                value = (Integer) runtime.getVar(var.name);
                runtime.assign(var.name, value + offset);
            } return value;
        } else throw new CflatRuntimeException(expr.op, "invalid postfix target");
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expr)
    {
        
        String operation = (String) expr.op.value();
        switch (operation) {
            case "==": return evaluate(expr.l).equals(evaluate(expr.r));
            case "!=": return !evaluate(expr.l).equals(evaluate(expr.r));
            case "||": {
                if (!castToBoolean(evaluate(expr.l))) return castToBoolean(evaluate(expr.r));
                return true;
            }
            case "&&": {
                if (castToBoolean(evaluate(expr.l))) return castToBoolean(evaluate(expr.r));
                return false;
            }
        }
        Object l = evaluate(expr.l);
        Object r = evaluate(expr.r);
        if (l instanceof List<?> && operation.equals("+")) {
                ((List<Object>) l).add(r);
                return l;
        }
        if (l instanceof String || r instanceof String) {
            if (operation.equals("+")) return castToString(l) + castToString(r);
        }
        if (l instanceof Double || r instanceof Double) {
            switch (operation) {
                case "+": return castToDouble(l) + castToDouble(r);
                case "-": return castToDouble(l) - castToDouble(r);
                case "*": return castToDouble(l) * castToDouble(r);
                case "/": return castToDouble(l) / castToDouble(r);
                case "<": return castToDouble(l) < castToDouble(r);
                case ">": return castToDouble(l) > castToDouble(r);
                case "<=": return castToDouble(l) <= castToDouble(r);
                case ">=": return castToDouble(l) >= castToDouble(r);
            }
        }
        // Fall through to integral operations
        switch (operation) {
            case "+": return castToInteger(l) + castToInteger(r);
            case "-": return castToInteger(l) - castToInteger(r);
            case "*": return castToInteger(l) * castToInteger(r);
            case "/": return castToInteger(l) / castToInteger(r);
            case "%": return castToInteger(l) % castToInteger(r);
            case "|": return castToInteger(l) | castToInteger(r);
            case "^": return castToInteger(l) ^ castToInteger(r);
            case "&": return castToInteger(l) & castToInteger(r);
            case "<<": return castToInteger(l) << castToInteger(r);
            case ">>": return castToInteger(l) >> castToInteger(r);
            case ">>>": return castToInteger(l) >>> castToInteger(r);
            case "<": return castToInteger(l) < castToInteger(r);
            case ">": return castToInteger(l) > castToInteger(r);
            case "<=": return castToInteger(l) <= castToInteger(r);
            case ">=": return castToInteger(l) >= castToInteger(r);
        }
        return null;
    }

    @Override  
    public Object visitTernaryExpression(Expression.Ternary expr)
    {
        if (castToBoolean(evaluate(expr.l))) return evaluate(expr.m);
        else return evaluate(expr.r);
    }

    // All default type casts
    private Boolean castToBoolean(Object value) {
        if (value instanceof Boolean) { return (boolean) value; }
        if (value instanceof Integer) { return (int) value % 2 == 1; }
        if (value instanceof Double) { return Double.compare((double) value, 0.0) > 0; }
        if (value instanceof String) { return ((String) value).length() != 0; }
        System.err.println("Nulling " + value.getClass() + " to bln");
        return null;
    }
    private Integer castToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) { return (int) ((boolean) value ? 1 : 0); }
        if (value instanceof Integer) { return (int) value; }
        if (value instanceof Double) { return (int) (double) value; }
        if (value instanceof String) { return Integer.parseInt(value.toString()); }
        System.err.println("Nulling " + value.getClass() + " to int");
        return null;
    }

    private Double castToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) { return (boolean) value ? 1.0 : 0.0; }
        if (value instanceof Integer) { return (double) (int) value; }
        if (value instanceof Double) { return (double) value; }
        if (value instanceof String) { return Double.parseDouble(value.toString()); }
        System.err.println("Nulling " + value.getClass() + " to flt");
        return null;
    }

    private String castToString(Object value) {
        if (value == null) {
            System.err.println("null");
            return null;
        }
        if (value instanceof String) { return (String) value; }
        return value.toString();
    }

    @Override
    public Object visitTypeCastExpression(Expression.TypeCast expr)
    {
        switch((String) expr.type.value()) {
            case "bln": return castToBoolean(evaluate(expr.expr));
            case "int": return castToInteger(evaluate(expr.expr));
            case "dbl": return castToDouble(evaluate(expr.expr));
            case "str": return castToString(evaluate(expr.expr));
        }
        return null;
    }
    
    @Override @Deprecated // Handled by Importer
    public Void visitFunctionDeclarationStatement(Statement.FunctionDeclaration stmt) { return null; }

    @Override @Deprecated // Handled by Importer
    public Void visitImportDeclarationStatement(Statement.ImportDeclaration statement) { return null; }

}
