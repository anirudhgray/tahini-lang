package com.tahini.lang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class StandardLibrary {

    public static void addStandardFunctions(Environment globalEnv) {
        globalEnv.define("input", new InputFunction());
        globalEnv.define("len", new ArrayLengthFunction());
    }

    public static void addInternalFunctions(Environment globalEnv) {
        globalEnv.define("_keys", new HashmapKeysFunction());
        globalEnv.define("_values", new HashmapValuesFunction());
        globalEnv.define("_read", new FileReadFunction());
        globalEnv.define("_write", new FileWriteFunction());
    }
}

class FileWriteFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeError(null, "Expected 2 arguments (file path and content) but got " + args.size() + ".", null);
        }
        Object pathArg = args.get(0);
        Object contentArg = args.get(1);

        if (!(pathArg instanceof String path)) {
            throw new RuntimeError(null, "Expected a string (file path) but got " + pathArg + ".", null);
        }
        if (!(contentArg instanceof String content)) {
            throw new RuntimeError(null, "Expected a string (content) but got " + contentArg + ".", null);
        }

        try {
            Files.write(Path.of(path), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeError(null, "Error writing file: " + e.getMessage(), null);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class FileReadFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument (file path) but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);

        if (!(arg instanceof String path)) {
            throw new RuntimeError(null, "Expected a string (file path) but got " + arg + ".", null);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(path));
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeError(null, "Error reading file: " + e.getMessage(), null);
        }
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class HashmapValuesFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        if (!(arg instanceof Map)) {
            throw new RuntimeError(null, "Expected a hashmap but got " + arg + ".", null);
        }
        return new ArrayList<>(((Map) arg).values());
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class HashmapKeysFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        if (!(arg instanceof Map)) {
            throw new RuntimeError(null, "Expected a hashmap but got " + arg + ".", null);
        }
        return new ArrayList<>(((Map) arg).keySet());
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class InputFunction implements TahiniCallable {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(!args.isEmpty() ? args.get(0).toString() : "");
        return scanner.nextLine();
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}

class ArrayLengthFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        return switch (arg) {
            case List<?> list ->
                (double) list.size();
            case String str ->
                (double) str.length();
            default ->
                throw new RuntimeError(null, "Expected an array or string but got " + arg + ".", null);
        };
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
