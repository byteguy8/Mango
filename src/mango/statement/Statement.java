package mango.statement;

public abstract class Statement {
    public abstract <T> T accept(StatementVisitor<T> visitor);
}