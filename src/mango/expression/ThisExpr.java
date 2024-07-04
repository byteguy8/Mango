package mango.expression;

import mango.Token;

public class ThisExpr extends Expression{
    public final Token thisToken;
    public final Token identifier;

    public ThisExpr(Token thisToken, Token identifier) {
        this.thisToken = thisToken;
        this.identifier = identifier;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitThisExpr(this);
    }
}