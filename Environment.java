import java.util.HashMap;

/**
 * An Environment represents a single scope in the code (preceded and succeeded by braces)
 * and holds a <code>HashMap</code> of all variables defined within the scope. When the scope is exited,
 * the Environment and all its variables are garbage-collected by Java.
 */
public class Environment {
    private final Environment enclosing;
    /** Local variable storage */
    public final HashMap<String, Object> variables = new HashMap<>();
    

    /**
     * Constructor.
     * 
     * @param enclosing the enclosing <code>Environment</code>
     */
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Puts a local variable in the HashMap
     * 
     * @param nameToken the token for the name of the variable
     * @param value the value of the variable
     */
    public void define(Token nameToken, Object value) {
        String name = (String) nameToken.value();
        if (variables.containsKey(name)) throw new CflatRuntimeException(nameToken, "Cannot re-declare variable '" + name + "' in the same scope");
        variables.put(name, value);
    }

    /**
     * Assigns a value to a local variable in the HashMap
     * 
     * @param name the token for the name of the variable
     * @param value the new value for the variable
     */
    public void assign(Token name, Object value) {
        if (variables.containsKey((String) name.value())) variables.put((String) name.value(), value); 
        else throw new CflatRuntimeException(name, "Unknown variable \"" + (String) name.value() + "\"");
    }

    /**
     * Retrieves a variable from the HashMap
     * 
     * @param name the token for the name of the variable
     * @return the value of the variable
     */
    @Deprecated
    public Object getVar(Token name) {
        if (variables.containsKey((String) name.value())) return variables.get((String) name.value());
        else throw new CflatRuntimeException(name, "Unknown variable \"" + (String) name.value() + "\"");
    }
    
    /**
     * Retrieves a variable from the the current or an enclosing environment given the number of "hops" down the Environment stack
     * 
     * @param dist the number of "hops"
     * @param name the name of the variable as a String
     * @return the value of the variable
     */
    public Object getAt(int dist, String name) {
        return ancestor(dist).variables.get(name);
    }

    /**
     * Assigns a value to a variable from the current or an enclosing environment given the number of "hops" down the Environment stack
     * 
     * @param dist the number of "hops"
     * @param name the name of the variable to be referenced
     * @param value the new value
     */
    public void assignAt(int dist, Token name, Object value) {
        ancestor(dist).variables.put((String) name.value(), value);
    }

    private Environment ancestor(int dist) {
        Environment environment = this;
        for (int i = 0; i < dist; i++) {
            environment = environment.enclosing;
        } return environment;
    }
}
