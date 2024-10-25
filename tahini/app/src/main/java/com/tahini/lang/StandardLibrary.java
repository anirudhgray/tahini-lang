package com.tahini.lang;

import java.util.List;
import java.util.Scanner;

class StandardLibrary {

    public static void addStandardFunctions(Environment globalEnv) {
        globalEnv.define("input", new InputFunction());
        globalEnv.define("len", new ArrayLengthFunction());
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
        if (!(args.get(0) instanceof List)) {
            throw new RuntimeError(null, "Expected an array but got " + args.get(0) + ".", null);
        }
        return (double) ((List<Object>) args.get(0)).size();
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
