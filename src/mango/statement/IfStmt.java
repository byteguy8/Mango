package mango.statement;

import mango.expression.Expression;

import java.util.List;

public class IfStmt extends Statement {
    public static class Branch {
        public final Expression condition;
        public final List<Statement> statements;
        public Branch(Expression condition, List<Statement> statements) {
            this.condition = condition;
            this.statements = statements;
        }
    }

    public final Expression ifCondition;
    public final List<Statement> ifStatements;
    public final List<Branch> elifBranches;
    public final List<Statement> elseStatements;

    public IfStmt(Expression ifCondition, List<Statement> ifStatements, List<Branch> elifBranches, List<Statement> elseStatements) {
        this.ifCondition = ifCondition;
        this.ifStatements = ifStatements;
        this.elifBranches = elifBranches;
        this.elseStatements = elseStatements;
    }

    @Override
    public <T> T accept(StatementVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}