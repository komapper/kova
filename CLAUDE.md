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
import org.komapper.extension.validator.Kova

val validator = Kova.string().min(1).max(10)
val result = validator.tryValidate("hello")  // Returns ValidationResult
```

### Object Validation Pattern

```kotlin
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema

data class User(val name: String, val age: Int)

object UserSchema : ObjectSchema<User>() {
    val name = User::name { Kova.string().min(1).max(10) }
    val age = User::age { Kova.int().min(0) }
}

val result = UserSchema.tryValidate(user)
```

**Important**: Properties are now defined as object properties (outside the constructor lambda). This allows them to be referenced when creating ObjectFactory instances. Property definitions use the `invoke` operator on `KProperty1` to register validators with the schema.

### Object-Level Constraints

You can add constraints that validate the entire object using the `constrain` method within the constructor lambda:

```kotlin
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectSchema
import java.time.LocalDate

data class Period(val startDate: LocalDate, val endDate: LocalDate)

object PeriodSchema : ObjectSchema<Period>({
    Period::startDate { Kova.localDate() }
    Period::endDate { Kova.localDate() }

    constrain("dateRange") {
        satisfies(
            it.input.startDate <= it.input.endDate,
            "startDate must be less than or equal to endDate"
        )
    }
})
```

**Note**: When you need object-level constraints, properties are defined within the constructor lambda. The lambda provides access to `ObjectSchemaScope` which includes the `constrain()` method. The `constrain` method takes a constraint key and a lambda that receives a `ConstraintContext<T>` and returns a `ConstraintResult`. Use `satisfies()` helper to simplify constraint creation.

### Object Factory Pattern

The ObjectFactory pattern allows you to validate inputs and construct objects in a single operation. This is useful for creating validated domain objects from raw inputs.

```kotlin
import org.komapper.extension.validator.Kova
import org.komapper.extension.validator.ObjectFactory
import org.komapper.extension.validator.ObjectSchema

data class Person(val name: String, val age: Int)

// Define properties as object properties (not in constructor lambda) when using ObjectFactory
object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1).max(50) }
    private val age = Person::age { Kova.int().min(0).max(150) }

    // Create a factory method that builds an ObjectFactory
    fun build(name: String, age: Int): ObjectFactory<Person> {
        val arg1 = Kova.arg(this.name, name)
        val arg2 = Kova.arg(this.age, age)
        return Kova.arguments(arg1, arg2).createFactory(PersonSchema, ::Person)
    }
}

// Use the factory to validate and construct
val factory = PersonSchema.build("Alice", 30)
val result = factory.tryCreate()  // Returns ValidationResult<Person>
// or
val person = factory.create()     // Returns Person or throws ValidationException
```

**Key Components**:
- **`Kova.arg(validator, value)`**: Creates an `Arg` that wraps a validator and input value
- **`Kova.arg(validator, factory)`**: Creates an `Arg` that wraps a validator and nested ObjectFactory (for nested objects)
- **`Kova.arguments(...)`**: Creates an `Arguments1` through `Arguments10` object (supports 1-10 arguments)
- **`.createFactory(schema, constructor)`**: Creates an `ObjectFactory` that validates inputs and constructs objects
- **`ObjectFactory.tryCreate(failFast = false)`**: Validates and constructs, returning `ValidationResult<T>`
- **`ObjectFactory.create(failFast = false)`**: Validates and constructs, returning `T` or throwing `ValidationException`

**Nested Object Validation**:
```kotlin
data class Age(val value: Int)
data class Person(val name: String, val age: Age)

object AgeSchema : ObjectSchema<Age>() {
    private val value = Age::value { Kova.int().min(0).max(120) }

    fun build(age: String): ObjectFactory<Age> {
        val arg1 = Kova.arg(Kova.string().toInt().then(this.value), age)
        return Kova.arguments(arg1).createFactory(AgeSchema, ::Age)
    }
}

object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1) }
    private val age = Person::age { AgeSchema }

    fun build(name: String, age: String): ObjectFactory<Person> {
        val arg1 = Kova.arg(this.name, name)
        val arg2 = Kova.arg(this.age, AgeSchema.build(age))  // Nested factory
        return Kova.arguments(arg1, arg2).createFactory(PersonSchema, ::Person)
    }
}
```

**Note**: Properties must be defined as object properties (not within the constructor lambda) so they can be referenced when creating `Arg` instances. The `createFactory()` method accepts the schema itself as the first parameter for validating the constructed object.

### Key Components

- **Validator<IN, OUT>**: Core interface with `execute(context, input)` method; public API uses `tryValidate()` and `validate()` extension functions
- **ValidationResult**: Sealed interface with `Success<T>` and `Failure` cases
- **ConstraintValidator**: Generic constraint evaluator used internally by all type-specific validators
- **Type-Specific Validators**: StringValidator, NumberValidator, LocalDateValidator, ComparableValidator, CollectionValidator (with min/max/length/notEmpty/onEach), MapValidator (with min/max/length/notEmpty/onEach/onEachKey/onEachValue), MapEntryValidator, LiteralValidator
- **ObjectSchema**: Validates objects by defining validation rules for individual properties as object properties or within a constructor lambda scope (when using object-level constraints)
- **ObjectSchemaScope**: Scope class providing access to `constrain()` method within ObjectSchema constructor lambda for object-level constraints
- **ObjectFactory**: Constructs objects from validated inputs (supports 1-10 arguments via Arguments1-Arguments10)
- **Arg**: Sealed interface wrapping either a validator with value (`Arg.Value`) or a validator with nested factory (`Arg.Factory`)
- **Arguments1-Arguments10**: Data classes that hold 1-10 `Arg` instances and provide `createFactory()` method
- **NullableValidator**: Wraps validators to handle nullable types
- **ConditionalValidator**: Supports conditional validation logic
- **EmptyValidator**: No-op validator that always succeeds, used by `Kova.generic()`

### Validator Composition

Validators are immutable and can be composed using operators:

```kotlin
import org.komapper.extension.validator.Kova

// Using + operator (same as and)
val validator = Kova.string().min(1).max(10) + Kova.string().notBlank()

// Using and infix function
val validator = Kova.string().min(1) and Kova.string().max(10)

// Using or infix function (succeeds if either validator passes)
val validator = Kova.string().isBlank() or Kova.string().min(5)

// Transform output with map
val intValidator = Kova.string().isInt().map { it.toString().toInt() }

// Chain validators with andThen/compose
val validator = stringValidator.andThen(intValidator)

// Add custom constraints to existing validators
val validator = Kova.string().constrain("custom.id") { ctx -> /* ... */ }
```

**Execution Model**: Validators implement the `execute(context, input)` method which handles the validation logic. The public API uses extension functions `tryValidate(input, failFast)` and `validate(input, failFast)` which create a `ValidationContext` and call `execute()` internally.

### Nullable Handling

Kova provides first-class support for nullable types through the `Kova.nullable()` factory method and specialized nullable validators.

```kotlin
// Create a nullable validator by wrapping a non-null validator using asNullable()
val nullableValidator = Kova.string().min(1).asNullable()
// Accepts null and passes validation (null is always valid in nullable validators)
// Non-null values are validated with the wrapped validator

// Create an empty nullable validator (for custom validation)
val emptyNullableValidator = Kova.nullable<String>()

// Require non-null value - use Kova.notNull() directly
val notNullValidator = Kova.notNull<String>()
// Null values fail validation, non-null values pass

// Accept null explicitly - use Kova.isNull() directly
val isNullValidator = Kova.isNull<String>()
// Only null values pass, non-null values fail

// Accept null OR validate non-null values
val nullOrMinValidator = Kova.nullable<String>().isNullOr(Kova.string().min(5))
// Null values pass, non-null values must satisfy min(5)

// Alternative: Use isNullOr for literal values
val nullOrValueValidator = Kova.isNullOr("default")
// Accepts null or the exact value "default"

// Require non-null AND validate the value - use Kova.notNullThen() directly
val notNullAndMinValidator = Kova.notNullThen(Kova.string().min(5)).toNonNullable()
// Null values fail, non-null values must satisfy min(5)

// Chain nullable validators with notNullThen
val chainedValidator = Kova.nullable<String>().notNullThen(Kova.string().min(5))
// Null values fail, non-null values must satisfy min(5)

// Set default value for null inputs
val withDefault = Kova.nullable<String>().orDefault("default")
// Null inputs are replaced with "default"
```

**Key behavior**: By default, `Kova.nullable()` treats null as a valid value. Use `Kova.notNull()` or `.notNull()` to enforce non-null requirements.

**API Summary**:
- `Kova.notNull<T>()` - Convenience method, same as `Kova.nullable<T>().notNull()`
- `Kova.isNull<T>()` - Convenience method, same as `Kova.nullable<T>().isNull()`
- `Kova.isNullOr(value)` - Convenience method, accepts null or a specific literal value
- `Kova.notNullThen(validator)` - Convenience method for chaining with non-null validation
- `.notNullThen(validator)` - Extension on nullable validators for chaining
- `.orDefault(value)` - Replace null values with a default

**Implementation note**:
- The `asNullable()` extension method is also available on any validator as an alternative API for converting a validator to nullable
- `Kova.nullable()` internally uses `Kova.generic()` which creates an `EmptyValidator` that always succeeds
- `notNull()` returns a regular `Validator<T?, S>` that enforces non-null constraints via `toNonNullable()` internally

### ValidationResult Algebra

The `+` operator on `ValidationResult` enables error accumulation:
- `Success + Success` → second Success value
- `Success + Failure` → Failure
- `Failure + Failure` → merged Failure (all errors combined)

This allows collecting multiple validation errors in a single pass.

### Path Tracking

Validation failures include `root` and `path` properties in `ValidationContext` (e.g., root: `"User"`, path: `"name"`). The context builds these paths during nested validation, enabling precise error location reporting.

### Internationalization

Constraints return `Message` objects for error messages. All `Message` types have a `content` property that contains the actual string message:
- **Message.Resource(constraintId, args)**: I18n messages resolved from `kova-core/src/main/resources/kova.properties`. The `content` property contains the formatted message.
- **Message.Text(content)**: Direct string messages. The `content` property contains the provided string.
- **Message.ValidationFailure(details)**: Nested validation failures. The `content` property contains the string representation of failure details.

Resource messages use Java MessageFormat with placeholders: `{0}`, `{1}`, etc.

Example: `kova.charSequence.min="{0}" must be at least {1} characters`

The `Message.resource0`, `Message.resource1`, `Message.resource2`, etc. companion functions create message factories that automatically include the input value as the first argument.

To access the actual error message string, use the `content` property:
```kotlin
val result = validator.tryValidate("invalid")
if (result.isFailure()) {
    result.messages.forEach { message ->
        println(message.content)  // Prints the actual error message string
    }
}
```

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

### Chaining Pattern

Type-specific validators use a chaining pattern where they compose with previous validators and constraint validators:

```kotlin
class StringValidator internal constructor(
    private val prev: Validator<String, String> = EmptyValidator(),
    private val transform: (String) -> String = { it },
    constraint: Constraint<String> = Constraint.satisfied(),
) : Validator<String, String> {
    private val next: ConstraintValidator<String> = ConstraintValidator(constraint)

    override fun execute(context: ValidationContext, input: String): ValidationResult<String> =
        prev.map(transform).chain(next).execute(context, input)

    fun min(length: Int): StringValidator = /* returns new instance with constraint */
}
```

This provides a composable validation pipeline while allowing type-specific extension methods. When adding constraints, a new instance with updated constraints is returned.

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
- `ConstraintValidator.kt` - Generic constraint evaluator used by all type-specific validators
- `StringValidator.kt` - Validates strings (min/max length, patterns, email, transformations, numeric conversions, enum validation, etc.)
- `NumberValidator.kt` - Validates numeric types (Int, Long, Double, Float, BigDecimal, etc. with min/max/positive/negative/notPositive/notNegative)
- `LocalDateValidator.kt` - Validates LocalDate values (future, past, futureOrPresent, pastOrPresent)
- `ComparableValidator.kt` - Validates comparable types (min/max comparisons)
- `CollectionValidator.kt` - Validates collections (min/max/length/notEmpty for size, onEach for element validation)
- `MapValidator.kt` - Validates maps (min/max/length/notEmpty for size, onEach/onEachKey/onEachValue for entry/key/value validation)
- `MapEntryValidator.kt` - Validates individual map entries
- `LiteralValidator.kt` - Validates literal values (single value or list of allowed values)
- `EmptyValidator.kt` - No-op validator used by `Kova.generic()` that always succeeds

**Special Validators**:
- `ObjectSchema.kt` - Validates objects by defining validation rules for individual properties as object properties or within a constructor lambda scope (includes `ObjectSchemaScope` for object-level constraints and `PropertyValidator`)
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