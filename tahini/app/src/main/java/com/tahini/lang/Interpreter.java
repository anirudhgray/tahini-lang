package com.tahini.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    class BreakException extends RuntimeException {
    }

    final boolean repl;

    final Environment globals = new Environment();
    private Environment environment = globals;

    public Interpreter(boolean repl) {
        this.repl = repl;
        globals.define("clock", new TahiniCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
        StandardLibrary.addStandardFunctions(environment);
    }

    private final Stack<CallFrame> callStack = new Stack<>();

    private final List<String> testResults = new ArrayList<>();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Tahini.runtimeError(error);
        }

        if (!testResults.isEmpty()) {
            printTestResults();
        }
    }

    private void printTestResults() {
        System.out.println("Test Results:");
        for (String result : testResults) {
            System.out.println(result);
        }
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitTahiniListExpr(Expr.TahiniList expr) {
        List<Object> tahiniList = new ArrayList<>();
        for (Expr element : expr.elements) {
            tahiniList.add(evaluate(element));
        }
        return tahiniList;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException e) {
                break;
            }

        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object condition = evaluate(stmt.condition);
        if (isTruthy(condition)) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    public Expr evaluateContractConditions(List<Expr> conditions, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;

            for (Expr condition : conditions) {
                Object value = evaluate(condition);
                if (!isTruthy(value)) {
                    return condition;
                }
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    public Void visitContractStmt(Stmt.Contract stmt) {
        Object condition = evaluateContractConditions(stmt.conditions, environment);
        if (condition != null) {
            throw new RuntimeError(stmt.type, stmt.type.lexeme + " contract failed (" + stmt.msg + ")", new ArrayList<>());
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if (repl) {
            System.err.println(stringify(value));
        }
        return null;
    }

    @Override
    public Void visitTestStmt(Stmt.Test stmt) {
        try {
            execute(stmt.body);
            testResults.add("PASS " + "(line " + stmt.name.line + ")" + ": " + stmt.name.literal);
        } catch (RuntimeError error) {
            testResults.add("FAIL " + "(line " + stmt.name.line + ")" + ": " + stmt.name.literal + " (" + error.getMessage() + ")");
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        TahiniFunction function = new TahiniFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object initVal = null;
        if (stmt.initializer != null) {
            initVal = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, initVal);
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        Token operator = expr.operator;
        return switch (operator.type) {
            case TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case TokenType.BANG ->
                !isTruthy(right);
            default ->
                null;
        };
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.getValue(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.", new ArrayList<>());
    }

    public Boolean isTruthy(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Double && (Double) obj == 0) {
            return false;
        }
        if (obj instanceof Boolean b) {
            return b;
        }
        return true;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                checkZDE(expr.operator, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String || right instanceof String) {
                    yield (String) stringify(left) + (String) stringify(right);
                }
                if (left instanceof List && right instanceof List) {
                    List<Object> tahiniList = new ArrayList<>((List<Object>) left);
                    tahiniList.addAll((List<Object>) right);
                    yield tahiniList;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.", new ArrayList<>());
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case BANG_EQUAL ->
                !isEqual(left, right);
            case EQUAL_EQUAL ->
                isEqual(left, right);
            default ->
                null;
        };

    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof TahiniCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.", new ArrayList<>());
        }

        TahiniCallable function = (TahiniCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected "
                    + function.arity() + " arguments but got "
                    + arguments.size() + ".", new ArrayList<>());
        }

        CallFrame frame = new CallFrame(function, expr.paren.line);

        callStack.push(frame);

        Object result;
        try {
            result = function.call(this, arguments);
        } catch (RuntimeError error) {
            if (error.token == null) {
                throw new RuntimeError(expr.paren, error.getMessage(), new ArrayList<>(callStack));
            }
            throw new RuntimeError(error.token, error.getMessage(), new ArrayList<>(callStack));
        }

        callStack.pop();

        return result;
    }

    @Override
    public Object visitListAccessExpr(Expr.ListAccess expr) {
        Object list = evaluate(expr.list);
        Object index = evaluate(expr.index);

        if (!(list instanceof List)) {
            throw new RuntimeError(expr.paren, "Can only access elements of a list.", new ArrayList<>());
        }

        if (!(index instanceof Double)) {
            throw new RuntimeError(expr.paren, "Index must be a number.", new ArrayList<>());
        }

        List<Object> tahiniList = (List<Object>) list;
        int i = ((Double) index).intValue();

        if (i < 0 || i >= tahiniList.size()) {
            throw new RuntimeError(expr.paren, "Index out of bounds.", new ArrayList<>());
        }

        return tahiniList.get(i);
    }

    @Override
    public Object visitListSliceExpr(Expr.ListSlice expr) {
        Object list = evaluate(expr.list);
        Object start = evaluate(expr.start);
        Object end = evaluate(expr.end);

        if (!(list instanceof List)) {
            throw new RuntimeError(expr.paren, "Can only slice a list.", new ArrayList<>());
        }

        if (!(start instanceof Double) || !(end instanceof Double)) {
            throw new RuntimeError(expr.paren, "Start and end must be numbers.", new ArrayList<>());
        }

        List<Object> tahiniList = (List<Object>) list;
        int s = ((Double) start).intValue();
        int e = ((Double) end).intValue();

        if (s < 0 || e < 0 || s > e || e > tahiniList.size()) {
            throw new RuntimeError(expr.paren, "Invalid slice.", new ArrayList<>());
        }

        return tahiniList.subList(s, e);
    }

    private void checkNumberOperands(Token operator, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Both operands must be numbers.", new ArrayList<>());
    }

    private void checkZDE(Token operator, Object b) {
        if ((double) b == 0) {
            throw new RuntimeError(operator, "Oops, ZDE.", new ArrayList<>());
        }
    }

    private Boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        return isTruthy(condition) ? left : right;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Token variable = expr.name;
        Object value = evaluate(expr.value);
        environment.assign(variable, value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expr.right);
    }
}
