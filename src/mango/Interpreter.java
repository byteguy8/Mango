package mango;

import mango.expression.*;
import mango.statement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Interpreter implements ExpressionVisitor<Object>, StatementVisitor<Void> {
    private final static class Frame {
        public int whileBlocks;
        public final Token leftParenthesis;
        public final String name;

        private Frame(Token leftParenthesis, String name) {
            this.leftParenthesis = leftParenthesis;
            this.name = name;
        }
    }

    private final Environment globals = new Environment();
    private Environment locals = globals;

    private MangoMethod currentMethod = null;
    private MangoInstance currentInstance = null;
    private final Stack<Frame> callStack = new Stack<>();

    public Interpreter() {
        addNativeFunction("is_bool", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawObject = arguments.get(0);

            if (rawObject instanceof Long) {
                long value = (long) rawObject;
                return value == 0 || value == 1;
            }

            if (rawObject instanceof String) {
                String str = (String) rawObject;
                return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
            }

            return rawObject instanceof Boolean;
        }));

        addNativeFunction("is_int", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawObject = arguments.get(0);

            if (rawObject instanceof String) {
                String str = (String) rawObject;

                try {
                    Long.valueOf(str);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }

            return rawObject instanceof Long;
        }));

        addNativeFunction("is_float", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawObject = arguments.get(0);

            if (rawObject instanceof String) {
                String str = (String) rawObject;

                try {
                    Double.valueOf(str);
                    return true;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }

            return rawObject instanceof Double;
        }));

        addNativeFunction("is_str", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> arguments.get(0) instanceof String));
        addNativeFunction("is_arr", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> arguments.get(0) instanceof MangoArray));

        addNativeFunction("is_fn", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            return arguments.get(0) instanceof MangoFunction ||
                    arguments.get(0) instanceof MangoMethod ||
                    arguments.get(0) instanceof MangoNativeFunction;
        }));

        addNativeFunction("is_klass", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> arguments.get(0) instanceof MangoClass));

        addNativeFunction("is_instance", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> arguments.get(0) instanceof MangoInstance));

        addNativeFunction("is", new String[]{"a", "b"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawLeft = arguments.get(0);
            Object rawRight = arguments.get(1);

            if (!(rawLeft instanceof MangoClass))
                throw error(leftParenthesis, "Expect class name at argument 1.");


            if (rawRight instanceof MangoInstance) {
                MangoClass klass = (MangoClass) rawLeft;
                MangoInstance instance = (MangoInstance) rawRight;

                return klass == instance.getKlass();
            }

            return false;
        }));

        addNativeFunction("to_bool", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);

            if (rawValue instanceof Long) {
                long value = (long) rawValue;

                if (value < 0 || value > 1)
                    throw error(leftParenthesis, String.format("Failed to convert to bool from int. Expect 0 or 1, but got %d", value));

                return value == 1;
            }

            if (rawValue instanceof String) {
                String value = ((String) rawValue).toLowerCase();

                if (!value.equals("true") && !value.equals("false"))
                    throw error(leftParenthesis, String.format("Failed to convert to bool form str. Expect 'false' or 'true', but got '%.10s'", value));

                return value.equals("true");
            }

            if (rawValue instanceof Boolean)
                return rawValue;

            throw error(leftParenthesis, "Failed to convert to bool. Unexpected value type.");
        }));

        addNativeFunction("to_int", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);

            if (rawValue instanceof Long)
                return rawValue;

            if (rawValue instanceof Double)
                return (long) ((double) rawValue);

            if (rawValue instanceof String) {
                String value = (String) rawValue;

                try {
                    return (long) Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    throw error(leftParenthesis, "Failed to convert to int. Illegal value type.");
                }
            }

            throw error(leftParenthesis, "Failed to convert to int. Illegal value type.");
        }));

        addNativeFunction("to_float", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);

            try {
                return Double.valueOf(rawValue.toString());
            } catch (NumberFormatException ex) {
                throw error(leftParenthesis, "Failed to convert to float. Illegal value type.");
            }
        }));

        addNativeFunction("to_str", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> Utils.stringify(arguments.get(0))));

        addNativeFunction("millis", new String[]{}, ((interpreter, arguments, leftParenthesis) -> System.currentTimeMillis()));

        addNativeFunction("nano", new String[]{}, ((interpreter, arguments, leftParenthesis) -> System.nanoTime()));

        addNativeFunction("char_code", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);

            if (!(rawValue instanceof String))
                throw error(leftParenthesis, "Expect str as argument, but got something else.");

            String str = (String) rawValue;

            if (str.length() != 1)
                throw error(leftParenthesis, "Expect a str of length 1.");

            return (long) str.charAt(0);
        }));

        addNativeFunction("len", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);

            if (rawValue instanceof String)
                return (long) ((String) rawValue).length();

            if (rawValue instanceof MangoArray)
                return (long) ((MangoArray) rawValue).length();

            throw error(leftParenthesis, "Expect str or array");
        }));

        addNativeFunction("panic", new String[]{"value"}, ((interpreter, arguments, leftParenthesis) -> {
            throw error(leftParenthesis, arguments.get(0).toString());
        }));

        addNativeFunction("read", new String[]{}, ((interpreter, arguments, leftParenthesis) -> new Scanner(System.in).nextLine()));

        addNativeFunction("sub_str", new String[]{"value", "from", "to"}, ((interpreter, arguments, leftParenthesis) -> {
            Object rawValue = arguments.get(0);
            Object rawFrom = arguments.get(1);
            Object rawTo = arguments.get(2);

            if (!(rawValue instanceof String))
                throw error(leftParenthesis, "Illegal type of argument 0. Expect str.");

            if (!(rawFrom instanceof Long))
                throw error(leftParenthesis, "Illegal type of argument 1. Expect int.");

            if (!(rawTo instanceof Long))
                throw error(leftParenthesis, "Illegal type of argument 2. Expect int.");

            String value = (String) rawValue;
            int from = (int) ((long) rawFrom);
            int to = (int) ((long) rawTo);

            if (from > to)
                throw error(leftParenthesis, "Illegal value of argument 1. Must not be greater than argument 2.");

            if (from < 0)
                throw error(leftParenthesis, "Illegal value of argument 1. Must not be less than to 0.");

            if (to >= value.length())
                throw error(leftParenthesis, "Illegal value of argument 2. Must not be greater than str length.");

            return value.substring(from, to + 1);
        }));
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Token operator = expr.operator;

        return executeBinary(left, operator, right);
    }

    @Override
    public Object visitLiteralExpr(LiteralExpr expr) {
        return expr.literal;
    }

    @Override
    public Object visitIdentifierExpr(IdentifierExpr expr) {
        return locals.administrate(expr.identifier);
    }

    @Override
    public Object visitAssignExpr(AssignExpr expr) {
        Expression leftExpr = expr.leftValue;
        Object lValue = evaluateRaw(leftExpr);

        if (lValue instanceof SymbolAdministrator) {
            SymbolAdministrator administrator = (SymbolAdministrator) lValue;

            if (!administrator.exists())
                administrator.create();

            Object rvalue = evaluate(expr.rightValue);

            administrator.setValue(rvalue);

            return rvalue;
        }

        throw error(expr.equalsToken, "Illegal assignation target");
    }

    @Override
    public Object visitCallExpr(CallExpr expr) {
        Object rawCallable = evaluate(expr.left);
        List<Expression> rawArguments = expr.arguments;

        if (!(rawCallable instanceof MangoCallable))
            throw error(expr.leftParenthesis, "Expect callable at left side of '('.");

        MangoCallable callable = (MangoCallable) rawCallable;

        if (callable.arity() != rawArguments.size())
            throw error(expr.leftParenthesis, String.format("Illegal call. Expect %d arguments, but got %d", callable.arity(), rawArguments.size()));

        List<Object> arguments = new ArrayList<>();

        for (Expression argument : rawArguments)
            arguments.add(evaluate(argument));

        final MangoMethod previousMethod = currentMethod;
        final MangoInstance previousInstance = currentInstance;

        if (rawCallable instanceof MangoMethod) {
            MangoMethod method = (MangoMethod) rawCallable;

            currentMethod = method;
            currentInstance = method.getInstance();
        }

        callStack.push(new Frame(expr.leftParenthesis, callable.name()));

        Object returnedValue;

        try {
            returnedValue = callable.call(this, arguments, expr.leftParenthesis);
        } catch (StackOverflowError ex) {
            throw error(expr.leftParenthesis, "StackOverFlow");
        }

        callStack.pop();

        currentMethod = previousMethod;
        currentInstance = previousInstance;

        return returnedValue;
    }

    @Override
    public Object visitComparisonExpr(ComparisonExpr expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Token operator = expr.operator;

        return executeComparison(left, operator, right);
    }

    @Override
    public Object visitGroupExpr(GroupExpr expr) {
        if (expr.expression != null)
            return evaluate(expr.expression);

        return null;
    }

    @Override
    public Object visitAccessExpr(AccessExpr expr) {
        Object rawObject = evaluate(expr.left);
        Token identifier = expr.right;

        if (!(rawObject instanceof MangoInstance))
            throw error(expr.periodToken, "Access expressions expect class instance.");

        MangoInstance instance = (MangoInstance) rawObject;

        if (!instance.contains(identifier))
            throw error(identifier, String.format("Instance '%s' does not contains member named '%s'.", instance.getName(), identifier.lexeme));

        return instance.get(identifier);
    }

    @Override
    public Object visitThisExpr(ThisExpr expr) {
        Token thisToken = expr.thisToken;
        Token identifier = expr.identifier;

        if (currentInstance == null)
            throw error(thisToken, "'this' expressions can only be used in instances scope");

        return currentInstance.get(identifier);
    }

    @Override
    public Object visitArrayExpr(ArrayExpr expr) {
        Token leftToken = expr.leftSquareToken;
        List<Expression> values = expr.values;
        Expression lengthExpr = expr.length;

        int length = values.size();

        if (lengthExpr != null) {
            Object rawLength = evaluate(lengthExpr);

            if (!(rawLength instanceof Long))
                throw error(leftToken, "Failed to create array. Illegal array length value type. Expect int.");

            length = (int) ((long) rawLength);
        }

        if (!values.isEmpty() && values.size() != length)
            throw error(leftToken, String.format("Failed to create array. Array length %d, but got %d values.", length, values.size()));

        Object[] arrayValues = new Object[length];

        for (int i = 0; i < values.size(); i++) {
            Expression value = values.get(i);
            arrayValues[i] = evaluate(value);
        }

        return new MangoArray(arrayValues);
    }

    @Override
    public Object visitArrayAccess(ArrayAccessExpr expr) {
        Expression left = expr.left;
        Token leftSquareToken = expr.leftSquareToken;
        Expression indexExpr = expr.index;

        Object lvalue = evaluate(left);

        if (!(lvalue instanceof String) && !(lvalue instanceof MangoArray))
            throw error(leftSquareToken, "Illegal left value in array access expression.");

        Object rvalue = evaluate(indexExpr);

        if (!(rvalue instanceof Long))
            throw error(leftSquareToken, "Expect int as array access index.");

        int index = (int) ((long) rvalue);

        if (lvalue instanceof String) {
            String str = (String) lvalue;

            if (index >= str.length())
                throw error(leftSquareToken, String.format("Index %d out of bounds %d for str.", index, str.length()));

            return String.valueOf(str.charAt(index));
        }

        MangoArray array = (MangoArray) lvalue;

        if (index >= array.length())
            throw error(leftSquareToken, String.format("Index %d out of bounds %d for array.", index, array.length()));

        return array.get(index);
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr expr) {
        Token operator = expr.operator;
        Expression rightExpr = expr.right;

        Object right = evaluate(rightExpr);

        switch (operator.type) {
            case BANG:
                if (!isBoolean(right))
                    throw error(operator, "Prefix operator ! expect bool in right side.");

                return !((boolean) right);

            case MINUS:
                if (!isNumeric(right))
                    throw error(operator, "Prefix operator - expect number in right side.");

                if (right instanceof Double)
                    return -((double) right);

                return -((long) right);
        }

        return null;
    }

    @Override
    public Object visitLogicalExpr(LogicalExpr expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Token operator = expr.operator;

        if (operator.type == TokenType.OR) {
            if (!isBoolean(left))
                throw error(operator, "Infix operator || expect numbers in both sides, but left is not.");

            if (!isBoolean(right))
                throw error(operator, "Infix operator || expect numbers in both sides, but right is not.");

            if (truthly(left))
                return true;

            return truthly(right);
        }

        if (operator.type == TokenType.AND) {
            if (!isBoolean(left))
                throw error(operator, "Infix operator && expect numbers in both sides, but left is not.");

            if (!isBoolean(right))
                throw error(operator, "Infix operator && expect numbers in both sides, but right is not.");

            if (!truthly(left))
                return false;

            return truthly(right);
        }

        return null;
    }

    @Override
    public Object visitAnonymousFunction(AnonymousFunctionExpr expr) {
        List<Token> parameters = expr.parameters;
        List<Statement> body = expr.body;

        return new MangoCallable() {
            final Environment enclosing = locals;

            @Override
            public int arity() {
                return parameters.size();
            }

            @Override
            public String name() {
                return "<anonymous fn>";
            }

            @Override
            public String stringify() {
                return "<anonymous fn>";
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
                    interpreter.executeBlock(fnEnvironment, body);
                } catch (ReturnValue returnValue) {
                    return returnValue.value;
                }

                return null;
            }
        };
    }

    @Override
    public Object visitEqualityExpr(Equality expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        Token operator = expr.operator;

        if (operator.type == TokenType.BANG_EQUALS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator != expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator != expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) != toDouble(right);

            return (long) left != (long) right;
        }

        if (operator.type == TokenType.EQUALS_EQUALS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator == expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator == expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) == toDouble(right);

            return (long) left == (long) right;
        }

        if (operator.type == TokenType.BANG_EQUALS_EQUALS) {
            if (!isComposed(left))
                throw error(operator, "Infix operator !== expect a composed type in both sides, but left is not.");

            if (!isComposed(right))
                throw error(operator, "Infix operator !== expect a composed type in both sides, but right is not.");

            return left.equals(right);
        }

        if (operator.type == TokenType.EQUALS_EQUALS_EQUALS) {
            if (!isComposed(left))
                throw error(operator, "Infix operator === expect a composed type in both sides, but left is not.");

            if (!isComposed(right))
                throw error(operator, "Infix operator === expect a composed type in both sides, but right is not.");

            return left.equals(right);
        }

        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionStmt stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(Utils.stringify(value));
        return null;
    }

    @Override
    public Void visitVarDeclarationStmt(VarDeclarationStmt stmt) {
        Token identifier = stmt.identifier;
        Expression initializer = stmt.initializer;

        declare(identifier);

        if (initializer != null) {
            Object value = evaluate(initializer);
            assign(identifier, value);
        }

        return null;
    }

    @Override
    public Void visitFnDeclarationStmt(FnDeclarationStmt stmt) {
        Token identifier = stmt.identifier;

        declare(identifier);

        MangoFunction function = new MangoFunction(
                locals,
                identifier.lexeme,
                stmt.parameters,
                stmt.body
        );

        assign(identifier, function);

        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt stmt) {
        executeBlock(new Environment(locals), stmt.statements);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt stmt) {
        if (callStack.isEmpty())
            throw error(stmt.returnToken, "Illegal use of return statement: Wrong scope.");

        if (currentMethod != null && currentMethod.isConstructor())
            throw error(stmt.returnToken, "Illegal use of return statement: Wrong scope.");

        Expression rawValue = stmt.value;

        if (rawValue != null)
            throw new ReturnValue(evaluate(rawValue));

        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt stmt) {
        Expression ifCondition = stmt.ifCondition;
        List<Statement> ifStatements = stmt.ifStatements;
        List<IfStmt.Branch> elifBranches = stmt.elifBranches;
        List<Statement> elseStatements = stmt.elseStatements;

        Object ifValue = evaluate(ifCondition);

        if (truthly(ifValue)) {
            executeBlock(new Environment(locals), ifStatements);
            return null;
        }

        if (elifBranches != null) {
            for (IfStmt.Branch elifBranch : elifBranches) {
                Object elifValue = evaluate(elifBranch.condition);
                List<Statement> elifStatements = elifBranch.statements;

                if (truthly(elifValue)) {
                    executeBlock(new Environment(locals), elifStatements);
                    return null;
                }
            }
        }

        if (elseStatements != null)
            executeBlock(new Environment(locals), elseStatements);

        return null;
    }

    @Override
    public Void visitClassStmt(ClassDeclarationStmt stmt) {
        Token identifier = stmt.identifier;
        FnDeclarationStmt rawConstructor = stmt.constructor;
        List<FnDeclarationStmt> rawMethods = stmt.methods;

        declare(identifier);

        MangoFunction constructor = null;

        if (rawConstructor != null)
            constructor = new MangoFunction(
                    null,
                    String.format("init_%s", identifier.lexeme),
                    rawConstructor.parameters,
                    rawConstructor.body
            );

        final Environment classEnvironment = new Environment(locals);
        MangoClass klass = new MangoClass(identifier.lexeme, classEnvironment, constructor);

        for (FnDeclarationStmt rawMethod : rawMethods) {
            Token methodIdentifier = rawMethod.identifier;

            if (klass.name().equals(methodIdentifier.lexeme))
                throw error(rawMethod.identifier, "Methods can not have the same name as the class where is declared.");

            if (klass.contains(methodIdentifier.lexeme))
                throw error(rawMethod.identifier, String.format("Class '%s' already has method named '%s'", klass.name(), methodIdentifier.lexeme));

            MangoFunction method = new MangoFunction(
                    null,
                    rawMethod.identifier.lexeme,
                    rawMethod.parameters,
                    rawMethod.body
            );

            klass.declare(methodIdentifier.lexeme);
            klass.assign(methodIdentifier.lexeme, method);
        }

        assign(identifier, klass);

        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt stmt) {
        Expression conditionExpr = stmt.condition;
        List<Statement> body = stmt.body;

        if (!callStack.isEmpty())
            callStack.peek().whileBlocks++;

        try {
            while (truthly(evaluate(conditionExpr)))
                executeWhileBlock(body);
        } catch (Break ignored) {
            return null;
        }

        if (!callStack.isEmpty())
            callStack.peek().whileBlocks--;

        return null;
    }

    @Override
    public Void visitBreakStmt(BreakStmt stmt) {
        if (callStack.isEmpty())
            throw error(stmt.breakToken, "Can't use 'break' outside a loop scope.");

        if (callStack.peek().whileBlocks == 0)
            throw error(stmt.breakToken, "Can't use 'break' outside a loop scope.");

        throw new Break();
    }

    @Override
    public Void visitContinueStmt(ContinueStmt stmt) {
        if (callStack.isEmpty())
            throw error(stmt.continueToken, "Can't use 'continue' outside a loop scope.");

        if (callStack.peek().whileBlocks == 0)
            throw error(stmt.continueToken, "Can't use 'continue' outside a loop scope.");

        throw new Continue();
    }

    public MangoInstance getCurrentInstance() {
        return currentInstance;
    }

    public void setCurrentInstance(MangoInstance currentInstance) {
        this.currentInstance = currentInstance;
    }

    public MangoMethod getCurrentMethod(){
        return currentMethod;
    }

    public void setCurrentMethod(MangoMethod currentMethod) {
        this.currentMethod = currentMethod;
    }

    private RuntimeError error(Token token, String message) {
        while (!callStack.isEmpty()) {
            Frame frame = callStack.pop();
            System.err.printf("at [ln: %d] in %s\n", frame.leftParenthesis.line, frame.name);
        }

        Mango.error(token, message);

        return new RuntimeError();
    }

    private boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    private boolean isNumeric(Object value) {
        return value instanceof Long || value instanceof Double;
    }

    private boolean isComposed(Object value) {
        return value instanceof String ||
                value instanceof MangoArray ||
                value instanceof MangoFunction ||
                value instanceof MangoMethod ||
                value instanceof MangoClass ||
                value instanceof MangoInstance;
    }

    private double toDouble(Object value) {
        if (!isNumeric(value))
            throw new IllegalArgumentException("value is not a number");

        if (value instanceof Long)
            return (double) ((long) value);

        return (double) value;
    }

    private void addNativeFunction(String name, String[] parameters, MangoNativeFunction.ResolveNative resolve) {
        MangoNativeFunction nativeFunction = new MangoNativeFunction(name, parameters) {
            @Override
            public Object resolve(Interpreter interpreter, List<Object> arguments, Token leftParenthesis) {
                return resolve.resolve(interpreter, arguments, leftParenthesis);
            }
        };

        globals.create(name, nativeFunction);
    }

    private Object evaluateRaw(Expression expression) {
        return expression.accept(this);
    }

    private Object evaluate(Expression expression) {
        Object value = expression.accept(this);

        if (value instanceof SymbolAdministrator)
            return ((SymbolAdministrator) value).getValue();

        return value;
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void executeBlock(Environment environment, List<Statement> statements) {
        final Environment previous = locals;
        locals = environment;

        try {
            for (Statement statement : statements)
                execute(statement);
        } finally {
            locals = previous;
        }
    }

    private void executeWhileBlock(List<Statement> statements) {
        try {
            executeBlock(new Environment(locals), statements);
        } catch (Continue ignored) {
        }
    }

    private void declare(Token identifier) {
        locals = locals.declare(identifier);
    }

    private void assign(Token identifier, Object value) {
        locals.assignRecursive(identifier, value);
    }

    private Object executeStringsConcatenation(Object left, Token operator, Object right) {
        if (!(left instanceof String) && !(right instanceof String))
            return null;

        if (!(right instanceof String))
            throw error(operator, "Strings concatenation expect str, but only left is.");

        if (!(left instanceof String))
            throw error(operator, "Strings concatenation expect str, but only right is.");

        return String.format("%s%s", left, right);
    }

    private String multiplyString(String str, long count) {
        StringBuilder sb = new StringBuilder();

        for (long i = 0; i < count; i++)
            sb.append(str);

        return sb.toString();
    }

    private Object executeStringMultiplication(Object left, Token operator, Object right) {
        if (!(left instanceof String) && !(right instanceof String))
            return null;

        if (left instanceof String) {
            if (!(right instanceof Long))
                throw error(operator, "String multiplication expect int in one of the sides, but right is not.");

            return multiplyString((String) left, (long) right);
        }

        if (!(left instanceof Long))
            throw error(operator, "String multiplication expect int in one of the sides, but left is not.");

        return multiplyString((String) right, (long) left);
    }

    public Object executeComparison(Object left, Token operator, Object right) {
        if (operator.type == TokenType.LESS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator < expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator < expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) < toDouble(right);

            return (long) left < (long) right;
        }

        if (operator.type == TokenType.GREATER) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator > expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator > expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) > toDouble(right);

            return (long) left > (long) right;
        }

        if (operator.type == TokenType.LESS_EQUALS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator <= expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator <= expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) <= toDouble(right);

            return (long) left <= (long) right;
        }

        if (operator.type == TokenType.GREATER_EQUALS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator >= expect numbers in both sides, but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator >= expect numbers in both sides, but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) >= toDouble(right);

            return (long) left >= (long) right;
        }

        return null;
    }

    public Object executeBinary(Object left, Token operator, Object right) {
        if (operator.type == TokenType.PLUS) {
            Object concatValue = executeStringsConcatenation(left, operator, right);

            if (concatValue != null) return concatValue;

            if (!isNumeric(left))
                throw error(operator, "Infix operator + expect numbers in both sides but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator + expect numbers in both sides but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) + toDouble(right);

            return (long) left + (long) right;
        }

        if (operator.type == TokenType.MINUS) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator - expect numbers in both sides but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator - expect numbers in both sides but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) - toDouble(right);

            return (long) left - (long) right;
        }

        if (operator.type == TokenType.ASTERISK) {
            Object stringMultiplication = executeStringMultiplication(left, operator, right);

            if (stringMultiplication != null) return stringMultiplication;

            if (!isNumeric(left))
                throw error(operator, "Infix operator * expect numbers in both sides but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator * expect numbers in both sides but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) * toDouble(right);

            return (long) left * (long) right;
        }

        if (operator.type == TokenType.SLASH) {
            if (!isNumeric(left))
                throw error(operator, "Infix operator / expect numbers in both sides but left is not.");

            if (!isNumeric(right))
                throw error(operator, "Infix operator / expect numbers in both sides but right is not.");

            if (left instanceof Double || right instanceof Double)
                return toDouble(left) / toDouble(right);

            return (long) left / (long) right;
        }

        return null;
    }

    private boolean truthly(Object value) {
        if (value == null)
            return false;

        if (value instanceof Boolean)
            return (Boolean) value;

        if (value instanceof Long && (long) value == 0L)
            return false;

        return !(value instanceof Double) || (double) value != 0.0;
    }

    public void execute(List<Statement> statements) {
        for (Statement statement : statements) {
            if (statement instanceof ReturnStmt) {
                Mango.error(((ReturnStmt) statement).returnToken, "Can't return from global scope.");
                break;
            }

            try {
                execute(statement);
            } catch (RuntimeError ex) {
                break;
            }
        }
    }
}