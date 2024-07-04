package mango;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MangoClass implements MangoCallable {
    private final String name;
    private final Environment local;
    private final MangoFunction constructor;
    private final Map<String, MangoFunction> functions = new HashMap<>();

    public MangoClass(String name, Environment environment, MangoFunction constructor) {
        this.name = name;
        this.local = environment;
        this.constructor = constructor;
    }

    @Override
    public int arity() {
        return constructor == null ? 0 : constructor.arity();
    }

    @Override
    public String stringify() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token leftParenthesis) {
        Environment instanceEnvironment = new Environment(local);
        MangoInstance instance = new MangoInstance(this, instanceEnvironment);

        for (Map.Entry<String, MangoFunction> entry : functions.entrySet())
            instanceEnvironment.create(entry.getKey(), new MangoMethod(instance, entry.getValue()));

        if (constructor != null) {
            MangoMethod method = new MangoMethod(instance, constructor);

            MangoInstance previous = interpreter.getCurrentInstance();
            interpreter.setCurrentInstance(instance);

            method.call(interpreter, arguments, leftParenthesis);

            interpreter.setCurrentInstance(previous);
        }

        return instance;
    }

    @Override
    public String toString() {
        return String.format("<class %s>", name);
    }

    public String name() {
        return name;
    }

    public boolean contains(String identifier) {
        return functions.containsKey(identifier);
    }

    public void declare(String identifier) {
        functions.put(identifier, null);
    }

    public void assign(String identifier, Object value) {
        if (!(value instanceof MangoFunction))
            throw new IllegalArgumentException("Only functions are allowed");

        functions.put(identifier, (MangoFunction) value);
    }
}