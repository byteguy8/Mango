package mango.expression;

import mango.Token;

public class GroupExpr extends Expression{
    public final Token leftParenthesis;
    public final Expression expression;

    public GroupExpr(Token leftParenthesis, Expression expression) {
        this.leftParenthesis = leftParenthesis;
        this.expression = expression;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitGroupExpr(this);
    }
}