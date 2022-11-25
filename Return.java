/** An "error" that is thrown to simulate an unwinding of the call stack, taking the interpreter out of the current function while optionally holding a value. */
public class Return extends RuntimeException {
    public final Object value;

    /**
     * Constructor with one parameter.
     * @param value
     */
    public Return(Object value) {
        super(null, null, false, false); //disables stack trace
        this.value = value;
    }
}
