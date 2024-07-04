package mango.statement;

import mango.Token;
import mango.expression.Expression;

public class ReturnStmt extends Statement{
    public final Token returnToken;
    public final Expression value;

    public ReturnStmt(Token returnToken, Expression value) {
        this.returnToken = returnToken;
        this.value = value;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitReturnStmt(this);
    }
}