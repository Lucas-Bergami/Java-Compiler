public class token {
    public enum Type {
        FUNCTION,
        MAIN,
        LET,
        INT,
        FLOAT,
        CHAR,
        IF,
        ELSE,
        WHILE,
        PRINTLN,
        RETURN,
        LBRACKET,
        RBRACKET,
        LBRACE,
        RBRACE,
        ARROW,
        COLON,
        SEMICOLON,
        COMMA,
        ASSIGN,
        EQ,
        NE,
        GT,
        GE,
        LT,
        LE,
        PLUS,
        MINUS,
        MULT,
        DIV,
        ID,
        INT_CONST,
        FLOAT_CONST,
        CHAR_LITERAL,
        FMT_STRING
    }

    private final Type type;
    private final String lexeme;
    private final int lineNumber;

    public token(Type type, String lexeme, int lineNumber) {
        this.type = type;
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

    public Type getType() { return type; }
    public String getLexeme() { return lexeme; }
    public int getLineNumber() { return lineNumber; }
}
