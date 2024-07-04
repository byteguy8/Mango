package mango.expression;

import mango.Token;

import java.util.List;

public class CallExpr extends Expression{
    public final Expression left;
    public final Token leftParenthesis;
    public final List<Expression> arguments;

    public CallExpr(Expression left, Token leftParenthesis, List<Expression> arguments) {
        this.left = left;
        this.leftParenthesis = leftParenthesis;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitCallExpr(this);
    }
}