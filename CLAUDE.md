# CLAUDE.md

This file provides guidance to Claude (claude.ai/code) when working with code in this repository.

## Build & Test Commands

No Gradle wrapper is checked in — use system `gradle`.

```bash
# Build everything
gradle build

# Run all tests
gradle test

# Run tests for a specific module
gradle :core-analysis:test

# Run a single test class
gradle :core-analysis:test --tests "com.photomate.SomeTest"

# Run a single test method
gradle :core-analysis:test --tests "com.photomate.SomeTest.someMethod"

# Clean build
gradle clean build
```

## Architecture

**photomate** is a Kotlin/JVM multi-module Gradle project (Kotlin 2.0.21, JVM 17).

### Modules

- **`core-analysis`** — the main module. Sources go in `core-analysis/src/main/kotlin/`, tests in `core-analysis/src/test/kotlin/`. Uses package `com.photomate`.

### Dependencies

- **Testing**: JUnit 5 (`junit-jupiter:5.11.3`) with Google Truth (`truth:1.4.4`) for assertions.

## License

MPL 2.0 — all source files should include the MPL header notice.
