package com.tahini.lang;

class CallFrame {

    final TahiniCallable function;
    final int returnToLine; // line number where the function was called

    CallFrame(TahiniCallable function, int returnToLine) {
        this.function = function;
        this.returnToLine = returnToLine;
    }
}
