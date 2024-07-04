package mango.statement;

import mango.Token;
import mango.expression.Expression;

public class PrintStmt extends Statement{
    public final Token printToken;
    public final Expression expression;

    public PrintStmt(Token printToken, Expression expression) {
        this.printToken = printToken;
        this.expression = expression;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitPrintStmt(this);
    }
}