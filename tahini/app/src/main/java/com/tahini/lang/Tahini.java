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

    public static void main(String[] args) {
        try {
            if (args.length > 2 || (args.length == 2 && !args[1].equals("--test") && !args[1].equals("--visualize"))) {
                System.out.println("Usage: jlox [script]");
                System.exit(64);
            } else if (args.length == 2 && args[1].equals("--test")) {
                runFile(args[0], true);
            } else if (args.length == 2 && args[1].equals("--visualize")) {
                visualizeAST(args[0]);
            } else if (args.length == 1) {
                runFile(args[0], false);
            } else {
                runPrompt();
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(74);
        }
    }

    private static void runFile(String path, Boolean testMode) throws IOException {
        interpreter = new Interpreter(false);
        Path filePath = Paths.get(path).toAbsolutePath();
        byte[] bytes = Files.readAllBytes(filePath);
        run(new String(bytes, Charset.defaultCharset()), testMode, filePath.normalize().toString());
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
            run(line, false, null);
            hadError = false;
        }
    }

    private static void run(String source, Boolean testMode, String filename) {
        Scanner scanner = new Scanner(source, filename);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens, testMode);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    static void error(String filename, int line, String message) {
        report(filename, line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.print("RuntimeError: " + error.getMessage()
                + "\n[at line " + error.token.line + " in " + error.token.filename + "]");

        List<CallFrame> callStack = error.callStack;
        for (int i = callStack.size() - 1; i >= 0; i--) {
            CallFrame frame = callStack.get(i);
            System.err.println(" in " + frame.function);
            System.err.println("[called at line " + frame.returnToLine + " in " + frame.returnToFilename + "]");
        }

        hadRuntimeError = true;
    }

    private static void report(String filename, int line, String where,
            String message) {
        System.err.println(
                "[file " + filename + "][line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.filename, token.line, " at end", message);
        } else {
            report(token.filename, token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void visualizeAST(String path) throws IOException {
        Path filePath = Paths.get(path).toAbsolutePath();
        byte[] bytes = Files.readAllBytes(filePath);
        String source = new String(bytes, Charset.defaultCharset());

        Scanner scanner = new Scanner(source, path);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens, false);
        List<Stmt> statements = parser.parse();

        if (hadError) {
            return;
        }

        ASTVisualizer visualizer = new ASTVisualizer();
        visualizer.display(statements);
    }

}
