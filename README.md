# Tahini Language

<img src="./tahini-logo.png" width=100 />

*A most definitely WIP logo for this WIP language.*

> Tahini is a paste made from sesame seeds that can be used as a dip, spread, or dressing. It's versatile, flavorful, and adds a unique touch to many dishes, while being a healthy choice. Inspired by the simplicity, flexibility, and richness of tahini, we present Tahini, a programming language that aims to be a joy to use, with a focus on simplicity, expressiveness, and extensive testing support.

**Tahini** is a lightweight, tree-based interpreted programming language that is written using Java, and which runs on the JVM (Java Virtual Machine), inspired by Lox and Python. It aims to provide simplicity and expressiveness alongside extensive testing and contract support, making it a joy for developers to use. Currently, Tahini supports a number of core language and testing features, with an exciting roadmap of future capabilities.

```
scoop "./kitchen.tah" into kitchen;

fun totalIngredients(ingredientQuantities)
    // contract
    postcondition: total >= 0
{
    var total = 0;
    for (var i = 0; i < len(ingredientQuantities); i = i + 1) {
        total = total + ingredientQuantities[i];
    }
    return total;
}

fun prepareDish() {
    return kitchen::bake(100, kitchen::ovenTemperature);
}

test "totalIngredients test" {
    // Test case: summing 3 ingredients
    assertion: totalIngredients([1, 2, 3]) == 6, "Should be 6!";
    // Test case: summing 0 ingredients
    assertion: totalIngredients([]) == 0, "Should be 0!";
}

var flour = 2;
var sugar = 1;
var eggs = 3;

var ingredientsList = [flour, sugar, eggs];
print "Total ingredients needed: " + totalIngredients(ingredientsList);
print prepareDish();
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
    - [Arrays](#arrays)
    - [Maps](#maps)
    - [Conditionals](#conditionals)
    - [Loops](#loops)
    - [Imports](#imports)
      - [Flat Imports](#flat-imports)
      - [Namespaced Imports](#namespaced-imports)
    - [Built-in Functions](#built-in-functions)
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
- [x] **Functions**: First class citizens of Tahini. Define and call reusable blocks of code, with support for contracts (`precondition`, `postcondition`, and `assertion`).
- [ ] **Classes**: Object-oriented features to group variables and methods (halted in favour of a lean towards a functional paradigm).
- [x] **Advanced Data Structures**: Basic support for lists, maps, and other data structures.
- [ ] **Error Handling**: Support for user-defined exceptions and error handling (in progress).
- [x] **Stack Traces**: Detailed error messages with line numbers and function names.
- [x] **Unit Tests**: Write test blocks directly in the source file to validate code correctness.
- [ ] **Import System**: Import other Tahini files to reuse code and create modular applications (in progress).
  
Planned features include a standard library and cross-language support.

## Getting Started

### Quick Start — Using Prebuilt Binaries

You can download the latest prebuilt binaries for your operating system from the [Releases](https://github.com/anirudhgray/tahini-lang/releases) page. Follow these steps to get started quickly.

1. Download the Latest Binary for your OS: [Latest Release](https://github.com/anirudhgray/tahini-lang/releases/latest)

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

1. Run Tahini Code (execute a Tahini script from file)
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

> [!WARNING]
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

> [!TIP]
> Check out the [VSCode extension for Tahini](https://github.com/anirudhgray/tahini-vscode) for a more integrated development experience.

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
var name = input();
print name;
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

### Arrays

Arrays are implemented as an ArrayList. You can create an array via `[...]` syntax, and access elements using the `[]` operator. Arrays can contain any object values, including functions (since functions are first-class citizens in Tahini), and can be sliced and concatenated.

```
var arr = [1, 2, "string", fib, 5];
print arr[0]; // 1
varr arr2 = arr[1:3];
print arr2; // [2, "string"]
```

You can write basic basic append and remove function to manipulate arrays (utilising the inbuilt `len` function):

```
fun append(arr, value) {
  return arr + [value];
}

fun remove(arr, index) {
  return arr[0:index] + arr[index+1:len(arr)];
}
```

### Maps

Maps are implemented as a HashMap. You can create a map via `{...}` syntax, and access elements using the `[]` operator. Maps can contain any object keys or values.

```
var map = {"key": "value", 1: 2, "fib": fib};
print map["key"]; // value
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

### Imports

Tahini supports importing other Tahini files to reuse code and create modular applications. You can import a file using the `scoop` keyword, followed by the path to the file. The imported file will be executed in the current scope, allowing you to access its variables and functions.

#### Flat Imports

```tahini
scoop "../kitchen.tah";

function_from_kitchen();
```

The above would do a **flat import** of the `kitchen.tah` file, executing it in the current scope, and making every variable and function in `kitchen.tah` available in the current file's global scope without any prefix. This should be used with caution, as it can lead to naming conflicts, pollution and unintended side effects.

#### Namespaced Imports

To avoid polluting the global environment, it is recommended to use **namespaced imports**.

```tahini
scoop "../kitchen.tah" into kitchen;

kitchen::function_from_kitchen();
```

With this, all functions and variables from kitchen.tah are accessible only through the kitchen namespace. If `kitchen.tah` defines a function `prepare()`, you would now call it as `kitchen::prepare()` in your current file.

Tahini also allows **nested imports**, so if a file you import also imports other files, they will follow the same flat or namespaced rules. For example:

```tahini
// C.tah
fun function_from_c() {...}

// D.tah
fun function_from_d() {...}

// B.tah 
scoop "C.tah";
scoop "D.tah" into D;
fun function_from_b() {...}

// A.tah
scoop "B.tah" into B;
B::function_from_b();
B::function_from_c();
B::D::function_from_d();
```

See [tests/namescoop](./tahini/tests/namescoop1.tah) for an example of how imports work.

### Built-in Functions

Currently, Tahini does not have a standard library. However, it does provide a set of built-in functions (filling some of the core gaps which an imported standard library would have provided) for common operations:

- `input()` - Read a line of string input from the user.
- `clock()` - Get the current time in seconds since the Unix epoch.
- `len(arr)` - Get the length of an array.

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

> [!TIP]
> Note: you can see the AST for a Tahini program by running the `--visualize` flag with the Tahini interpreter.

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
