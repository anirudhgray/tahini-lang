package com.tahini.lang;

import java.util.List;
import java.util.Scanner;

class StandardLibrary {

    public static void addStandardFunctions(Environment globalEnv) {
        globalEnv.define("input", new InputFunction());
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
