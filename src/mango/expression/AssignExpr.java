package mango.expression;

import mango.Token;

public class AssignExpr extends Expression{
    public final Expression leftValue;
    public final Token equalsToken;
    public final Expression rightValue;

    public AssignExpr(Expression leftValue, Token equalsToken, Expression rightValue) {
        this.leftValue = leftValue;
        this.equalsToken = equalsToken;
        this.rightValue = rightValue;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitAssignExpr(this);
    }
}