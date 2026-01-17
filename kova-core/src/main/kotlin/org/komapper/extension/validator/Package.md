# Kova - org.komapper.extension.validator

A type-safe Kotlin validation library with context-based validators.

## Overview

This package provides a declarative validation DSL that leverages Kotlin's context parameters
to create fluent, composable validation rules. Validators are extension functions that chain
together naturally while automatically tracking validation paths and accumulating errors.

## Entry Points

- `tryValidate` - Executes validation and returns `ValidationResult` (Success or Failure)
- `validate` - Executes validation and returns the value or throws `ValidationException`

## Core Components

### Context and Configuration
- `Validation` - Context object tracking validation state (root, path, config, accumulator)
- `ValidationConfig` - Configuration settings (failFast, clock, logger)

### Results
- `ValidationResult` - Public API result type (Success/Failure)
- `ValidationIor` - Internal inclusive-or type for partial success handling

### Schema Validation
- `Schema` - DSL for property-based object validation
- `schema` - Entry point for schema validation using reflection

### Constraints
- `Constraint` - Context for defining validation rules with `Constraint.satisfies`
- `constrain` - Adds custom constraints to values
- `transformOrRaise` - Transforms values with immediate failure on error

### Path Tracking
- `Path` - Linked list tracking validation path through object graph
- `addPath`, `addPathChecked` - Path management with circular reference detection

### Error Handling
- `Accumulate` - Error accumulation strategy (collect all vs fail-fast)
- `Message` - Validation error messages (Text or Resource-based)
- `text`, `resource` - Message creation functions
- `withMessage` - Wraps validation block errors in a custom message

### Logging
- `LogEntry` - Debug log entries (Satisfied/Violated)
- `log` - Lazy logging function

## Validators

Built-in validators are organized by type:
- `AnyValidator.kt` - Universal validators (ensureEquals, ensureNotEquals, ensureSameAs)
- `NullableValidator.kt` - Null handling (ensureNull, ensureNotNull, ensureNullOr)
- `BooleanValidator.kt` - Boolean validators (ensureTrue, ensureFalse)
- `ComparableValidator.kt` - Comparison validators (ensureAtLeast, ensureAtMost, ensureInRange)
- `CharSequenceValidator.kt` - Text validators (ensureNotBlank, ensureLengthAtLeast, ensureMatches)
- `StringValidator.kt` - String transformers (transformToInt, transformToLocalDate, etc.)
- `NumberValidator.kt` - Numeric validators (ensurePositive, ensureNegative)
- `IterableValidator.kt` - Iterable validators (ensureEach, ensureContains)
- `CollectionValidator.kt` - Collection validators (ensureNotEmpty, ensureSizeAtLeast)
- `MapValidator.kt` - Map validators (ensureContainsKey, ensureEachEntry)
- `TemporalValidator.kt` - Date/time validators (ensureInPast, ensureInFuture)

## Object Creation

- `capture` - Property delegate for capturing validated values during object construction

## Example Usage

```kotlin
// Simple validation
val result = tryValidate {
    name.ensureNotBlank().ensureLengthInRange(1..100)
}

// Schema validation
context(_: Validation)
fun User.validate() = schema {
    ::name { it.ensureNotBlank().ensureLengthInRange(1..100) }
    ::age { it.ensureInRange(0..120) }
}

// Object creation with capture
context(_: Validation)
fun buildUser(rawName: String, rawAge: String): User {
    val name by capture { rawName.ensureNotBlank() }
    val age by capture { rawAge.transformToInt() }
    return User(name, age)
}
```
