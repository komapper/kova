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
- **example-core**, **example-factory**, **example-ktor**, **example-exposed**, **example-hibernate-validator**, **example-konform**: Examples

## Core API

### Entry Points
- `tryValidate { ... }` → `ValidationResult<T>` (Success | Failure)
- `validate { ... }` → `T` (throws ValidationException on failure)

### Validation Context
All validators are extension functions on `Validation` with **input-first parameters**:
```kotlin
tryValidate {
    ensureNotBlank(name)
    ensureMinLength(name, 1)
    ensureMaxLength(name, 100)
    name
}
```

### Schema Validation
```kotlin
fun Validation.validate(user: User) = user.schema {
    user::name { ensureLengthInRange(it, 1..100) }
    user::age { ensureMin(it, 0); ensureMax(it, 120) }
}
```

### Factory Validation
```kotlin
fun Validation.buildUser(rawName: String, rawAge: String) = factory {
    val name by bind(rawName) { ensureNotBlank(it); it }
    val age by bind(rawAge) { parseInt(it) }
    User(name, age)
}
```

### Ktor Integration
```kotlin
data class Customer(...) : Validated {
    override fun Validation.validate() = this@Customer.schema {
        this@Customer::id { ensurePositive(it) }
        this@Customer::name { ensureNotBlank(it); ensureMinLength(it, 1) }
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
- Constraint IDs: `kova.any.*`, `kova.boolean.*`, `kova.comparable.*`, `kova.charSequence.*`, `kova.string.*`, `kova.number.*`, `kova.iterable.*`, `kova.collection.*`

### Circular Reference Detection
- `Validation.addPathChecked()` detects circular refs via object identity (`===`)
- Returns `null` when circular reference detected (schema skips property)

### Nullable Handling
- `ensureNull(input)`, `ensureNotNull(input)`, `ensureNullOr(input) { block }`
- `ensureNotNull(input)` uses Kotlin contract for smart casting and raises immediately on null (stops subsequent validation)

### Conditional Validation
- `or { ... } orElse { ... }` - try first, fallback to second
- `withMessage(transform) { ... }` - wrap errors with custom message

### Collections
- `ensureEach(collection) { constraint }` - validates each element
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
- `AnyValidator.kt`, `BooleanValidator.kt`, `CharSequenceValidator.kt`, `StringValidator.kt`, `NumberValidator.kt`, `ComparableValidator.kt`
- `IterableValidator.kt`, `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`
- `Constraint.kt`, `Accumulate.kt`, `Message.kt`, `Path.kt`

### kova-factory
- `Factory.kt` - `factory()`, `bind()`

### kova-ktor
- `SchemaValidator.kt`, `Validated.kt`
