import java.util.List;

/** An interface that defines all expressions that can be called */
public interface Callable {
    /**
     * The number of parameters
     * 
     * @return the arity of the function
     */
    int arity();

    /**
     * Executes the function with given arguments
     * @param interpreter the master interpreter
     * @param args the arguments to be fed into the function when executed
     * @return an object that represents the return value of the function. Null if no return.
     */
    Object call(Interpreter interpreter, List<Object> args);
}
