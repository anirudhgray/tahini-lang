package com.tahini.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Environment {

    final Environment enclosing;

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    private final Map<String, Object> values = new HashMap<>();

    final Map<String, Environment> namespaces = new HashMap<>();

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void defineNamespace(String name, Environment namespace) {
        namespaces.put(name, namespace);
    }

    public Object getValue(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.getValue(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.", new ArrayList<>());
    }

    public Environment getNamespace(Token name) {
        if (namespaces.containsKey(name.lexeme)) {
            return namespaces.get(name.lexeme);
        }

        if (enclosing != null) {
            return enclosing.getNamespace(name);
        }

        throw new RuntimeError(name, "Undefined namespace '" + name.lexeme + "'.", new ArrayList<>());
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.", new ArrayList<>());
    }
}
