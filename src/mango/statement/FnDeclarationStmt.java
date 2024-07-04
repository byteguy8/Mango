package mango.statement;

import mango.Token;

import java.util.List;

public class FnDeclarationStmt extends Statement{
    public final Token identifier;
    public final List<Token> parameters;
    public final List<Statement> body;

    public FnDeclarationStmt(Token identifier, List<Token> parameters, List<Statement> body) {
        this.identifier = identifier;
        this.parameters = parameters;
        this.body = body;
    }
    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitFnDeclarationStmt(this);
    }
}