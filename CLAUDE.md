# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Kova** is a type-safe Kotlin validation library that provides composable validators through a fluent API. It uses Kotlin's type system and reflection to enable validation with detailed error reporting. The library supports both simple value validation and complex object validation with nested property tracking.

## Build and Test Commands

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew kova-core:test

# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Run a specific test class (Kotest)
./gradlew test --tests "org.komapper.extension.validator.StringValidatorTest"

# Check code formatting (fails if code is not properly formatted)
./gradlew spotlessCheck

# Auto-format code (also runs automatically during build)
./gradlew spotlessApply
```

**Test Framework**: Kotest (with JUnit Platform runner)
**Build Tool**: Gradle 8.14 with Kotlin DSL
**JVM Target**: Java 17
**Code Formatter**: Spotless with ktlint (automatically runs during build)

## Code Formatting

The project uses **Spotless** with **ktlint** for automatic code formatting.

- Code formatting is **automatically applied** during the build process (before compilation)
- All Kotlin source files (`**/*.kt`) and Gradle Kotlin DSL files (`**/*.gradle.kts`) are formatted
- Configuration is in `build.gradle.kts:24-41`

## Core Architecture

Kova's architecture is built around **composable validators** that implement the `Validator<IN, OUT>` interface. The library supports two main usage patterns:

### Simple Validation Pattern

```kotlin
val validator = Kova.string().min(1).max(10)
val result = validator.tryValidate("hello")  // Returns ValidationResult
```

### Object Validation Pattern

```kotlin
val validator = Kova.validator {
    User::class {
        User::name { Kova.string().min(1).max(10) }
        User::age { Kova.int().min(0) }
    }
}
val result = validator.tryValidate(user)
```

### Object Factory Pattern

```kotlin
val factory = Kova.factory {
    ::Person {
        args(Kova.string().min(1), Kova.int().min(0))
    }
}
val person = factory.create("Alice", 30)  // Validates and constructs
```

### Key Components

- **Validator<IN, OUT>**: Core interface with `tryValidate()` method
- **ValidationResult**: Sealed interface with `Success<T>` and `Failure` cases
- **CoreValidator**: Generic constraint evaluator used internally by all type-specific validators
- **Type-Specific Validators**: CharSequenceValidator, NumberValidator, ComparableValidator, CollectionValidator, MapValidator, EnumValidator
- **ObjectValidator**: Validates objects by validating individual properties
- **ObjectFactory**: Constructs objects from validated inputs via reflection
- **NullableValidator**: Wraps validators to handle nullable types
- **ConditionalValidator**: Supports conditional validation logic

### Validator Composition

Validators are immutable and can be composed using operators:

```kotlin
// Using + operator (same as and)
val validator = Kova.string().min(1).max(10) + Kova.string().isNotBlank()

// Using and infix function
val validator = Kova.string().min(1) and Kova.string().max(10)

// Using or infix function (succeeds if either validator passes)
val validator = Kova.string().isBlank() or Kova.string().min(5)

// Transform output with map
val intValidator = Kova.string().isInt().map { it.toString().toInt() }

// Chain validators with andThen/compose
val validator = stringValidator.andThen(intValidator)

// Add constraints to existing validators
val validator = Kova.string().constraint { ctx -> /* ... */ }
```

### Nullable Handling

Kova provides first-class support for nullable types through the `Kova.nullable()` factory method and specialized nullable validators.

```kotlin
// Create a nullable validator by wrapping a non-null validator
val nullableValidator = Kova.nullable(Kova.string().min(1))
// Accepts null and passes validation (null is always valid in nullable validators)
// Non-null values are validated with the wrapped validator

// Create a nullable validator without wrapping (for custom validation)
val emptyNullableValidator = Kova.nullable<String>()

// Require non-null value
val notNullValidator = Kova.nullable<String>().isNotNull()
// Null values fail validation, non-null values pass

// Accept null OR validate non-null values
val nullOrMinValidator = Kova.nullable<String>().isNullOr(Kova.string().min(5))
// Null values pass, non-null values must satisfy min(5)

// Require non-null AND validate the value
val notNullAndMinValidator = Kova.nullable<String>().isNotNullAnd(Kova.string().min(5))
// Equivalent to: isNotNull() + wrapped validator
// Null values fail, non-null values must satisfy min(5)
```

**Key behavior**: By default, `Kova.nullable()` treats null as a valid value. Use `isNotNull()` or `isNotNullAnd()` to enforce non-null requirements.

**Implementation note**: The `asNullable()` extension method is also available on any validator as an alternative API for converting a validator to nullable.

### ValidationResult Algebra

The `+` operator on `ValidationResult` enables error accumulation:
- `Success + Success` → second Success value
- `Success + Failure` → Failure
- `Failure + Failure` → merged Failure (all errors combined)

This allows collecting multiple validation errors in a single pass.

### Path Tracking

Validation failures include `root` and `path` properties in `ValidationContext` (e.g., root: `"User"`, path: `"name"`). The context builds these paths during nested validation, enabling precise error location reporting.

### Internationalization

Constraints return `Message` objects for error messages:
- **Message.Resource(key, args)**: I18n keys resolved from `kova-core/src/main/resources/kova.properties`
- **Message.Text(content)**: Direct string messages
- **Message.ValidationFailure(details)**: Nested validation failures

Resource messages use Java MessageFormat with placeholders: `{0}`, `{1}`, etc.

Example: `kova.charSequence.min="{0}" must be at least {1} characters`

The `Message.resource0`, `Message.resource1`, `Message.resource2`, etc. companion functions create message factories that automatically include the input value as the first argument.

## Important Patterns

### Immutability
All validators are immutable. Methods like `min()`, `max()` return new instances with updated constraint lists. This enables safe sharing and composition.

### Reflection Usage
Heavy use of Kotlin reflection (`KClass`, `KProperty1`, `KParameter`, `KFunction`) for:
- Property binding via property references (`User::name`)
- Constructor parameter resolution by name
- Type-safe object construction

### Error Handling
- Constructor invocation errors are caught and wrapped in `FailureDetail` with cause
- `InvocationTargetException` from init blocks are captured
- Missing constructor arguments generate descriptive error messages

### Delegation Pattern

Type-specific validators use class delegation to `CoreValidator`:

```kotlin
class CharSequenceValidator<T : CharSequence>(
    private val delegate: CoreValidator<T, T> = CoreValidator(transform = { it })
) : Validator<T, T> by delegate
```

This provides core validation logic while allowing type-specific extension methods. When adding constraints, a new instance with updated constraints is returned.

### Constraint Evaluation

Constraints are evaluated using the `Constraint` functional interface:

```kotlin
fun interface Constraint<T> {
    fun apply(context: ConstraintContext<T>): ConstraintResult
}
```

The `Constraint.satisfies()` helper simplifies constraint creation:

```kotlin
Constraint.satisfies(condition, message)
```

Returns `ConstraintResult.Satisfied` if `condition` is true, otherwise `ConstraintResult.Violated(message)`.

**Note**: The old `Constraint.check()` function is deprecated in favor of `satisfies()` for better clarity.

### ValidationContext and Fail-Fast

`ValidationContext` tracks validation state including:
- `root`: The root object type being validated
- `path`: The current property path
- `failFast`: Whether to stop on first error (default: false)

When `failFast` is true, validation stops at the first constraint violation.

## Module Structure

- **kova-core**: Core validation library (main module)
- **example**: Example application demonstrating Kova usage patterns

## Key Files

**Entry Point**:
- `Kova.kt` - Main API and factory methods for creating validators

**Core Interfaces**:
- `Validator.kt` - Core `Validator<IN, OUT>` interface and composition operators
- `Constraint.kt` - `Constraint<T>` interface and `ConstraintContext`/`ConstraintResult` types
- `ValidationResult.kt` - Sealed `ValidationResult` with `Success`/`Failure` cases
- `ValidationContext.kt` - Tracks validation state (root, path, failFast)
- `ValidationException.kt` - Exception thrown by `validate()` extension function

**Core Validators**:
- `CoreValidator.kt` - Generic constraint evaluator used by all type-specific validators
- `CharSequenceValidator.kt` - Validates strings/char sequences (min/max length, patterns, etc.)
- `NumberValidator.kt` - Validates numeric types (Int, Long, Double, Float, BigDecimal, etc.)
- `ComparableValidator.kt` - Validates comparable types (min/max comparisons)
- `CollectionValidator.kt` - Validates collections (size, element validators)
- `MapValidator.kt` - Validates maps (size, key/value validators)
- `MapEntryValidator.kt` - Validates individual map entries
- `EnumValidator.kt` - Validates enum values
- `GenericValidator.kt` - Generic validator with transformation support

**Special Validators**:
- `ObjectValidator.kt` - Validates objects by validating individual properties with DSL
- `NullableValidator.kt` - Wraps validators to handle nullable types
- `ConditionalValidator.kt` - Supports conditional validation logic

**Object Construction**:
- `ObjectFactory.kt` - Factory classes that validate inputs and construct objects via reflection

**Supporting Types**:
- `Message.kt` - Error message abstraction (Text, Resource, ValidationFailure)
- `Constraints.kt` - Common constraint implementations

## Current Naming Conventions

- `ValidationResult.Success/Failure` (not Ok/Error) for result types
- `Message` (not MessageSource) for error message abstraction
- `ConstraintResult.Satisfied/Violated` for constraint evaluation results
- Method parameter `message` when accepting message factory functions