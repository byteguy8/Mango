package mango;

import java.util.List;

public interface MangoCallable {
    int arity();

    String name();

    String stringify();

    Object call(Interpreter interpreter, List<Object> arguments, Token leftParenthesis);
}