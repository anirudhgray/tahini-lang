#!/bin/bash

# Directory containing test files
TEST_DIR="tests"

# Path to your Lox interpreter
INTERPRETER="./gradlew run --args="

# Function to run a single test
run_test() {
  local test_file=$1
  local expected_output=$(grep -E '^// ' "$test_file" | sed 's/^\/\/ //')
  
  # Run the interpreter from the project root directory and capture both stdout and stderr
  local actual_output=$($INTERPRETER"$(pwd)/$test_file" 2>&1 | grep -vE '^(> Task|BUILD SUCCESSFUL|BUILD FAILED|^$|^> Task :app:compileJava|^> Task :app:processResources|^> Task :app:classes|^> Task :app:run|^2 actionable tasks:)')

  # Extract only the relevant lines from the actual output
  actual_output=$(echo "$actual_output" | grep -vE '^(FAILURE:|^\* What went wrong:|^> Process|^\* Try:|^> Run with|^> Get more help at|^Execution failed for task|^BUILD FAILED|^2 actionable tasks:)')

  if [ "$expected_output" == "$actual_output" ]; then
    echo "Test $test_file passed."
  else
    echo "Test $test_file failed."
    echo "Expected:"
    echo "$expected_output"
    echo "Actual:"
    echo "$actual_output"
  fi
}

# Run all tests
for test_file in $TEST_DIR/*.lox; do
  run_test "$test_file"
done
