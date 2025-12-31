# CLAUDE.md

**Kova** is a type-safe Kotlin validation library with context-based validators.

## Build Commands

```bash
./gradlew test              # Run all tests
./gradlew kova-core:test    # Run kova-core tests
./gradlew build             # Build project
./gradlew spotlessApply     # Format code
```

**Stack**: Kotlin, Gradle 8.14, Java 17, Kotest, Ktor 3.0.0+

## Modules

- **kova-core**: Core validation (`org.komapper.extension.validator`)
- **kova-factory**: Factory validation with property delegation (`org.komapper.extension.validator.factory`)
- **kova-ktor**: Ktor integration (`org.komapper.extension.validator.ktor.server`)
- **example-core**, **example-factory**, **example-ktor**, **example-exposed**, **example-hibernate-validator**: Examples

## Core API

### Entry Points
- `tryValidate { ... }` → `ValidationResult<T>` (Success | Failure)
- `validate { ... }` → `T` (throws ValidationException on failure)

### Validation Context
All validators are extension functions on `Validation` with **input-first parameters**:
```kotlin
tryValidate {
    notBlank(name)
    minLength(name, 1)
    maxLength(name, 100)
    name
}
```

### Schema Validation
```kotlin
fun Validation.validate(user: User) = user.schema {
    user::name { minLength(it, 1); maxLength(it, 100) }
    user::age { minValue(it, 0); maxValue(it, 120) }
}
```

### Factory Validation
```kotlin
fun Validation.buildUser(rawName: String, rawAge: String) = factory {
    val name by bind(rawName) { notBlank(it); it }
    val age by bind(rawAge) { toInt(it) }
    User(name, age)
}
```

### Ktor Integration
```kotlin
data class Customer(...) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        this@Customer::id { positive(it) }
        this@Customer::name { notBlank(it); minLength(it, 1) }
    }
}
```

## Key Implementation Details

### Custom Validators
Use `constrain(id)` and `satisfies(condition, message)`:
```kotlin
fun Validation.alphanumeric(input: String) = input.constrain("custom.alphanumeric") {
    satisfies(it.all { c -> c.isLetterOrDigit() }) { "kova.string.alphanumeric".resource }
}
```

### Message System
- `"kova.constraint.id".resource(arg1, arg2)` - i18n messages from `kova.properties`
- `satisfies(condition) { message }` - MessageProvider is `() -> Message`
- Constraint IDs: `kova.comparable.*`, `kova.charSequence.*`, `kova.string.*`, `kova.number.*`

### Circular Reference Detection
- `Validation.addPathChecked()` detects circular refs via object identity (`===`)
- Returns `null` when circular reference detected (schema skips property)

### Nullable Handling
- `isNull(input)`, `notNull(input)`, `isNullOr(input) { block }`, `toNonNullable(input)`

### Conditional Validation
- `or { ... } orElse { ... }` - try first, fallback to second
- `withMessage(transform) { ... }` - wrap errors with custom message

### Collections
- `onEach(collection) { constraint }` - validates each element
- Automatic index tracking: `items[0]<collection element>`

### Temporal Validators
- Clock from `ValidationConfig.clock` (use fixed clock for testing)
- `MonthDay`: Comparable only (no past/future)

### ValidationConfig
- `failFast: Boolean` - stop at first error vs collect all
- `clock: Clock` - for temporal validators
- `logger: ((LogEntry) -> Unit)?` - debug logging

## Key Files

### kova-core
- `Validator.kt`, `Validation.kt`, `ValidationResult.kt`, `ValidationConfig.kt`
- `CharSequenceValidator.kt`, `StringValidator.kt`, `NumberValidator.kt`, `ComparableValidator.kt`
- `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`
- `Constraint.kt`, `Accumulate.kt`, `Message.kt`, `Path.kt`

### kova-factory
- `Factory.kt` - `factory()`, `bind()`

### kova-ktor
- `SchemaValidator.kt`, `Validated.kt`
