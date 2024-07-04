package mango.expression;

public class LiteralExpr extends Expression{
    public final Object literal;

    public LiteralExpr(Object literal) {
        this.literal = literal;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitLiteralExpr(this);
    }
}