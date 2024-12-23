package com.tahini.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    class BreakException extends RuntimeException {
    }

    final boolean repl;
    private int functionDepth = 0;

    private final Set<Path> scoopedFiles = new HashSet<>();

    final Environment globals = new Environment();
    public Environment environment = globals;

    public Interpreter(boolean repl) {
        this.repl = repl;
        StandardLibrary.addStandardFunctions(environment);
        StandardLibrary.addInternalFunctions(environment);
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
    public Object visitTahiniMapExpr(Expr.TahiniMap expr) {
        Map<Object, Object> tahiniMap = new HashMap<>();
        for (int i = 0; i < expr.keys.size(); i++) {
            Object key = evaluate(expr.keys.get(i));
            Object value = evaluate(expr.values.get(i));
            tahiniMap.put(key, value);
        }
        return tahiniMap;
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
        if (condition != null && stmt.type.type == TokenType.ASSERTION) {
            throw new RuntimeError(stmt.type, stmt.type.lexeme + " contract failed (" + stmt.msg + ")", new ArrayList<>());
        } else if (condition != null && stmt.type.type == TokenType.WARNING) {
            // stderr
            System.err.println("Warning (" + stmt.msg + ") [" + stmt.type.filename + ":" + stmt.type.line + "]");
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if (repl && functionDepth == 0) {
            System.err.println(stringify(value));
        }
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        List<Stmt> importedDeclarations;
        try {
            importedDeclarations = loadAndParseFile(stmt.path);
        } catch (IOException e) {
            throw new RuntimeError(stmt.path, "Error importing file " + stmt.path.lexeme + ".", new ArrayList<>());
        }

        if (stmt.name != null) {
            Environment previous = this.environment;
            Environment importedEnv = new Environment();

            try {
                this.environment = importedEnv;
                StandardLibrary.addStandardFunctions(environment);
                StandardLibrary.addInternalFunctions(environment);

                for (Stmt statement : importedDeclarations) {
                    if (statement instanceof Stmt.Function || statement instanceof Stmt.Var || statement instanceof Stmt.Import) {
                        execute(statement);
                    }
                }
            } finally {
                this.environment = previous;
            }

            environment.defineNamespace(stmt.name.lexeme, importedEnv);
        } else {
            for (Stmt statement : importedDeclarations) {
                if (statement instanceof Stmt.Function || statement instanceof Stmt.Var || statement instanceof Stmt.Import) {
                    execute(statement);
                }
            }
        }
        scoopedFiles.remove(Paths.get((String) stmt.path.literal).toAbsolutePath());

        return null;
    }

    private List<Stmt> loadAndParseFile(Token path) throws IOException {
        String importPath = (String) path.literal;
        List<Stmt> parsedStatements = new ArrayList<>();

        if (importPath.startsWith("larder/")) {
            String stdlibFilePath = "/stdlib" + importPath.substring("larder".length()) + ".tah";
            parsedStatements.addAll(loadSingleStdlibModule(stdlibFilePath, path));
        } else {
            Path filePath = Paths.get(importPath).toAbsolutePath();
            if (scoopedFiles.contains(filePath)) {
                throw new RuntimeError(path, "Circular import detected.", new ArrayList<>());
            }
            scoopedFiles.add(filePath);

            byte[] bytes = Files.readAllBytes(filePath);
            String source = new String(bytes, Charset.defaultCharset());
            parsedStatements.addAll(parseSource(source, importPath));
        }

        return parsedStatements;
    }

    private List<Stmt> loadSingleStdlibModule(String stdlibFilePath, Token path) throws IOException {
        InputStream stdlibStream = getClass().getResourceAsStream(stdlibFilePath);
        if (stdlibStream == null) {
            throw new RuntimeError(path, "File " + stdlibFilePath + " not found in the larder.", new ArrayList<>());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdlibStream, StandardCharsets.UTF_8))) {
            String source = reader.lines().collect(Collectors.joining("\n"));
            return parseSource(source, stdlibFilePath);
        }
    }

    private List<Stmt> parseSource(String source, String sourcePath) {
        Parser parser = new Parser(new Scanner(source, sourcePath).scanTokens(), false);
        List<Stmt> allStatements = parser.parse();

        return allStatements.stream()
                .filter(stmt -> stmt instanceof Stmt.Function || stmt instanceof Stmt.Var || stmt instanceof Stmt.Import)
                .collect(Collectors.toList());
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

    @Override
    public Object visitNamespacedVariableExpr(Expr.NamespacedVariable expr) {
        List<Token> nameParts = expr.nameParts;
        Environment env = environment;
        for (int i = 0; i < nameParts.size() - 1; i++) {
            env = env.getNamespace(nameParts.get(i));
        }
        return env.getValue(nameParts.get(nameParts.size() - 1));
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
            case MODULO -> {
                checkNumberOperands(expr.operator, left, right);
                checkZDE(expr.operator, right);
                yield (double) left % (double) right;
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

        CallFrame frame = new CallFrame(function, expr.paren.line, expr.paren.filename);

        callStack.push(frame);

        Object result;
        try {
            this.functionDepth++;
            if (expr.callee instanceof Expr.NamespacedVariable namespacedVariable) {
                List<Token> nameParts = namespacedVariable.nameParts;
                Environment env = environment;
                Environment previousEnv = environment;
                for (int i = 0; i < nameParts.size() - 1; i++) {
                    env = env.getNamespace(nameParts.get(i));
                }
                this.environment = env;
                result = function.call(this, arguments);
                this.environment = previousEnv;
            } else {
                result = function.call(this, arguments);
            }
        } catch (RuntimeError error) {
            if (error.token == null) {
                throw new RuntimeError(expr.paren, error.getMessage(), new ArrayList<>(callStack));
            }
            throw new RuntimeError(error.token, error.getMessage(), new ArrayList<>(callStack));
        } finally {
            this.functionDepth--;
        }

        callStack.pop();

        return result;
    }

    @Override
    public Object visitListAccessExpr(Expr.ListAccess expr) {
        Object collection = evaluate(expr.list);
        Object index = evaluate(expr.index);

        if (!(collection instanceof List || collection instanceof String || collection instanceof Map)) {
            throw new RuntimeError(expr.paren, "Can only access elements of a list, map or a string.", new ArrayList<>());
        }

        return switch (collection) {
            case List<?> list -> {
                if (!(index instanceof Double)) {
                    throw new RuntimeError(expr.paren, "Index must be a number for list access.", new ArrayList<>());
                }
                int i = ((Double) index).intValue();
                if (i < 0 || i >= list.size()) {
                    throw new RuntimeError(expr.paren, "Index out of bounds.", new ArrayList<>());
                }
                yield list.get(i);
            }
            case String str -> {
                if (!(index instanceof Double)) {
                    throw new RuntimeError(expr.paren, "Index must be a number for string access.", new ArrayList<>());
                }
                int i = ((Double) index).intValue();
                if (i < 0 || i >= str.length()) {
                    throw new RuntimeError(expr.paren, "Index out of bounds.", new ArrayList<>());
                }
                yield String.valueOf(str.charAt(i));
            }
            case Map<?, ?> map -> {
                if (!map.containsKey(index)) {
                    throw new RuntimeError(expr.paren, "Key not found in map.", new ArrayList<>());
                }
                yield map.get(index);
            }
            default ->
                throw new RuntimeError(expr.paren, "Unexpected error.", new ArrayList<>());
        };
    }

    @Override
    public Object visitListSliceExpr(Expr.ListSlice expr) {
        Object collection = evaluate(expr.list);
        Object start = evaluate(expr.start);
        Object end = evaluate(expr.end);

        if (!(collection instanceof List || collection instanceof String)) {
            throw new RuntimeError(expr.paren, "Can only slice a list or a string.", new ArrayList<>());
        }

        if (!(start instanceof Double) || !(end instanceof Double)) {
            throw new RuntimeError(expr.paren, "Start and end must be numbers.", new ArrayList<>());
        }

        int s = ((Double) start).intValue();
        int e = ((Double) end).intValue();

        return switch (collection) {
            case List<?> list -> {
                if (s < 0 || e < 0 || s > e || e > list.size()) {
                    throw new RuntimeError(expr.paren, "Index out of bounds.", new ArrayList<>());
                }
                yield list.subList(s, e);
            }
            case String str -> {
                if (s < 0 || e < 0 || s > e || e > str.length()) {
                    throw new RuntimeError(expr.paren, "Index out of bounds.", new ArrayList<>());
                }
                yield str.substring(s, e);
            }
            default ->
                throw new RuntimeError(expr.paren, "Unexpected error.", new ArrayList<>());
        };
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
