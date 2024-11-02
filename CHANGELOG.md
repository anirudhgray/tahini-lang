# Changelog

## [Unreleased]

### Added
- Concurrency features
- Database support and SQL queries
- Cross-language import of Java classes
- Mocking features
- Run file by disabling/enabling contracts

### Changed
- Enhanced HTTP Request/Response support for methods (POST, PUT, DELETE, etc.)
- Critical (runtimeError) vs warning (stderr) assertions

---

## [0.4.0] - 2024-11-01

### Added
- `typeOf` function to determine the type of a variable.

### Fixed
- Add custom error messages for failing contracts (like for assertions).

## [0.3.2] - 2024-10-31

### Fixed
- Enabled URL protocols for native image support
- Resolved null pointer bug in AST visualization

## [0.3.1] - 2024-10-31

### Added
- Introduced `time`, `random`, and `http` modules in Larder library

## [0.3.0-beta.1] - 2024-10-31

### Added
- Added the `--visualize` flag to visualize the AST of the parsed program file.
- Implemented the `larder` standard library internals.
- Modulo (`%`) operator support for integers and floats.
- Implemented the `larder/collections`, `larder/string` and `larder/io` stdlib modules.

### Fixed
- Workflow adjustments to ensure resource inclusion in builds
- Handle source file not found error in Tahini itself.
- Include larder resources in the native image build.

## [0.2.1] - 2024-10-29

### Added
- Namespaced import functionality

### Fixed
- Addressed error handling and improved documentation for namespaced imports

## [0.2.0] - 2024-10-28

### Added
- Introduced namespace and import functionality
- Introduced "scoop" functionality to directly flat import all environment variables

### Fixed
- Circular import detection
- Improved error reporting for scanner and parser across multiple files

## [0.1.4] - 2024-10-27

### Added
- Implemented hashmap functionality, allowing for map expression and value access
- Extended built-ins with `len` function and slicing for strings

## [0.1.3] - 2024-10-27

### Added
- Introduced array functionalities including length retrieval, concatenation, slicing, and element access with `[]` operator

### Fixed
- Improved error reporting for built-in functions

## [0.0.3-beta.2] - 2024-10-26

### Changed
- Release workflow enhancements to support beta versions and note updates

## [0.0.3-beta.1] - 2024-10-26

### Added
- Parsing and interpreting list variables

### Changed
- Documentation for user inputs and other built-in functions

## [0.0.2] - 2024-10-25

### Added
- Mock standard library support for input handling

### Changed
- Updated testing framework to handle user inputs

## [0.0.1] - 2024-09-30

### Added
- Initialised core interpreter components: scanner, parser and interpreter
- Implemented core language features: variables, expressions, statements (loops, conditionals, functions, logic), error reporting
- Introduced assertions, function contracts, test block support, and test mode
- Initial build setup and release workflow
