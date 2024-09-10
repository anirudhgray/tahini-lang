package com.tahini.lang;

class CallFrame {

    final TahiniFunction function;
    final int returnToLine; // line number where the function was called

    CallFrame(TahiniFunction function, int returnToLine) {
        this.function = function;
        this.returnToLine = returnToLine;
    }
}
