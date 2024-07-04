package mango.expression;

import mango.Token;

public class IdentifierExpr extends Expression{
    public final Token identifier;

    public IdentifierExpr(Token identifier) {
        this.identifier = identifier;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitIdentifierExpr(this);
    }
}