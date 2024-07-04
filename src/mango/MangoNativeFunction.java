package mango;

import java.util.List;

public abstract class MangoNativeFunction implements MangoCallable {
    public static interface ResolveNative{
        Object resolve(Interpreter interpreter, List<Object> arguments, Token leftParenthesis);
    }

    private final String name;
    private final String[] parameters;

    protected MangoNativeFunction(String name, String[] parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    @Override
    public int arity() {
        return parameters.length;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String stringify() {
        return String.format("<native fn '%s'>", name);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token leftParenthesis) {
        return resolve(interpreter, arguments, leftParenthesis);
    }

    public abstract Object resolve(Interpreter interpreter, List<Object> arguments, Token leftParenthesis);
}