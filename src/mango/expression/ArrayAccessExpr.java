package mango.expression;

import mango.Token;

public class ArrayAccessExpr extends Expression{
    public final Expression left;
    public final Token leftSquareToken;
    public final Expression index;

    public ArrayAccessExpr(Expression left, Token leftSquareToken, Expression index) {
        this.left = left;
        this.leftSquareToken = leftSquareToken;
        this.index = index;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }
}