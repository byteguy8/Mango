package mango.statement;

import mango.Token;

public class BreakStmt extends Statement {
    public final Token breakToken;

    public BreakStmt(Token breakToken) {
        this.breakToken = breakToken;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBreakStmt(this);
    }
}