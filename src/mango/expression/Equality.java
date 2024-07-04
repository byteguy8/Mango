package mango.expression;

import mango.Token;

public class Equality extends Expression {
    public final Expression left;
    public final Token operator;
    public final Expression right;

    public Equality(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitEqualityExpr(this);
    }
}