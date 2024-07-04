package mango.statement;

import mango.expression.Expression;

public class ExpressionStmt extends Statement {
    public final Expression expression;

    public ExpressionStmt(Expression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}