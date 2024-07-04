package mango.expression;

import mango.Token;

public class BinaryExpr extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public BinaryExpr(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}