package mango.statement;

import mango.Token;

public class ContinueStmt extends Statement{
    public final Token continueToken;

    public ContinueStmt(Token continueToken) {
        this.continueToken = continueToken;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitContinueStmt(this);
    }
}