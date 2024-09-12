package com.tahini.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Tahini {

    private static Interpreter interpreter;
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 2 || (args.length == 2 && !args[1].equals("--test"))) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 2 && args[1].equals("--test")) {
            runFile(args[0], true);
        } else if (args.length == 1) {
            runFile(args[0], false);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path, Boolean testMode) throws IOException {
        interpreter = new Interpreter(false);
        Path filePath = Paths.get(path).toAbsolutePath();
        byte[] bytes = Files.readAllBytes(filePath);
        run(new String(bytes, Charset.defaultCharset()), testMode);
        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        interpreter = new Interpreter(true);
        System.out.println("Welcome to Tahini. Type in your code below:");
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                System.out.println("Exiting prompt.");
                break;
            }
            run(line, false);
            hadError = false;
        }
    }

    private static void run(String source, Boolean testMode) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens, testMode);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.print("RuntimeError: " + error.getMessage()
                + "\n[line " + error.token.line + "]");

        List<CallFrame> callStack = error.callStack;
        for (int i = callStack.size() - 1; i >= 0; i--) {
            CallFrame frame = callStack.get(i);
            System.err.println(" in " + frame.function);
            System.err.print("[line " + frame.returnToLine + "]");
        }

        hadRuntimeError = true;
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
