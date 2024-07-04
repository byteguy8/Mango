package mango;

public class MangoArray {
    private final Object[] values;

    public MangoArray(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return String.format("<array: %d>", values.length);
    }

    public int length() {
        return values.length;
    }

    public SymbolAdministrator get(int index) {
        return new SymbolAdministrator() {
            @Override
            public boolean exists() {
                return index >= 0 && index < values.length;
            }

            @Override
            public void create() {
                // No need implementation
            }

            @Override
            public String getName() {
                return values[index].toString();
            }

            @Override
            public void setValue(Object value) {
                values[index] = value;
            }

            @Override
            public Object getValue() {
                return values[index];
            }
        };
    }
}