package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        Path filePath = Paths.get(path).toAbsolutePath();
        System.out.println("Attempting to read file: " + filePath);
        byte[] bytes = Files.readAllBytes(filePath);
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        System.out.println("Welcome to Lox. Type in your code below:");
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                System.out.println("Exiting prompt.");
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        System.out.println("Running code: " + source);
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) {
            return;
        }

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
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
