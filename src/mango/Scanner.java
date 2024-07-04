package mango;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private int line;
    private int start;
    private int current;
    private String source;
    private List<Token> tokens;
    private final Map<String, TokenType> keywords = new HashMap<>();

    public Scanner() {
        keywords.put("nil", TokenType.NIL);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("var", TokenType.VAR);
        keywords.put("if", TokenType.IF);
        keywords.put("elif", TokenType.ELIF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("fn", TokenType.FN);
        keywords.put("class", TokenType.CLASS);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("continue", TokenType.CONTINUE);
        keywords.put("this", TokenType.THIS);
        keywords.put("init", TokenType.INIT);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean match(char c) {
        if (peek() != c)
            return false;

        advance();

        return true;
    }

    private void addToken(Object literal, TokenType type) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(line, lexeme, literal, type));
    }

    private void addToken(TokenType type) {
        addToken(null, type);
    }

    private void comment() {
        while (!isAtEnd() && peek() != '\n')
            advance();
    }

    private void number() {
        TokenType type = TokenType.INTEGER_TYPE;

        while (!isAtEnd() && isDigit(peek()))
            advance();

        if (match('.')) {
            type = TokenType.FLOAT_TYPE;

            while (!isAtEnd() && isDigit(peek()))
                advance();
        }

        String rawNumber = source.substring(start, current);
        Object literal;

        if (type == TokenType.INTEGER_TYPE)
            literal = Long.valueOf(rawNumber);
        else
            literal = Double.valueOf(rawNumber);

        addToken(literal, type);
    }

    private void string() {
        while (!isAtEnd() && peek() != '"') {
            if (advance() == '\n')
                line++;
        }

        if (peek() != '"')
            Mango.error(line, "Unterminated string");

        advance();

        String str = source.substring(start + 1, current - 1);

        str = str.replace("\\t", "\t");
        str = str.replace("\\b", "\b");
        str = str.replace("\\f", "\f");
        str = str.replace("\\n", "\n");
        str = str.replace("\\r", "\r");
        str = str.replace("\\0", "\0");

        addToken(str, TokenType.STRING_TYPE);
    }

    private void identifier() {
        while (!isAtEnd() && isAlphaNumeric(peek()))
            advance();

        String key = source.substring(start, current);
        TokenType type = keywords.get(key);

        if (type == null)
            addToken(TokenType.IDENTIFIER);
        else
            addToken(type);
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '+':
                addToken(TokenType.PLUS);
                break;

            case '-':
                addToken(TokenType.MINUS);
                break;

            case '*':
                addToken(TokenType.ASTERISK);
                break;

            case '/':
                if (match('/'))
                    comment();
                else
                    addToken(TokenType.SLASH);
                break;

            case ':':
                addToken(TokenType.COLON);
                break;

            case ';':
                addToken(TokenType.SEMICOLON);
                break;

            case '.':
                addToken(TokenType.PERIOD);
                break;

            case ',':
                addToken(TokenType.COMMA);
                break;

            case '(':
                addToken(TokenType.LEFT_PARENTHESIS);
                break;

            case ')':
                addToken(TokenType.RIGHT_PARENTHESIS);
                break;

            case '[':
                addToken(TokenType.LEFT_SQUARE);
                break;

            case ']':
                addToken(TokenType.RIGHT_SQUARE);
                break;

            case '{':
                addToken(TokenType.LEFT_BRACKET);
                break;

            case '}':
                addToken(TokenType.RIGHT_BRACKET);
                break;

            case '=':
                if (match('>')) {
                    addToken(TokenType.ARROW);
                    break;
                }

                if (match('='))
                    if (match('='))
                        addToken(TokenType.EQUALS_EQUALS_EQUALS);
                    else
                        addToken(TokenType.EQUALS_EQUALS);
                else
                    addToken(TokenType.EQUALS);
                break;

            case '>':
                if (match('='))
                    addToken(TokenType.GREATER_EQUALS);
                else
                    addToken(TokenType.GREATER);
                break;

            case '<':
                if (match('='))
                    addToken(TokenType.LESS_EQUALS);
                else
                    addToken(TokenType.LESS);
                break;

            case '!':
                if (match('='))
                    if (match('='))
                        addToken(TokenType.BANG_EQUALS_EQUALS);
                    else
                        addToken(TokenType.BANG_EQUALS);
                else
                    addToken(TokenType.BANG);
                break;

            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                    break;
                }

            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                    break;
                }

            case ' ':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            default: {
                if (isDigit(c))
                    number();
                else if (c == '"')
                    string();
                else if (isAlphaNumeric(c))
                    identifier();
                else
                    Mango.error(line, String.format("Unknown token '%c'", c));
            }
        }
    }

    public List<Token> scan(String source) {
        this.line = 1;
        this.start = 0;
        this.current = 0;
        this.source = source;
        this.tokens = new ArrayList<>();

        while (!isAtEnd()) {
            scanToken();
            start = current;
        }

        addToken(TokenType.EOF);

        return tokens;
    }
}