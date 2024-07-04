package mango.expression;

public interface ExpressionVisitor<T> {
    T visitBinaryExpr(BinaryExpr expr);

    T visitLiteralExpr(LiteralExpr expr);

    T visitIdentifierExpr(IdentifierExpr expr);

    T visitAssignExpr(AssignExpr expr);

    T visitCallExpr(CallExpr expr);

    T visitComparisonExpr(ComparisonExpr expr);

    T visitGroupExpr(GroupExpr expr);

    T visitAccessExpr(AccessExpr expr);

    T visitThisExpr(ThisExpr expr);

    T visitArrayExpr(ArrayExpr expr);

    T visitArrayAccess(ArrayAccessExpr expr);

    T visitUnaryExpr(UnaryExpr expr);

    T visitLogicalExpr(LogicalExpr expr);

    T visitAnonymousFunction(AnonymousFunctionExpr expr);

    T visitEqualityExpr(Equality expr);
}