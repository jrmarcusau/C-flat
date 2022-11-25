import java.util.List;

/** Holds a user-defined or library function as an instance of this class */
public class Function implements Callable {
    private final Statement.FunctionDeclaration declaration;
    
    /**
     * Constructs a <code>Function</code> instance from a Function Declaration Statement
     * @param declaration the function declaration syntax tree node
     */
    public Function(Statement.FunctionDeclaration declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(interpreter.getEnvironment());
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i), args.get(i));
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnObject) {
            return returnObject.value;
        }

        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
}
