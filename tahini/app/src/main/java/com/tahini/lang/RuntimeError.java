package com.tahini.lang;

import java.util.List;

class RuntimeError extends RuntimeException {

    final Token token;

    final List<CallFrame> callStack;

    RuntimeError(Token token, String message, List<CallFrame> callStack) {
        super(message);
        this.token = token;
        this.callStack = callStack;
    }
}
