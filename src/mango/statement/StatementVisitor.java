package mango.statement;

public interface StatementVisitor<T> {
    T visitExpressionStmt(ExpressionStmt stmt);

    T visitPrintStmt(PrintStmt stmt);

    T visitVarDeclarationStmt(VarDeclarationStmt stmt);

    T visitFnDeclarationStmt(FnDeclarationStmt stmt);

    T visitBlockStmt(BlockStmt stmt);

    T visitReturnStmt(ReturnStmt stmt);

    T visitIfStmt(IfStmt stmt);

    T visitClassStmt(ClassDeclarationStmt stmt);

    T visitWhileStmt(WhileStmt stmt);

    T visitBreakStmt(BreakStmt stmt);

    T visitContinueStmt(ContinueStmt stmt);
}