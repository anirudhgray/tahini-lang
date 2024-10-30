package com.tahini.lang;

import java.util.ArrayList;
import java.util.List;

class TahiniFunction implements TahiniCallable {

    private final Stmt.Function declaration;

    TahiniFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter,
            List<Object> arguments) {
        Environment environment = new Environment(interpreter.environment);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme,
                    arguments.get(i));
        }

        Expr failingPre = interpreter.evaluateContractConditions(declaration.preconditions, environment);
        if (failingPre != null) {
            throw new RuntimeError(declaration.name,
                    "Precondition failed.", new ArrayList<>());
        }

        Object returnValue = null;
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValueException) {
            returnValue = returnValueException.value;
        }

        Expr failingPost = interpreter.evaluateContractConditions(declaration.postconditions, environment);
        if (failingPost != null) {
            throw new RuntimeError(declaration.name,
                    "Postcondition failed.", new ArrayList<>());
        }

        return returnValue;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
