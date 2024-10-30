package com.tahini.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class StandardLibrary {

    public static void addStandardFunctions(Environment globalEnv) {
        globalEnv.define("input", new InputFunction());
        globalEnv.define("len", new ArrayLengthFunction());
    }

    public static void addInternalFunctions(Environment globalEnv, String namespace) {
        if (namespace == null) {
            defineInternalFunctions(globalEnv);
        } else {
            Environment mapEnv = new Environment();
            defineInternalFunctions(mapEnv);

            globalEnv.defineNamespace(namespace, mapEnv);
        }
    }

    private static void defineInternalFunctions(Environment mapEnv) {
        mapEnv.define("_keys", new HashmapKeysFunction());
        mapEnv.define("_values", new HashmapValuesFunction());
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
}
