package mango.expression;

import mango.Token;

public class UnaryExpr extends Expression{
    public final Token operator;
    public final Expression right;

    public UnaryExpr(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitUnaryExpr(this);
    }
}