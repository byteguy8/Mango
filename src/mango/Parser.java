package mango;

import mango.expression.*;
import mango.statement.*;

import java.util.ArrayList;
import java.util.List;
public class Parser {
    private int current;
    private List<Token> tokens;

    private RuntimeError error(Token token, String message) {
        Mango.error(token, message);
        throw new RuntimeError();
    }

    private void sync() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case VAR:
                case IF:
                case WHILE:
                case FN:
                case CLASS:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType... types) {
        Token token = peek();

        for (TokenType type : types) {
            if (token.type == type)
                return true;
        }

        return false;
    }

    private boolean match(TokenType... types) {
        Token token = peek();

        for (TokenType type : types) {
            if (token.type == type) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        Token token = peek();

        if (token.type != type)
            throw error(previous(), message);

        advance();

        return token;
    }

    private Expression expression() {
        return assignExpr();
    }

    private Expression assignExpr() {
        Expression left = arrayExpr();

        if (match(TokenType.EQUALS)) {
            Token equalsToken = previous();
            Expression right = assignExpr();
            return new AssignExpr(left, equalsToken, right);
        }

        return left;
    }

    private Expression arrayExpr() {
        if (match(TokenType.LEFT_SQUARE)) {
            Token leftSquareToken = previous();
            List<Expression> values = new ArrayList<>();
            Expression length = null;

            if (!check(TokenType.RIGHT_SQUARE)) {
                do {
                    values.add(arrayExpr());
                } while (!isAtEnd() && match(TokenType.COMMA));
            }

            consume(TokenType.RIGHT_SQUARE, "Expect ']' at end of array values.");

            if (match(TokenType.COLON))
                length = termExpr();

            return new ArrayExpr(leftSquareToken, values, length);
        }

        return anonFnExpr();
    }

    private boolean validateAnonFnHeader() {
        final int previousCurrent = current;

        try {
            if (!match(TokenType.LEFT_PARENTHESIS))
                return false;

            if (!check(TokenType.RIGHT_PARENTHESIS)) {
                do {
                    if (match(TokenType.COMMA))
                        return true;

                    advance();

                } while (!isAtEnd() && !check(TokenType.RIGHT_PARENTHESIS));
            }

            match(TokenType.RIGHT_PARENTHESIS);

            if (match(TokenType.ARROW))
                return true;
        } finally {
            current = previousCurrent;
        }

        return false;
    }

    private Expression anonFnExpr() {
        if (validateAnonFnHeader()) {
            advance();

            List<Token> parameters;
            List<Statement> body;

            if (!check(TokenType.RIGHT_PARENTHESIS))
                parameters = fnParameters();
            else
                parameters = new ArrayList<>();

            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of anonymous function parameters.");
            consume(TokenType.ARROW, "Expect '=>' before anonymous function body.");
            consume(TokenType.LEFT_BRACKET, "Expect '{' at start of anonymous function body.");

            body = blockStmt();

            return new AnonymousFunctionExpr(parameters, body);
        }

        return logicalOr();
    }

    private Expression logicalOr() {
        Expression left = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = logicalAnd();
            left = new LogicalExpr(left, operator, right);
        }

        return left;
    }

    private Expression logicalAnd() {
        Expression left = equalityExpr();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equalityExpr();
            left = new LogicalExpr(left, operator, right);
        }

        return left;
    }

    private Expression equalityExpr() {
        Expression left = comparisonExpr();

        while (match(TokenType.EQUALS_EQUALS, TokenType.BANG_EQUALS, TokenType.EQUALS_EQUALS_EQUALS, TokenType.BANG_EQUALS_EQUALS)) {
            Token operator = previous();
            Expression right = comparisonExpr();
            left = new Equality(left, operator, right);
        }

        return left;
    }

    private Expression comparisonExpr() {
        Expression left = termExpr();

        while (match(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUALS, TokenType.LESS_EQUALS)) {
            Token operator = previous();
            Expression right = termExpr();
            left = new ComparisonExpr(left, operator, right);
        }

        return left;
    }

    private Expression termExpr() {
        Expression left = factorExpr();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = factorExpr();
            left = new BinaryExpr(left, operator, right);
        }

        return left;
    }

    private Expression factorExpr() {
        Expression left = unaryExpr();

        while (match(TokenType.ASTERISK, TokenType.SLASH)) {
            Token operator = previous();
            Expression right = unaryExpr();
            left = new BinaryExpr(left, operator, right);
        }

        return left;
    }

    private Expression unaryExpr() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unaryExpr();

            return new UnaryExpr(operator, right);
        }

        return getExpr();
    }

    private List<Expression> callArgs() {
        List<Expression> expressions = new ArrayList<>();

        do {
            if (expressions.size() > 255)
                throw error(peek(), "Call expressions can not have more than 255 arguments.");

            expressions.add(arrayExpr());
        } while (!isAtEnd() && match(TokenType.COMMA));

        return expressions;
    }

    private Expression getExpr() {
        Expression left = thisExpr();

        while (match(TokenType.LEFT_SQUARE, TokenType.LEFT_PARENTHESIS, TokenType.PERIOD)) {
            Token previous = previous();

            switch (previous.type) {
                case PERIOD: {
                    Token identifier = consume(TokenType.IDENTIFIER, "Expect identifier after '.' in access expression.");

                    left = new AccessExpr(left, previous, identifier);

                    break;
                }

                case LEFT_SQUARE: {
                    Expression index = termExpr();

                    left = new ArrayAccessExpr(left, previous, index);

                    consume(TokenType.RIGHT_SQUARE, "Expect ']' at end of array access expression.");

                    break;
                }

                case LEFT_PARENTHESIS: {
                    Token leftParenthesis = previous();

                    List<Expression> arguments;

                    if (!check(TokenType.RIGHT_PARENTHESIS))
                        arguments = callArgs();
                    else
                        arguments = new ArrayList<>();

                    consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of call.");

                    left = new CallExpr(left, leftParenthesis, arguments);

                    break;
                }
            }
        }

        return left;
    }

    private Expression thisExpr() {
        if (match(TokenType.THIS)) {
            Token thisToken = previous();
            Token identifier = null;

            if (match(TokenType.PERIOD))
                identifier = consume(TokenType.IDENTIFIER, "Expect identifier after '.' in this expression.");

            return new ThisExpr(thisToken, identifier);
        }

        return literalExpr();
    }

    private Expression literalExpr() {
        if (match(TokenType.NIL))
            return new LiteralExpr(null);

        if (match(TokenType.TRUE))
            return new LiteralExpr(true);

        if (match(TokenType.FALSE))
            return new LiteralExpr(false);

        if (match(TokenType.INTEGER_TYPE,
                TokenType.FLOAT_TYPE,
                TokenType.STRING_TYPE))
            return new LiteralExpr(previous().literal);

        if (match(TokenType.IDENTIFIER))
            return new IdentifierExpr(previous());

        if (match(TokenType.LEFT_PARENTHESIS)) {
            Token parenthesisToken = previous();
            Expression expression = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of group expression.");

            return new GroupExpr(parenthesisToken, expression);
        }

        throw error(peek(), String.format("Expected something, but got '%s'", peek().lexeme));
    }

    private Statement statement() {
        try {
            // declarative
            if (match(TokenType.VAR))
                return varDeclarationStmt();

            if (match(TokenType.FN))
                return fnDeclarationStmt();

            if (match(TokenType.CLASS))
                return classDeclarationStmt();

            // no declarative
            if (match(TokenType.PRINT))
                return printStmt();

            if (match(TokenType.RETURN))
                return returnStmt();

            if (match(TokenType.BREAK))
                return breakStmt();

            if (match(TokenType.CONTINUE))
                return continueStmt();

            if (match(TokenType.IF))
                return ifStmt();

            if (match(TokenType.LEFT_BRACKET))
                return new BlockStmt(blockStmt());

            if (match(TokenType.WHILE))
                return whileStmt();

            // others
            return expressionStmt();
        } catch (RuntimeError ex) {
            sync();
            return null;
        }
    }

    private Statement varDeclarationStmt() {
        Token identifier;
        Expression initializer = null;

        identifier = consume(TokenType.IDENTIFIER, "Expect identifier after 'cl' keyword.");

        if (match(TokenType.EQUALS))
            initializer = expression();

        consume(TokenType.SEMICOLON, "Expect ';' at end of variable declaration statement.");

        return new VarDeclarationStmt(identifier, initializer);
    }

    private List<Token> fnParameters() {
        List<Token> parameters = new ArrayList<>();

        do {
            if (parameters.size() > 255)
                throw error(peek(), "Function can not has more than 255 parameters");

            parameters.add(consume(TokenType.IDENTIFIER, "Expect identifier as function parameter"));
        } while (!isAtEnd() && match(TokenType.COMMA));

        return parameters;
    }

    private Statement fnDeclarationStmt() {
        Token identifier = consume(TokenType.IDENTIFIER, "Expect identifier after 'fn' keyword.");

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after function identifier.");

        List<Token> parameters;

        if (!check(TokenType.RIGHT_PARENTHESIS))
            parameters = fnParameters();
        else
            parameters = new ArrayList<>();

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of function parameters.");
        consume(TokenType.LEFT_BRACKET, "Expect '{' ar start of function body.");

        List<Statement> body = blockStmt();

        return new FnDeclarationStmt(identifier, parameters, body);
    }

    private Statement classDeclarationStmt() {
        Token identifier = consume(TokenType.IDENTIFIER, "Expect class name after 'class' keyword.");
        FnDeclarationStmt constructor = null;
        List<FnDeclarationStmt> methods = new ArrayList<>();

        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of class body.");

        if (!check(TokenType.RIGHT_BRACKET)) {
            if (match(TokenType.INIT)) {
                Token constructorToken = previous();
                List<Token> parameters;
                List<Statement> body;

                consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'init' keyword.");

                if (!check(TokenType.RIGHT_PARENTHESIS))
                    parameters = fnParameters();
                else
                    parameters = new ArrayList<>();

                consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of init parameters.");
                consume(TokenType.LEFT_BRACKET, "Expect '{' at start of init body.");

                body = blockStmt();

                constructor = new FnDeclarationStmt(constructorToken, parameters, body);
            }

            while (match(TokenType.FN)) {
                methods.add((FnDeclarationStmt) fnDeclarationStmt());
            }
        }

        consume(TokenType.RIGHT_BRACKET, "Expect '}' at end of class body.");

        return new ClassDeclarationStmt(identifier, constructor, methods);
    }

    private Statement printStmt() {
        Token printToken = previous();
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' at end of print statement.");
        return new PrintStmt(printToken, expression);
    }

    private Statement returnStmt() {
        Token returnToken = previous();
        Expression expression = null;

        if (!check(TokenType.SEMICOLON))
            expression = expression();

        consume(TokenType.SEMICOLON, "Expect ';' at end of return statement.");

        return new ReturnStmt(returnToken, expression);
    }

    private Statement breakStmt() {
        Token breakToken = previous();

        consume(TokenType.SEMICOLON, "Expect ';' at end of break statement.");

        return new BreakStmt(breakToken);
    }

    private Statement continueStmt() {
        Token breakToken = previous();

        consume(TokenType.SEMICOLON, "Expect ';' at end of continue statement.");

        return new ContinueStmt(breakToken);
    }

    private List<IfStmt.Branch> elifBranches() {
        List<IfStmt.Branch> branches = new ArrayList<>();

        do {
            Expression elifCondition;
            List<Statement> statements;

            consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'elif' keyword.");
            elifCondition = logicalOr();
            consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of elif condition.");

            consume(TokenType.LEFT_BRACKET, "Expect '{' at start of if body.");
            statements = blockStmt();

            branches.add(new IfStmt.Branch(elifCondition, statements));
        } while (!isAtEnd() && match(TokenType.ELIF));

        return branches;
    }

    private Statement ifStmt() {
        Expression ifCondition;
        List<Statement> ifStatements;
        List<IfStmt.Branch> elifBranches = null;
        List<Statement> elseStatements = null;

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' after 'if' keyword.");
        ifCondition = logicalOr();
        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of if condition.");

        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of if body.");
        ifStatements = blockStmt();

        if (match(TokenType.ELIF))
            elifBranches = elifBranches();

        if (match(TokenType.ELSE)) {
            consume(TokenType.LEFT_BRACKET, "Expect '{' at start of else body.");
            elseStatements = blockStmt();
        }

        return new IfStmt(ifCondition, ifStatements, elifBranches, elseStatements);
    }

    private List<Statement> blockStmt() {
        List<Statement> statements = new ArrayList<>();

        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                statements.add(statement());
            } while (!isAtEnd() && !check(TokenType.RIGHT_BRACKET));
        }

        consume(TokenType.RIGHT_BRACKET, "Expect '}' at end of block statement.");

        return statements;
    }

    private Statement whileStmt() {
        Token whileToken = previous();
        Expression condition;
        List<Statement> body;

        consume(TokenType.LEFT_PARENTHESIS, "Expect '(' at start of while condition.");

        condition = logicalOr();

        consume(TokenType.RIGHT_PARENTHESIS, "Expect ')' at end of while condition.");

        consume(TokenType.LEFT_BRACKET, "Expect '{' at start of while body.");

        body = blockStmt();

        return new WhileStmt(whileToken, condition, body);
    }

    private Statement expressionStmt() {
        Expression expression = expression();

        consume(TokenType.SEMICOLON, "Expect ';' at end of expression statement.");

        return new ExpressionStmt(expression);
    }

    public List<Statement> parse(List<Token> tokens) {
        this.tokens = tokens;

        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            Statement statement = statement();

            if (statement == null) continue;

            statements.add(statement);
        }

        return statements;
    }
}