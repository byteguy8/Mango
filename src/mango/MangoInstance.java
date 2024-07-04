package mango;

public class MangoInstance {
    private final MangoClass klass;
    private final Environment environment;

    public MangoInstance(MangoClass klass, Environment environment) {
        this.klass = klass;
        this.environment = environment;
    }

    @Override
    public String toString() {
        return String.format("<Instance of '%s'>", klass.name());
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getName() {
        return klass.name();
    }

    public MangoClass getKlass() {
        return klass;
    }

    public boolean contains(Token identifier) {
        return environment.contains(identifier);
    }

    public SymbolAdministrator get(Token identifier) {
        if (identifier == null) {
            return new SymbolAdministrator() {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public void create() {

                }

                @Override
                public String getName() {
                    return MangoInstance.this.getName();
                }

                @Override
                public void setValue(Object value) {

                }

                @Override
                public Object getValue() {
                    return MangoInstance.this;
                }
            };
        }

        return new SymbolAdministrator() {
            @Override
            public boolean exists() {
                return contains(identifier);
            }

            @Override
            public void create() {
                environment.declareMutate(identifier);
            }

            @Override
            public String getName() {
                return contains(identifier) ? identifier.lexeme : Utils.stringify(null);
            }

            @Override
            public void setValue(Object value) {
                environment.assign(identifier, value);
            }

            @Override
            public Object getValue() {
                return environment.get(identifier);
            }
        };
    }
}