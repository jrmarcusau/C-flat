import java.util.ArrayList;
import java.util.Arrays;

/**
 * Tokenizer takes in a raw string of text. It processes this text and spits out an 
 * array of tokens. This array of tokens is then shipped off to the Parser to be put
 * into an AST.
 * 
 * @author Marcus Au
 */
public class Tokenizer {

    private String text;
    private String fileName;

    private ArrayList<Token> tokens;
    private int tokCursor;
    private int curLine;

    /**
     * Constructs a tokenizer instance with the code as a string and a file name
     * @param text
     * @param fileName
     */
    public Tokenizer(String text, String fileName) {
        this.text = text;
        this.fileName = fileName;
        this.curLine = 1;
        tokens = tokenize();
    }

    private ArrayList<Token> tokenize() {
        ArrayList<Token> myTokens = new ArrayList<Token>();
        int cursor = 0;
        int tStart, tEnd;
        while(cursor < text.length() ) {
            Character curChar = text.charAt(cursor);
            Token tToken = null;
            String sToken = null; 
            
            //whitespace
            while (curChar == ' ') {
                cursor++;
                if (cursor == text.length()) break;
                curChar = text.charAt(cursor);
            }
            if (cursor == text.length()) break;

            //Strings
            if (curChar == '"') {
                cursor++;
                tStart = cursor;
                curChar = text.charAt(cursor);
                while(curChar != '"') {
                    cursor++;
                    curChar = text.charAt(cursor);
                }
                tEnd = cursor;
                cursor++;
                sToken = text.substring(tStart, tEnd);
                tToken = toToken_String(sToken); 
            }

            //newline
            else if (curChar == '\n') {
                //tToken = toToken_Newline(curChar, Type.LITERAL);
                cursor++;
                curLine++;
            }

            //char
            else if (curChar == '\'') {
                sToken = text.substring(cursor, cursor + 3);
                tToken = toToken_Char(sToken);
                cursor++; cursor++; cursor++;
            }

            //number
            else if (Character.isDigit(curChar)) {
                tStart = cursor;
                while(curChar == '.' || (!Character.isWhitespace(curChar) && Character.isLetterOrDigit(curChar))) {//can also hit special char
                    cursor++;
                    if (cursor == text.length()) break;
                    curChar = text.charAt(cursor);
                }
                tEnd = cursor;
                sToken = text.substring(tStart, tEnd);
                tToken = toToken_Number(sToken);                
            } 
            
            //words
            else if (Character.isLetter(curChar)) {
                tStart = cursor;
                while(!Character.isWhitespace(curChar) && Character.isLetterOrDigit(curChar)  ) {
                    cursor++;
                    if (cursor == text.length()) break;
                    curChar = text.charAt(cursor);
                }
                tEnd = cursor;
                sToken = text.substring(tStart, tEnd);
                Type type = getType_Word( sToken);
                tToken = toToken_Word(sToken, type);
            } 
            
            //operators
            else if (isOperator(curChar)) {
                tStart = cursor;
                while(isOperator(curChar)) {
                    cursor++;
                    if (cursor == text.length()) break;
                    curChar = text.charAt(cursor);
                }
                tEnd = cursor;
                sToken = text.substring(tStart, tEnd);
                tToken = toToken_Operator(sToken);
            }

            //special char
            else {
                sToken = curChar.toString();
                Type type = getType_Special(sToken);
                tToken = toToken_Special(sToken, type);
                cursor++;
            }

            if (tToken != null && tToken.type() != Type.UNKNOWN) {
                myTokens.add(tToken);
            }
        }
        myTokens.add(new Token(Type.EOF, null, curLine, fileName));

        return myTokens;
    }

    private boolean isOperator (Character c) {
        ArrayList<Character> operators = new ArrayList<Character>(Arrays.asList(
            '=', '+', '-', '*', '/', '%', '&', '|', '^', '<', '>',
            '?', '!' ));
        return operators.contains(c);
    }

    private Type getType_Word (String sToken) {
        switch (sToken) {
            case "import":
                return Type.IMPORT;
            case "switch":
                return Type.SWITCH;
            case "case":
                return Type.CASE;
            case "default":
                return Type.DEFAULT;
            case "break":
                return Type.BREAK;
            case "if":
                return Type.IF;
            case "else":
                return Type.ELSE;
            case "do":
                return Type.DO;
            case "while":
                return Type.WHILE;
            case "for":
                return Type.FOR;
            case "continue":
                return Type.CONTINUE;
            case "return":
                return Type.RETURN;
            case "func":
                return Type.FUNC;
            case "void":
                return Type.VOID;
            case "var":
                return Type.VAR;
            case "arr":
                return Type.ARR;
            case "true":
                return Type.TRUE;
            case "false":
                return Type.FALSE;
            case "null":
                return Type.NULL;
            default:
                return Type.IDENTIFIER;
        }   
    }

    private Type getType_Special (String sToken) {
        switch(sToken) {
            case "(":
                return Type.LPAREN;
            case ")":
                return Type.RPAREN;
            case "[":
                return Type.LBRACKET;
            case "]":
                return Type.RBRACKET;
            case "{":
                return Type.LBRACE;
            case "}":
                return Type.RBRACE;
            case ".":
                return Type.PERIOD;
            case ",":
                return Type.COMMA;
            case ";":
                return Type.SEMICOLON;
            case ":":
                return Type.COLON;
            default:
                return Type.UNKNOWN;
        }
    }

    private Token toToken_String (String str) {
        return new Token(Type.STR_LITERAL, str, curLine, fileName);
    }

    private Token toToken_Char (String str) {
        return new Token(Type.STR_LITERAL, "" + str.charAt(0), curLine, fileName);
    }

    private Token toToken_Number (String str) {
        if (str.contains(".")) {
            return new Token(Type.DBL_LITERAL, Double.parseDouble(str), curLine, fileName);
        } else {
            return new Token(Type.LNG_LITERAL, Integer.parseInt(str), curLine, fileName);
        }
        
    }

    private Token toToken_Word (String str, Type type) {
        return new Token(type, str, curLine, fileName);
    }

    private Token toToken_Operator (String str) {
        return new Token(Type.OPERATOR, str, curLine, fileName);
    }

    private Token toToken_Special (String str, Type type) {
        return new Token(type, str, curLine, fileName);
    }

    /**
     * Gets the next <code>Token</code>s in the <code>ArrayList</code> tokens 
     * 
     * @return the next <code>Token</code>s in the <code>ArrayList</code> tokens 
     */
    public Token next() {
        return tokens.get(tokCursor++);
    }

    /**
     * Returns true if there are still more <code>Token</code>s in the <code>ArrayList</code> tokens 
     * and false if not.
     * 
     * @return whether there are more <code>Token</code>s in the <code>ArrayList</code> tokens 
     */
    public boolean hasNext() {
        return tokCursor != tokens.size();
    }

    /**
     * Returns all the <code>Token</code>s in the text.
     * 
     * @return an <code>ArrayList</code> of all the <code>Token</code>s in the text
     */
    public ArrayList<Token> getTokens() {
        return tokens;
    }
}
