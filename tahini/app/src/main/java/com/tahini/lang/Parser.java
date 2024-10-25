package com.tahini.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {

    private static class ParseError extends RuntimeException {
    }
    private final List<Token> tokens;
    private int current = 0;
    private int loopLevel = 0;
    private int functionLevel = 0;

    final boolean testMode;

    Parser(List<Token> tokens, boolean testMode) {
        this.tokens = tokens;
        this.testMode = testMode;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Stmt next = declaration();
            if (next != null) {
                statements.add(next);
            }
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            if (match(TokenType.FUN)) {
                return function();
            }
            if (match(TokenType.TEST)) {
                Stmt testStmt = testStatement();
                if (testMode) {
                    return testStmt;
                } else {
                    return null;
                }
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(block());
        }
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.BREAK)) {
            return breakStatement();
        }
        if (match(TokenType.RETURN)) {
            return returnStatement();
        }
        if (match(TokenType.ASSERTION)) {
            return assertationStatement();
        }
        return expressionStatement();
    }

    private int getLoopLevel() {
        return this.loopLevel;
    }

    private void incrementLoopLevel() {
        loopLevel += 1;
    }

    private void decrementLoopLevel() {
        loopLevel -= 1;
    }

    private Stmt breakStatement() {
        Token breakToken = previous();
        consume(TokenType.SEMICOLON, "Expected ';' after break.");
        if (getLoopLevel() <= 0) {
            error(breakToken, "Expected 'break' inside a loop.");
        }
        return new Stmt.Break();
    }

    private void beginFunction() {
        functionLevel++;
    }

    private void endFunction() {
        functionLevel--;
    }

    private boolean isInFunction() {
        return functionLevel > 0;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        if (!isInFunction()) {
            error(keyword, "Cannot return from top-level code.");
        }
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt forStatement() {
        try {
            incrementLoopLevel();
            consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
            Stmt initializer;
            if (match(TokenType.SEMICOLON)) {
                initializer = null;
            } else if (match(TokenType.VAR)) {
                initializer = varDeclaration();
            } else {
                initializer = expressionStatement();
            }
            Expr condition = null;
            if (!check(TokenType.SEMICOLON)) {
                condition = expression();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after condition.");
            Expr sideEffect = null;
            if (!check(TokenType.SEMICOLON)) {
                sideEffect = expression();
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after side effect.");
            Stmt body = statement();

            if (sideEffect != null) {
                body = new Stmt.Block(
                        Arrays.asList(
                                body,
                                new Stmt.Expression(sideEffect)));
            }

            if (condition == null) {
                condition = new Expr.Literal(true);
            }
            body = new Stmt.While(condition, body);

            if (initializer != null) {
                body = new Stmt.Block(Arrays.asList(initializer, body));
            }

            return body;
        } finally {
            decrementLoopLevel();
        }

    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'while' condition.");
        try {
            incrementLoopLevel();
            Stmt body = statement();

            return new Stmt.While(condition, body);
        } finally {
            decrementLoopLevel();
        }
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'if' condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Stmt testStatement() {
        Token name = consume(TokenType.STRING, "Expect test name.");
        Stmt body = statement();
        return new Stmt.Test(name, body);
    }

    private Stmt function() {
        beginFunction();
        Token name = consume(TokenType.IDENTIFIER, "Expect function name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");

        List<Token> parameters = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        // precondition and postcondition, both optional
        // TODO refactor this to helper methods
        // TODO add support for return check in postcondition
        List<Expr> preconditions = new ArrayList<>();
        List<Expr> postconditions = new ArrayList<>();
        if (match(TokenType.PRECONDITION)) {
            consume(TokenType.COLON, "Expect ':' after 'precondition'.");
            do {
                preconditions.add(expression());
            } while (match(TokenType.COMMA));
        }
        if (match(TokenType.POSTCONDITION)) {
            consume(TokenType.COLON, "Expect ':' after 'postcondition'.");
            do {
                postconditions.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");
        List<Stmt> body = block();
        endFunction();
        return new Stmt.Function(name, parameters, body, preconditions, postconditions);
    }

    private Stmt assertationStatement() {
        Token type = previous();
        consume(TokenType.COLON, "Expect ':' after 'assertation'.");
        List<Expr> conditions = new ArrayList<>();
        Object msg = null;
        do {
            if (match(TokenType.STRING)) {
                msg = previous().literal;
                break;
            }
            conditions.add(expression());
        } while (match(TokenType.COMMA));
        consume(TokenType.SEMICOLON, "Expect ';' after assertation.");
        return new Stmt.Contract(type, conditions, msg);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            if (expr instanceof Expr.Variable v) {
                return new Expr.Assign(v.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = logicalOr();

        if (match(TokenType.QUESTION_MARK)) {
            Expr left = ternary();
            consume(TokenType.COLON, "Expect ':' after expression.");
            Expr right = ternary();
            expr = new Expr.Ternary(expr, left, right);
        }

        return expr;
    }

    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Tahini.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case TokenType.CLASS:
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN:
                    return;
            }

            advance();
        }
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        }

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN,
                "Expect ')' after arguments.");

        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if (match(TokenType.LEFT_SQUARE)) {
            List<Expr> elements = new ArrayList<>();
            if (!check(TokenType.RIGHT_SQUARE)) {
                do {
                    elements.add(expression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_SQUARE, "Expect ']' after list elements.");
            return new Expr.TahiniList(elements);
        }

        // Error production for binary operator without left-hand operand
        if (match(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            throw error(operator, "Missing left hand operand.");
        }

        throw error(peek(), "Expected expression.");
    }
}
