package mango;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    public static final class SymbolContainer {
        public Object value;

        public SymbolContainer(Object value) {
            this.value = value;
        }
    }

    private final Environment enclosing;
    private final Map<String, SymbolContainer> symbols;

    public Environment(Environment enclosing, Map<String, SymbolContainer> symbols) {
        this.enclosing = enclosing;
        this.symbols = symbols;
    }

    public Environment(Environment enclosing) {
        this(enclosing, new HashMap<>());
    }

    public Environment() {
        this(null);
    }

    private RuntimeError error(Token token, String message) {
        Mango.error(token, message);
        return new RuntimeError();
    }

    private SymbolContainer getContainer(Token identifier) {
        return symbols.get(identifier.lexeme);
    }

    public SymbolAdministrator administrate(Token identifier) {
        if (symbols.containsKey(identifier.lexeme)) {
            return new SymbolAdministrator() {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public void create() {
                    // No need implementation
                }

                @Override
                public String getName() {
                    return identifier.lexeme;
                }

                @Override
                public void setValue(Object value) {
                    getContainer(identifier).value = value;
                }

                @Override
                public Object getValue() {
                    return getContainer(identifier).value;
                }
            };
        } else if (enclosing != null)
            return enclosing.administrate(identifier);
        else
            throw error(identifier, String.format("Symbol '%s' do not exists.", identifier.lexeme));
    }

    public void create(String name, Object value) {
        if (symbols.containsKey(name))
            throw new IllegalStateException(String.format("Already exists a symbol with the name '%s'.", name));

        SymbolContainer container = new SymbolContainer(value);

        symbols.put(name, container);
    }

    public boolean contains(Token identifier) {
        return symbols.containsKey(identifier.lexeme);
    }

    public Object get(Token identifier) {
        if (symbols.containsKey(identifier.lexeme))
            return symbols.get(identifier.lexeme).value;

        throw error(identifier, String.format("Symbol '%s' do not exists.", identifier.lexeme));
    }

    public void declareMutate(Token identifier) {
        if (symbols.containsKey(identifier.lexeme))
            throw error(identifier, String.format("Already exists a symbol with the name '%s'.", identifier.lexeme));

        symbols.put(identifier.lexeme, new SymbolContainer(null));
    }

    public Environment declare(Token identifier) {
        if (symbols.containsKey(identifier.lexeme))
            throw error(identifier, String.format("Already exists a symbol with the name '%s'.", identifier.lexeme));

        HashMap<String, SymbolContainer> symbols = new HashMap<>(this.symbols);
        symbols.put(identifier.lexeme, new SymbolContainer(null));

        return new Environment(enclosing, symbols);
    }

    public void assign(Token identifier, Object value) {
        if (symbols.containsKey(identifier.lexeme)) {
            getContainer(identifier).value = value;
            return;
        }

        throw error(identifier, String.format("Symbol '%s' do not exists.", identifier.lexeme));
    }

    public void assignRecursive(Token identifier, Object value) {
        if (symbols.containsKey(identifier.lexeme))
            getContainer(identifier).value = value;
        else if (enclosing != null)
            enclosing.assignRecursive(identifier, value);
        else
            throw error(identifier, String.format("Symbol '%s' do not exists.", identifier.lexeme));
    }
}