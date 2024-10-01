# Tahini Language

> Tahini is a paste made from sesame seeds that can be used as a dip, spread, or dressing. It's versatile, flavorful, and adds a unique touch to many dishes, while being a healthy choice. Inspired by the simplicity, flexibility, and richness of tahini, we present Tahini, a programming language that aims to be a joy to use, with a focus on simplicity, expressiveness, and extensive testing support.

**Tahini** is a lightweight, tree-based interpreted programming language that is written using Java, and which runs on the JVM (Java Virtual Machine), inspired by Lox and Python. It aims to provide simplicity and expressiveness alongside extensive testing and contract support, making it a joy for developers to use. Currently, Tahini supports a number of core language and testing features, with an exciting roadmap of future capabilities, including an import system, auto function mocking, and cross-language support.

```
var check = 10;
fun percentage(part, total)
    precondition: total > 0, part >= 0
    postcondition: result >= 0, result <= 100
{
    var result = (part / total) * 100;
    assertion: check == 10, "Unwanted side effects!";
    return result;
}

test "percentage test" {
    assertion: percentage(20, 50) == 40;
    assertion: percentage(10, 100) == 10;
}

print percentage(20, 28);
```

## Table of Contents
- [Tahini Language](#tahini-language)
  - [Table of Contents](#table-of-contents)
  - [Features](#features)
  - [Getting Started](#getting-started)
    - [Quick Start — Using Prebuilt Binaries](#quick-start--using-prebuilt-binaries)
    - [Installation — Building from Source](#installation--building-from-source)
      - [Verify Installation via Tests](#verify-installation-via-tests)
    - [Running Tahini Code](#running-tahini-code)
  - [Syntax Overview](#syntax-overview)
    - [Variables](#variables)
    - [Functions](#functions)
      - [Contracts (Preconditions, Postconditions and Assertions)](#contracts-preconditions-postconditions-and-assertions)
      - [Unit Tests](#unit-tests)
    - [Conditionals](#conditionals)
    - [Loops](#loops)
  - [Planned Features](#planned-features)
  - [Stretch Goals](#stretch-goals)
- [The Theory Behind This Implementation of Tahini](#the-theory-behind-this-implementation-of-tahini)
  - [What is a Tree-Walk Interpreter?](#what-is-a-tree-walk-interpreter)
  - [Why Tree-Walk on the JVM?](#why-tree-walk-on-the-jvm)
  - [Future Evolution](#future-evolution)

## Features

Tahini currently implements:
- [x] **Variables**: Declare mutable variables using a simple and concise syntax.
- [x] **Loops**: Supports `while` and `for` loops to handle iteration.
- [x] **Conditionals**: If-else statements for decision-making.
- [x] **Functions**: Define and call reusable blocks of code, with support for contracts (`precondition`, `postcondition`, and `assertion`).
- [ ] **Classes**: Object-oriented features to group variables and methods (in progress).
- [ ] **Advanced Data Structures**: Support for lists, maps, and other data structures (in progress).
- [ ] **Error Handling**: Support for user-defined exceptions and error handling (in progress).
- [x] **Stack Traces**: Detailed error messages with line numbers and function names.
- [x] **Unit Tests**: Write test blocks directly in the source file to validate code correctness.
  
Planned features include an import system, standard library, and cross-language support.

## Getting Started

### Quick Start — Using Prebuilt Binaries

You can download the latest prebuilt binaries for your operating system from the [Releases](https://github.com/anirudhgray/tahini-lang/releases) page. Follow these steps to get started quickly.

1. Download the Latest Binary
   - Linux: [tahini-ubuntu-latest](https://github.com/anirudhgray/tahini-lang/releases/download/v0.0.1/tahini-linux)
   - macOS: [tahini-macos-latest](https://github.com/anirudhgray/tahini-lang/releases/download/v0.0.1/tahini-macos)
   - Windows: [tahini-windows-latest.exe](https://github.com/anirudhgray/tahini-lang/releases/download/v0.0.1/tahini.exe)

2. Make the Binary Executable (Linux/macOS)
```bash
chmod +x tahini-linux # For Linux
chmod +x tahini-macos # For macOS
```

1. Run Tahini (start up the REPL)
```bash
./tahini-linux # For Linux
./tahini-macos # For macOS
tahini.exe # For Windows
Welcome to Tahini. Type in your code below:
>
```

1. Run Tahini Code (execute a Tahini script)
```bash
./tahini-linux path/to/file.tah # For Linux
./tahini-macos path/to/file.tah # For macOS
tahini.exe path/to/file.tah # For Windows
```

### Installation — Building from Source

If you prefer to build Tahini from the source code, follow these instructions:

To get started with Tahini, clone the repository and build the project using Maven or Gradle. Since Tahini is built on top of the JVM, ensure you have a valid Java JDK installed (>=21).

```bash
git clone
cd tahini
gradle build
```

> If you get an error, it may be due to the Java version. Ensure you have Java 21 or higher installed, or use the `./gradlew` wrapper to run the project.
> ```bash
> ./gradlew run --args="path/to/file.tah"
> ```

#### Verify Installation via Tests

To ensure that Tahini is correctly installed, you can run the test suite using the provided script:

```bash
(>_>) ./run_tests.sh
Building the project...

BUILD SUCCESSFUL in 439ms
5 actionable tasks: 5 up-to-date
Running regular tests...
Test tests/recursion.tah passed.
Test tests/ternary.tah passed.
... (more tests)
Running flag tests...
Test tests/flag/basic.tah passed.
... (more tests)
Tests completed.
```

### Running Tahini Code

Tahini comes with a simple REPL (Read-Eval-Print Loop) to run your code interactively. You can also execute scripts via the command line.

To start the REPL:
```bash
(>_>) java -jar app/build/libs/app.jar
Welcome to Tahini. Type in your code below:
> 3+4;
7
> print "hello there!";
hello there!
> Exiting prompt.
```

To run a Tahini script:
```bash
java -jar app/build/libs/app.jar "../test.tah"
```

## Syntax Overview

### Variables

All variables in Tahini are dynamically typed, and you can declare them using the `var` keyword. Variables can be reassigned, and their scope is determined by the block in which they are declared.

```
var a = "global a";
{
  var a = "local a";
  print a;
}
print a;
var x = 10;
var y = 20;
print x + y;
```

### Functions

```
fun greet(name) {
  print clock();
  print "Hello, " + name + "!";
}
greet("Name");
```

#### Contracts (Preconditions, Postconditions and Assertions)

Inspired by the documentation of Dlang: https://dlang.org/spec/function.html#contracts, as well as the following proposal for C++: http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2016/p0380r1.pdf

For example, the following function calculates the square root of a number using the Newton-Raphson method, with a `precondition` that the input value must be non-negative, and a `postcondition` that helps confirm that the sqrt function produced an acceptable result:
```
fun safeSqrt(value) 
    precondition: value >= 0
    postcondition: x >= 0
{
    var x = value;
    var tolerance = 0.00001; // Define a tolerance level for the approximation
    var difference = x;

    while (difference > tolerance) {
        var newX = 0.5 * (x + value / x);
        difference = x - newX;
        if (difference < 0) {
            difference = -difference;
        }
        x = newX;
    }

    return x;
}
```

If a contract is violated, a runtime error will be thrown. Preconditions are checked before the function is executed, and postconditions are checked after the function body, before it returns.

```bash
Precondition failed.
[line 3] in <fn withdraw>
[line 15]
```

`assertion` works in a similar way, except it can be used anywhere in the function body, and outside of functions as well.

```
var check = false;
assertion: check, "Check should be true!";
```

#### Unit Tests

Inspired by https://ziglang.org/documentation/master/#Zig-Test and https://dlang.org/spec/unittest.html

You can define test blocks — any statement (block, declaration or individual statement) that is prefixed with the `test` keyword. The test block can contain assertions that check the correctness of the code, and will be ignored during normal execution but will be run when the file is executed with the `--test` flag. This allows you to write unit tests for your code directly in the source file.

If a test block fails (i.e., when any statement within it throws a RuntimeError) while running with the test flag, the test block name and the line number of the failing assertion will be printed to the console.

```
// Fibonacci function
fun fib(n) {
  if (n <= 1) return n;
  return fib(n - 2) + fib(n - 1);
}

// Regular code block
var x = 10;
print "Fib(x): " + fib(x);

// Test block to check Fibonacci function
test "checking this out" {
  assertion: fib(0) == 0;
  assertion: fib(1) == 1;
  assertion: fib(2) == 1;
  assertion: fib(3) == 2;
  assertion: fib(4) == 3;
  assertion: fib(5) == 5;
  assertion: fib(6) == 8;
}

// Test block that should fail
test "this should fail" {
  assertion: fib(0) == 0;
  assertion: fib(1) == 1;
  assertion: fib(2) == 1222; // This will fail
  assertion: fib(3) == 2;
}

// Another regular code block
var y = 20;
print "Value of y: " + y;

// Test block to check variable values
test "variable check" {
  assertion: x == 10;
  assertion: y == 20;
  assertion: fib(4) == 3;
}
```

Running the file normally will ignore the test blocks:
```bash
Fib(x): 55
Value of y: 20
```

Running the file with the `--test` flag will execute the test blocks:
```bash
Fib(x): 55
Value of y: 20
Test Results:
PASS (line 12): checking this out
FAIL (line 23): this should fail (assertion contract failed (null))
PASS (line 35): variable check
```

### Conditionals

```
var a = 10;
if (a > 5) {
  print "Greater than 5";
} else {
  print "Less than or equal to 5";
}

// ternary
var b = a > 5 ? "Greater than 5" : "Less than or equal to 5";
```

### Loops

```
for (var i = 0; i < 10; i = i + 1) {
  if (i == 5) break;
  print i;
}

var i = 0;
while (i < 5) {
  print i;
  i = i + 1;
}
```

## Planned Features

Tahini is under active development, and we plan to introduce several new features to enhance its functionality:

1. **Import System** (TODO)
   - Add support for importing external modules and scripts, enabling code modularity and reuse.
   
   Example (tentative):

2. **Standard Library** (TODO)
   - A set of basic utility functions for common operations such as user input, pattern matching, and file handling.
   
   Example (tenative std lib features):

3. **Auto Mocking Functions** (TODO)
   
   Example (tentative):

## Stretch Goals

**Note:** we will do in-depth feasability analysis on these goals, and decide on attempting to accomplish them based on our provided timeline.

Looking ahead, Tahini has ambitious stretch goals that would set it apart as a versatile and flexible language:

1. **Cross-Language Imports** (Stretch Goal)
   - Integrate cross-language importing capabilities, allowing you to import Java `.java` files directly into Tahini code.

2. **In-line and In-built SQL Query Support** (Stretch Goal)
   - Integrate SQL query support directly into the language, enabling easy database queries within the code.

# The Theory Behind This Implementation of Tahini

**Tahini** is implemented as a **tree-walk interpreter** running on the **Java Virtual Machine (JVM)**. This architecture leverages the simplicity of an interpreted language model while taking advantage of the powerful runtime capabilities of the JVM. Here's a theoretical breakdown of this approach:

## What is a Tree-Walk Interpreter?

A **tree-walk interpreter** is a simple form of interpreter that executes programs by directly traversing an abstract syntax tree (AST) after parsing the source code. The AST is a hierarchical representation of the structure of the program, where nodes represent operations, expressions, statements, and other language constructs.

In Tahini's case:
1. **Parsing**: The source code is tokenized and parsed into an AST.
2. **Interpretation**: The interpreter walks the tree recursively, evaluating expressions, executing statements, and manipulating variables as it encounters them.

This approach is straightforward because the AST is used directly without translating the code into an intermediate form or machine-level bytecode.

## Why Tree-Walk on the JVM?

While more complex implementations (such as bytecode interpreters or ahead-of-time compilers) are possible, a tree-walk interpreter has several advantages for a language like Tahini:

1. **Simplicity and Flexibility**: The tree-walk interpreter is easier to implement and modify, making it ideal for rapid development and iteration. New language features (like functions, loops, and conditionals) can be integrated into the AST, and the interpreter can be adapted accordingly.

2. **Development Speed**: By focusing on high-level language features rather than low-level optimizations, the initial development of Tahini can be more focused on usability, syntax, and expressiveness, rather than performance overhead.

3. **Integration with Java**: Since Tahini is written in Java and runs on the JVM, it allows deep integration with existing Java libraries and tooling. Even in a tree-walk interpreter model, Java's mature ecosystem can be leveraged to provide features like I/O, networking, or concurrency, without needing to reinvent those aspects in Tahini.

## Future Evolution

There are, of course, trade-offs to this approach. Tree-walk interpreters can be slower than compiled languages or more sophisticated interpreters, as they reevaluate the AST on each execution. In the future, Tahini could explore more advanced techniques like JIT compilation, bytecode generation, or runtime optimizations to improve performance. For example, we could implement Tahini in C, and translate Tahini code into an efficient Bytecode representation, which can be executed by a custom VM. We would have to go deeper into the implementation of features such as garbage collection, memory management, etc, which are currently handled by Java and the JVM for us.

---

We hope you enjoy using **Tahini**! Stay tuned for more features and updates.
