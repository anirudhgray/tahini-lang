package com.tahini.lang;

import java.util.List;

interface TahiniCallable {

    int arity();

    boolean isInternal();

    Object call(Interpreter interpreter, List<Object> arguments);
}
