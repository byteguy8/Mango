package mango.statement;

import java.util.List;

public class BlockStmt extends Statement{
    public final List<Statement> statements;

    public BlockStmt(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitBlockStmt(this);
    }
}