import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/** Puts all functions, including those imported from other files, into <code>Runtime</code> for <code>Interpreter</code> to call */
public class Importer {
    private final Interpreter interpreter;
    private final Resolver resolver;

    /**
     * Constructs an instance of Importer with references to the master interpreter and a resolver
     * 
     * @param interpreter
     * @param resolver
     */
    public Importer(Interpreter interpreter, Resolver resolver) {
        this.interpreter = interpreter;
        this.resolver = resolver;
    }

    /**
     * Imports functions.
     * 
     * @param stmts to iterate over and load functions into the Interpreter from
     */
    public void importFunctions(List<Statement> stmts) {
        Iterator<Statement> iter = stmts.iterator();
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            if (stmt instanceof Statement.FunctionDeclaration) {
                Statement.FunctionDeclaration functionDeclaration = (Statement.FunctionDeclaration) stmt;
                Function function = new Function(functionDeclaration);
                if (functionDeclaration.returns) {
                    interpreter.runtime.defineFunc(functionDeclaration.name, functionDeclaration.params.size(), function);
                } else {
                    interpreter.runtime.defineVoid(functionDeclaration.name, functionDeclaration.params.size(), function);
                }
                resolver.resolve(stmt);
                iter.remove();
            }
            else if (stmt instanceof Statement.ImportDeclaration) {
                for (Token im : ((Statement.ImportDeclaration) stmt).imports) {
                    try {
                        importFunctions(Cflat.parseFile("./cflatexe/" + im.value() + ".cflat"));
                    } catch (IOException e) {
                        Cflat.parsetimeError(im, "File '" + im.value() + ".cflat' not found");
                    }
                }
                iter.remove();
            }
        }
    }

    
}
