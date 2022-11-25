import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Runtime is a special <code>Environment</code> that holds all runtime functions and global variables.
 */
public class Runtime extends Environment {
    public final HashMap<String, Callable> functions;
    public final HashMap<String, Callable> voids;
    private final Scanner scanner;
    
    public Runtime() {
        super(null);
        functions = new HashMap<>();
        voids = new HashMap<>();
        scanner = new Scanner(System.in);
        loadNativeFunctions();
    }

    /**
     * Gets a function from the HashMap
     * @param name the name of the function
     * @param arity the number of arguments
     * @return a reference to the executable function
     */
    public Callable getFunc(Token name, int arity) {
        String handle = (String) name.value() + arity;
        if (functions.containsKey(handle)) return functions.get(handle);
        if (voids.containsKey(handle)) {
            throw new CflatRuntimeException(name, "function " + name.value() + " does not return a value");
        } else {
            String message = "unknown function '" + name.value() + "' with " + arity + " arguments";
            // Suggestion for calling with a different number of arguments if exists
            for (int i = arity + 2; i >= 0; i--) { 
                handle = (String) name.value() + i;
                if (functions.containsKey(handle) || voids.containsKey(handle)) {
                    message = message + ". Did you mean '" + name.value() + "' with " + i + " parameters?";
                    break;
                }
            }
            throw new CflatRuntimeException(name, message);
        }
    }

    /**
     * Gets a void function from the HashMap
     * 
     * @param name the name of the called function
     * @param arity the number of arguments found
     * @return a reference to an executable void function
     */
    public Callable getVoid(Token name, int arity) {
        String handle = (String) name.value() + arity;
        if (voids.containsKey(handle)) return voids.get(handle);
        if (functions.containsKey(handle)) return functions.get(handle);
        throw new CflatRuntimeException(name, "unknown function '" + name.value() + "'");
    }

    /**
     * Puts a function in the HashMap
     * 
     * @param name the name of the function
     * @param arity the number of arguments it accepts
     * @param function a reference to the function
     */
    public void defineFunc(Token name, int arity, Function function) {
        String handle = (String) name.value() + arity;
        if (functions.containsKey(handle) || voids.containsKey(handle)) 
            throw new CflatRuntimeException(name, "cannot re-declare function '" + name.value() + "'");
        functions.put(handle, function);
    }

    /**
     * Puts a void function in the void HashMap
     * 
     * @param name the name of the void function
     * @param arity the number of arguments it accepts
     * @param function a reference to the void function
     */
    public void defineVoid(Token name, int arity, Function function) {
        String handle = (String) name.value() + arity;
        if (functions.containsKey(handle) || voids.containsKey(handle))
            throw new CflatRuntimeException(name, "cannot re-declare void function '" + name.value() + "'");
        voids.put(handle, function);
    }

    private void defineNative(String name, boolean returns, Callable function) {
        String handle = name + function.arity();
        (returns ? functions : voids).put(handle, function);
    }

    private void loadNativeFunctions() {
        // void print(string) prints a string to the console
        defineNative("print", false, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                System.out.print(args.get(0));
                return null;
            }
        });
        // void println() prints a new line to the console
        defineNative("println", false, new Callable() {
            public int arity() { return 0; }
            public Object call(Interpreter itpr, List<Object> args) {
                System.out.println();
                return null;
            }
        });
        // void println(string) prints a new line to the console
        defineNative("println", false, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                System.out.println(args.get(0));
                return null;
            }
        });
        // func length(string or list) returns the length of the array or string as an int
        defineNative("length", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                if (args.get(0) instanceof List<?>) {
                    return ((List<Object>) args.get(0)).size();
                } else if (args.get(0) instanceof String) {
                    return ((String) args.get(0)).length();
                }
                return -1;
            }
        });
        // func yeet(string or array, n) yeets the nth element of string or array
        defineNative("yeet", true, new Callable() {
            public int arity() { return 2; }
            public Object call(Interpreter itpr, List<Object> args) {
                if (args.get(0) instanceof List<?>) {
                    return ((List<Object>) args.get(0)).remove((int) args.get(1));
                } else if (args.get(0) instanceof String) {
                    String str = (String) args.get(0);
                    return str.substring(0, (int) args.get(1)) + str.substring((int) args.get(1) + 1);
                }
                return -1;
            }
        });
        // func isAlphabetic(string) returns whether the first character of the string is alphabetic
        defineNative("isAlphabetic", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                return Character.isAlphabetic(((String) args.get(0)).charAt(0));
            }
        });
        // func isUpperCase(string) returns whether the first character of the string is upper case
        defineNative("isUpperCase", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                return Character.isUpperCase(((String) args.get(0)).charAt(0));
            }
        });
        // func isLowerCase(string) returns whether the first character of the string is lower case
        defineNative("isLowerCase", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                return Character.isLowerCase(((String) args.get(0)).charAt(0));
            }
        });
        // func toUpperCase(string) returns an all-uppercase string
        defineNative("toUpperCase", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                return ((String) args.get(0)).toUpperCase();
            }
        });
        // func toLowerCase(string) returns an all-lowercase string
        defineNative("toLowerCase", true, new Callable() {
            public int arity() { return 1; }
            public Object call(Interpreter itpr, List<Object> args) {
                return ((String) args.get(0)).toLowerCase();
            }
        });
        // func input() returns a line from the console as a str
        defineNative("input", true, new Callable() {
            public int arity() { return 0; }
            public Object call(Interpreter itpr, List<Object> args) {
                return scanner.nextLine();
            }
        });
        // func rand() returns Math.random();
        defineNative("rand", true, new Callable() {
            public int arity() { return 0; }
            public Object call(Interpreter itpr, List<Object> args) {
                return Math.random();
            }
        });
        // func clock() gets the number of milliseconds of runtime
        defineNative("clock", true, new Callable() {
            public int arity() { return 0; }
            public Object call(Interpreter itpr, List<Object> args) {
                return (int) System.currentTimeMillis();
            }
        });
    }
}
