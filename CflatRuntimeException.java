/** 
 * Cflat runtime exception 
 */
public class CflatRuntimeException extends RuntimeException {
    /** The token at which a runtime error is thrown */
    public final Token token;
    
    /**
     * Constructor.
     * 
     * @param token the token at which it failed
     * @param message the message to the user
     */
    CflatRuntimeException(Token token, String message) {
        super(message);
        this.token = token;
    }
}
