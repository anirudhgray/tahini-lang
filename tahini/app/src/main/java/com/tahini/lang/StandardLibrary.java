package com.tahini.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class StandardLibrary {

    public static void addStandardFunctions(Environment globalEnv) {
        globalEnv.define("input", new InputFunction());
        globalEnv.define("len", new ArrayLengthFunction());
        globalEnv.define("clock", new UnixEpochSecondsFunction());
    }

    public static void addInternalFunctions(Environment globalEnv) {
        globalEnv.define("_keys", new HashmapKeysFunction());
        globalEnv.define("_values", new HashmapValuesFunction());
        globalEnv.define("_read", new FileReadFunction());
        globalEnv.define("_write", new FileWriteFunction());
        globalEnv.define("_random", new RandomHelperFunction());
        globalEnv.define("_http", new HTTPRestFunction());
    }
}

class HTTPRestFunction implements TahiniCallable {

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeError(null, "Expected 2 arguments but got " + args.size() + ".", null);
        }
        Object urlarg = args.get(0);
        if (!(urlarg instanceof String url)) {
            throw new RuntimeError(null, "Expected a string url but got " + urlarg + ".", null);
        }
        Object methodarg = args.get(1);
        if (!(methodarg.equals("GET"))) {
            throw new RuntimeError(null, "Expected 'GET' but got " + methodarg + ".", null);
        }
        String response;
        try {
            response = sendGetRequest(url);
            return response;
        } catch (IOException e) {
            throw new RuntimeError(null, "Error sending HTTP request: " + e.getMessage(), null);
        }
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    private static String sendGetRequest(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();
        } else {
            return "Failed to get the API response. Response code: " + responseCode;
        }
    }
}

class RandomHelperFunction implements TahiniCallable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return Math.random();
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}

class UnixEpochSecondsFunction implements TahiniCallable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}

class FileWriteFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeError(null, "Expected 2 arguments (file path and content) but got " + args.size() + ".", null);
        }
        Object pathArg = args.get(0);
        Object contentArg = args.get(1);

        if (!(pathArg instanceof String path)) {
            throw new RuntimeError(null, "Expected a string (file path) but got " + pathArg + ".", null);
        }
        if (!(contentArg instanceof String content)) {
            throw new RuntimeError(null, "Expected a string (content) but got " + contentArg + ".", null);
        }

        try {
            Files.write(Path.of(path), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeError(null, "Error writing file: " + e.getMessage(), null);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class FileReadFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument (file path) but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);

        if (!(arg instanceof String path)) {
            throw new RuntimeError(null, "Expected a string (file path) but got " + arg + ".", null);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(path));
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeError(null, "Error reading file: " + e.getMessage(), null);
        }
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class HashmapValuesFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        if (!(arg instanceof Map)) {
            throw new RuntimeError(null, "Expected a hashmap but got " + arg + ".", null);
        }
        return new ArrayList<>(((Map) arg).values());
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class HashmapKeysFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        if (!(arg instanceof Map)) {
            throw new RuntimeError(null, "Expected a hashmap but got " + arg + ".", null);
        }
        return new ArrayList<>(((Map) arg).keySet());
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}

class InputFunction implements TahiniCallable {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(!args.isEmpty() ? args.get(0).toString() : "");
        return scanner.nextLine();
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}

class ArrayLengthFunction implements TahiniCallable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeError(null, "Expected 1 argument but got " + args.size() + ".", null);
        }
        Object arg = args.get(0);
        return switch (arg) {
            case List<?> list ->
                (double) list.size();
            case String str ->
                (double) str.length();
            default ->
                throw new RuntimeError(null, "Expected an array or string but got " + arg + ".", null);
        };
    }

    @Override
    public String toString() {
        return "<native fn>";
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
