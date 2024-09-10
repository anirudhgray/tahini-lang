#!/bin/bash

# Directory containing test files
TEST_DIR="tests"

# Path to your built JAR file
JAR_PATH="app/build/libs/app.jar"

# Build the project
echo "Building the project..."
gradle build

# Check if the build was successful
if [ $? -ne 0 ]; then
  echo "Build failed. Exiting."
  exit 1
fi

# Function to run a single test
run_test() {
  local test_file=$1
  local expected_output=$(grep -E '^// ' "$test_file" | sed 's/^\/\/ //')
  
  # Run the JAR file and capture both stdout and stderr
  local actual_output=$(java -jar "$JAR_PATH" "$test_file" 2>&1)

  if [ "$expected_output" == "$actual_output" ]; then
    echo -e "\033[32mTest $test_file passed.\033[0m"
  else
    echo -e "\033[31mTest $test_file failed.\033[0m"
    echo "Expected:"
    echo "$expected_output"
    echo "Actual:"
    echo "$actual_output"
  fi
}

# Run all tests in parallel
echo "Running tests..."
for test_file in $TEST_DIR/*.tah; do
  run_test "$test_file" &
done

# Wait for all tests to complete
wait

# Print summary
echo "Tests completed."

# Exit with appropriate status
exit 0
