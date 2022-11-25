import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Main class
 */
public class Cflat {
    public static boolean hasError = false;

    /**
     * Takes a file directory pointing to a .cflat file and parses it into a <code>List</code> of <code>Statement</code>s
     * 
     * @param path file directory
     * @return list of abstract syntax trees
     * @throws IOException
     */
    public static List<Statement> parseFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String text = new String(bytes, Charset.defaultCharset());
        String fileName = fileName(path);
        Parser parser = new Parser(new Tokenizer(text, fileName));
        return parser.parse();
    }

    /**
     * Prints a parsetime error with line number and error message.
     * Called by <code>Parser</code>, <code>Importer</code>, and <code>Resolver</code>.
     * 
     * @param token a placeholder token to extract the line number from
     * @param message the error message
     */
    public static void parsetimeError(Token token, String message) {
        int line = token.line();
        String file = token.file();
        System.err.println(
            "[" + file + " ln " + line + "] Syntax: " + message
        );
    }

    /**
     * Prints a runtime error with line number and error message. Called by <code>Interpreter</code>.
     * 
     * @param token a placeholder token to extract the line number from
     * @param message the error message
     */
    public static void runtimeError(Token token, String message) {
        int line = token.line();
        String file = token.file();
        System.err.println(
            "[" + file + " ln " + line + "] Runtime: " + message
        );
        System.exit(1);
    }

    private static String fileName(String path) {
        int lastSlash = -1;
        while (path.indexOf("/") != -1) {
            lastSlash = path.indexOf("/");
            path = path.substring(lastSlash + 1);
        }
        return path;
    }

    /**
     * Main.
     * 
     * @param args the .cflat file to be executed
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: Cflat [path]");
            System.exit(64);
        } else {
            List<Statement> main;
            if (args.length == 0) main = parseFile("./cflatexe/main.cflat");
            else main = parseFile(args[0]);
            if (hasError) return;
            // ASTPrinter printer = new ASTPrinter();
            // printer.print(main);
            System.out.println();
            System.out.println();
            System.out.println();
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(main);
            // try {
            //     interpreter.interpret(main);
            // } catch (StackOverflowError e) { throw new StackOverflowError("no further information"); }
        }
    }
}
