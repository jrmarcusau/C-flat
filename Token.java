/**
 *  A token. Contains this token's type and value. '
 */
public class Token {
    
    private final Type type;
    private final Object value;
    private final int line;
    private final String fileName;

    Token(Type type, Object value, int line, String fileName) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.fileName = fileName;
    }

    /**
     * Returns the type of this token
     * 
     * @return this token's type
     */
    public Type type() {
        return type;
    }

    /** 
     * Returns the value of this token
     * 
     * @return this token's value
     */
    public Object value() {
        return value;
    }

    /**
     * Returns the line at which this token was found
     * 
     * @return the line
     */
    public int line() {
        return line;
    }

    /**
     * Returns the file name at which this token was found
     * 
     * @return the file name
     */
    public String file() {
        return fileName;
    }

    @Override
    public String toString() {
        return type + " " + value;
    }
}
