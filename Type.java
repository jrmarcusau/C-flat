/** List of all possible types of tokens */
public enum Type {

    IMPORT,
    
    SWITCH, CASE, DEFAULT, BREAK, IF, ELSE, DO, WHILE, FOR, CONTINUE, RETURN,

    FUNC, VOID, VAR, ARR,

    IDENTIFIER, // Value is name of variable, method, structure, etc.

    LNG_LITERAL, DBL_LITERAL, STR_LITERAL, // Value is the value of the literal (int, double, or String)
    
    TRUE, FALSE, NULL,

    OPERATOR, /* Possible values:

        Operator precedence from least to greatest

        Assignment   =, +=, -=, *=, /=, %=, &=, |=, ^=, <<=, >>=, >>>=
        Ternary      ?
        Logical OR   ||
        Logical AND  &&
        Bitwise OR   |
        Bitwise XOR  ^
        Bitwise AND  &
        Equality     == !=
        Relational   < > <= >=
        Bitshift     <<, >>, >>>
        Term-like    + -
        Factor-like  * / %
        Unary        ++expr --expr + - ~ !
        Postfix      expr++ expr--

    */
    
    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, 
    PERIOD, COMMA, SEMICOLON, COLON, UNKNOWN,
    
    EOF // End of file
}
