# Contributing to TailwindFX

Thank you for your interest in contributing to TailwindFX! This document provides guidelines and instructions for contributing.

## Code of Conduct

Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before participating in this project.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* Use a clear and descriptive title
* Describe the exact steps to reproduce the problem
* Provide specific examples to demonstrate the steps
* Describe the behavior you observed and what behavior you expected
* Include screenshots if possible
* Include Java version, JavaFX version, and OS information

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* Use a clear and descriptive title
* Provide a detailed description of the suggested enhancement
* Explain why this enhancement would be useful
* List some examples of how this enhancement would be used

### Your First Code Contribution

Unsure where to begin contributing? You can start by looking at issues labeled:
* `good first issue` - These contain only a few lines of code and are ideal for first-time contributors
* `help wanted` - These need extra attention and help from contributors

### Pull Requests

1. Fork the repository
2. Create a new branch from `develop` (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests to ensure everything works
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to your branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request against the `develop` branch

## Development Setup

### Prerequisites

* Java 21 or higher
* Maven 3.6+
* Git

### Setting Up the Project

```bash
# Clone the repository
git clone https://github.com/yasmramos/TailwindFX.git
cd TailwindFX

# Build the project
mvn clean install

# Run tests
mvn test
```

### Running Examples

```bash
# Build the project first
mvn clean install

# Run example applications from the examples module
cd tailwindfx-examples
mvn javafx:run -Djavafx.mainClass=tailwindfx.examples.DashboardApp
```

For more examples, check the `tailwindfx-examples/src/main/java/tailwindfx/examples/` directory.

## Coding Guidelines

### Java Code Style

* Follow Oracle's Java Code Conventions
* Use meaningful variable and method names
* Add Javadoc comments for public APIs
* Keep methods small and focused
* Write unit tests for new functionality

### CSS Guidelines

* Follow BEM naming conventions where applicable
* Keep utilities atomic and single-purpose
* Document new utilities with comments
* Test utilities across different JavaFX controls

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
feat: add new grid layout utilities
fix: resolve NPE in FxFlexPane when wrap=true
docs: update README with new examples
test: add unit tests for ColorPalette
refactor: simplify JitCompiler cache logic
```

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FxFlexPaneTest

# Run tests with coverage
mvn test jacoco:report
```

### Writing Tests

* Write unit tests for new functionality
* Aim for at least 80% code coverage
* Test edge cases and error conditions
* Use descriptive test method names

## Documentation

* Update README.md for user-facing changes
* Add Javadoc comments for public APIs
* Update CHANGELOG.md with new features
* Include code examples in documentation

## Release Process

Releases follow semantic versioning (MAJOR.MINOR.PATCH):

1. Update version numbers in pom.xml
2. Update CHANGELOG.md with release date
3. Create a git tag
4. Build and publish to Maven Central

## Questions?

Feel free to open an issue for any questions or join our community discussions.

Thank you for contributing to TailwindFX! 🎉
