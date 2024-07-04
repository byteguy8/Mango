package mango.statement;

import mango.Token;
import mango.expression.Expression;

public class VarDeclarationStmt extends Statement{
    public final Token identifier;
    public final Expression initializer;

    public VarDeclarationStmt(Token identifier, Expression initializer) {
        this.identifier = identifier;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitVarDeclarationStmt(this);
    }
}