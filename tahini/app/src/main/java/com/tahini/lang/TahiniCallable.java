package com.tahini.lang;

import java.util.List;

interface TahiniCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
