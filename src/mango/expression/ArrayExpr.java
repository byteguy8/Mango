package mango.expression;

import mango.Token;

import java.util.List;

public class ArrayExpr extends Expression {
    public final Token leftSquareToken;
    public final List<Expression> values;
    public final Expression length;

    public ArrayExpr(Token leftSquareToken, List<Expression> values, Expression length) {
        this.leftSquareToken = leftSquareToken;
        this.values = values;
        this.length = length;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitArrayExpr(this);
    }
}