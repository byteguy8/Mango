package mango.expression;

import mango.Token;
import mango.statement.Statement;

import java.util.List;

public class AnonymousFunctionExpr extends Expression{
    public final List<Token> parameters;
    public final List<Statement> body;

    public AnonymousFunctionExpr(List<Token> parameters, List<Statement> body) {
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitAnonymousFunction(this);
    }
}