package com.tahini.lang;

class CallFrame {

    final TahiniCallable function;
    final int returnToLine; // line number where the function was called
    final String returnToFilename; // filename where the function was called

    CallFrame(TahiniCallable function, int returnToLine, String returnToFilename) {
        this.function = function;
        this.returnToLine = returnToLine;
        this.returnToFilename = returnToFilename;
    }
}
