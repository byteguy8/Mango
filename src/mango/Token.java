package mango;
public class Token {
    public final int line;
    public final String lexeme;
    public final Object literal;
    public final TokenType type;

    public Token(int line, String lexeme, Object literal, TokenType type) {
        this.line = line;
        this.lexeme = lexeme;
        this.literal = literal;
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("line: %d lexeme: %s type: %s literal: %s", line, lexeme, type, literal);
    }
}