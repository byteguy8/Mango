package mango;

public abstract class SymbolAdministrator {
    public abstract boolean exists();

    public abstract void create();

    public abstract String getName();

    public abstract void setValue(Object value);

    public abstract Object getValue();
}