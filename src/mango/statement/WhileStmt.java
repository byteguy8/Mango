package mango.statement;

import mango.Token;
import mango.expression.Expression;

import java.util.List;

public class WhileStmt extends Statement{
    public final Token whileToken;
    public final Expression condition;
    public final List<Statement> body;

    public WhileStmt(Token whileToken, Expression condition, List<Statement> body) {
        this.whileToken = whileToken;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}