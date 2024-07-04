package mango.statement;

import mango.Token;

import java.util.List;

public class ClassDeclarationStmt extends Statement{
    public final Token identifier;
    public final FnDeclarationStmt constructor;
    public final List<FnDeclarationStmt> methods;

    public ClassDeclarationStmt(Token identifier, FnDeclarationStmt constructor, List<FnDeclarationStmt> methods) {
        this.identifier = identifier;
        this.constructor = constructor;
        this.methods = methods;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitClassStmt(this);
    }
}