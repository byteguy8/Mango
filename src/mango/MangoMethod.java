package mango;

import mango.statement.Statement;

import java.util.List;

public class MangoMethod implements MangoCallable {
    private final MangoInstance instance;
    private final MangoFunction function;

    public MangoMethod(MangoInstance instance, MangoFunction function) {
        if (instance == null)
            throw new IllegalArgumentException("instance must not be null");

        this.instance = instance;
        this.function = function;
    }

    @Override
    public int arity() {
        return function.arity();
    }

    @Override
    public String name() {
        return function.name();
    }

    @Override
    public String stringify() {
        return String.format("<method '%s'>", name());
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token leftParenthesis) {
        Environment environment = new Environment(instance.getEnvironment());

        List<Token> parameters = function.getParameters();
        List<Statement> statements = function.getStatements();

        for (int i = 0; i < parameters.size(); i++) {
            Token identifier = parameters.get(i);
            Object value = arguments.get(i);

            environment.declareMutate(identifier);
            environment.assign(identifier, value);
        }

        try {
            interpreter.executeBlock(environment, statements);
        } catch (ReturnValue returnValue) {
            return returnValue.value;
        }

        return null;
    }

    public MangoInstance getInstance() {
        return instance;
    }
}