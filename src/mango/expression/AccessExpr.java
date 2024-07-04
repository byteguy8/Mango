package mango.expression;

import mango.Token;

public class AccessExpr extends Expression{
    public final Expression left;
    public final Token periodToken;
    public final Token right;

    public AccessExpr(Expression left, Token periodToken, Token right) {
        this.left = left;
        this.periodToken = periodToken;
        this.right = right;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitAccessExpr(this);
    }
}