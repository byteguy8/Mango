package mango;

import mango.statement.Statement;

import java.util.List;

public class MangoFunction implements MangoCallable {
    private final Environment enclosing;
    private final String name;
    private final List<Token> parameters;
    private final List<Statement> statements;

    public MangoFunction(Environment enclosing, String name, List<Token> parameters, List<Statement> statements) {
        this.enclosing = enclosing;
        this.name = name;
        this.parameters = parameters;
        this.statements = statements;
    }

    @Override
    public int arity() {
        return parameters.size();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String stringify() {
        return String.format("<fn %s>", name);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments, Token leftParenthesis) {
        Environment fnEnvironment = new Environment(enclosing);

        for (int i = 0; i < parameters.size(); i++) {
            Token identifier = parameters.get(i);
            Object value = arguments.get(i);

            fnEnvironment.declareMutate(identifier);
            fnEnvironment.assign(identifier, value);
        }

        try {
            interpreter.executeBlock(fnEnvironment, statements);
        } catch (ReturnValue returnValue) {
            return returnValue.value;
        }

        return null;
    }

    public List<Token> getParameters() {
        return parameters;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}