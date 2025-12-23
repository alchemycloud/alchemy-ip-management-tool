# Contributing to Alchemy IP Management Tool

Thank you for your interest in contributing to Alchemy IP Management Tool! This document provides guidelines and information for contributors.

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for everyone.

## How to Contribute

### Reporting Bugs

Before creating a bug report, please check existing issues to avoid duplicates. When creating a bug report, include:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Your environment (Java version, Spring Boot version, database, etc.)
- Relevant logs or error messages

### Suggesting Features

Feature requests are welcome! Please provide:

- A clear description of the feature
- The problem it solves or use case it addresses
- Any implementation ideas you have

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the coding style** (see below)
3. **Add tests** for any new functionality
4. **Update documentation** as needed
5. **Ensure all tests pass** before submitting

## Development Setup

### Prerequisites

- JDK 21 or later
- Maven 3.9+
- Your favorite IDE (IntelliJ IDEA recommended)

### Building the Project

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/alchemy-ip-management-tool.git
cd alchemy-ip-management-tool

# Build the project
./mvnw clean install

# Run tests
./mvnw test
```

### Running Tests

```bash
# Run all tests (unit + integration)
./mvnw test

# Run specific test class
./mvnw test -Dtest=DefaultIpAddressExtractorTest

# Run only integration tests
./mvnw test -Dtest=IpAddressStorageIntegrationTest

# Run with coverage
./mvnw test jacoco:report
```

The project includes:
- **Unit tests**: Test individual components in isolation
- **Integration tests**: Test the full Spring Boot context with a real embedded server and H2 database

## Coding Style

### General Guidelines

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep methods focused and concise
- Write self-documenting code where possible

### Java Style

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use `final` for immutable variables
- Prefer constructor injection over field injection
- Use `Optional` instead of null for optional values

### Documentation

- Add Javadoc for all public classes and methods
- Include `@param`, `@return`, and `@throws` tags
- Document non-obvious implementation details

### Example

```java
/**
 * Extracts the client IP address from the given HTTP request.
 *
 * <p>This method checks multiple headers commonly used by reverse proxies
 * and CDNs to forward the original client IP address.
 *
 * @param request the HTTP servlet request
 * @return an {@link Optional} containing the extracted IP address,
 *         or {@link Optional#empty()} if the IP cannot be determined
 * @throws IllegalArgumentException if request is null
 */
@Override
public Optional<String> extractIpAddress(HttpServletRequest request) {
    if (request == null) {
        throw new IllegalArgumentException("Request cannot be null");
    }
    // Implementation
}
```

## Testing Guidelines

- Write unit tests for all new functionality
- Use descriptive test method names
- Follow the Arrange-Act-Assert pattern
- Mock external dependencies
- Test edge cases and error conditions

### Test Naming Convention

```java
@Test
void shouldExtractIpFromXForwardedForHeader() { }

@Test
void shouldReturnEmptyWhenRequestIsNull() { }

@Test
void shouldHandleMultipleIpsInXForwardedFor() { }
```

## Commit Messages

Follow conventional commit format:

```
type(scope): subject

body (optional)

footer (optional)
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Examples:
```
feat(extractor): add support for Fastly CDN header

fix(schema): handle table existence check for uppercase names

docs(readme): add configuration examples
```

## Pull Request Process

1. Update the README.md with details of changes if applicable
2. Update the CHANGELOG.md with a note describing your changes
3. Ensure your code passes all tests and linting
4. Request review from maintainers
5. Address any feedback from code review

## Release Process

Releases are handled by maintainers. The process involves:

1. Updating version numbers
2. Updating CHANGELOG.md
3. Creating a release tag
4. Publishing to Maven Central

## Questions?

If you have questions, feel free to:

- Open a GitHub issue
- Start a discussion in GitHub Discussions

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
