# CLAUDE.md

This file helps AI assistants and contributors navigate and modify the Kova codebase.

For API documentation and usage examples, see `Package.md` or README.md.

## Build Commands

```bash
./gradlew test              # Run all tests
./gradlew kova-core:test    # Run kova-core tests
./gradlew build             # Build project
./gradlew spotlessApply     # Format code
```

**Stack**: Kotlin, Gradle 8.14, Java 17, Kotest, Ktor 3.0.0+

**Requires**: `-Xcontext-parameters` compiler flag (configured in build.gradle.kts)

## Modules

- **kova-core**: Core validation (`org.komapper.extension.validator`)
- **kova-ktor**: Ktor integration (`org.komapper.extension.validator.ktor.server`)
- **example-core**, **example-ktor**, **example-exposed**, **example-hibernate-validator**, **example-konform**: Examples

## Key Files

### kova-core
- `Validator.kt` - `tryValidate`, `validate`, `ValidationException`
- `Validation.kt` - `Validation` context class and `ValidationConfig`
- `ValidationResult.kt` - `ValidationResult` (Success/Failure)
- `ValidationIor.kt` - Internal inclusive-or type, `bind`, `or`, `orElse`
- `Schema.kt` - `schema` function and `Schema` class for object validation
- `Constraint.kt` - `constrain`, `transformOrRaise`, `Constraint` class
- `Accumulate.kt` - Error accumulation logic
- `Message.kt` - `Message` types, `text()`, `resource()`, `withMessage`
- `Path.kt` - `Path` class, `addPath`, `addPathChecked`, `named`
- `Log.kt` - `LogEntry`, `log()`
- `Capture.kt` - `capture()` for object creation with property delegation
- Validators: `AnyValidator.kt`, `BooleanValidator.kt`, `NullableValidator.kt`, `CharSequenceValidator.kt`, `StringValidator.kt`, `NumberValidator.kt`, `ComparableValidator.kt`, `IterableValidator.kt`, `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`

### kova-ktor
- `SchemaValidator.kt`, `Validated.kt`

## Implementation Details

### Custom Validators
Use `constrain(id)` and `satisfies(condition, message)`:
```kotlin
context(_: Validation)
fun String.ensureAlphanumeric() = constrain("custom.alphanumeric") {
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
- `input.ensureNull()`, `input.ensureNotNull()`, `input.ensureNullOr { block }`
- `input.ensureNotNull()` uses Kotlin contract for smart casting and raises immediately on null (stops subsequent validation)

### Conditional Validation
- `or { ... } orElse { ... }` - try first, fallback to second
- `withMessage(transform) { ... }` - wrap errors with custom message

### Collections
- `collection.ensureEach { constraint }` - validates each element
- Automatic index tracking: `items[0]<collection element>`

### Temporal Validators
- Clock from `ValidationConfig.clock` (use fixed clock for testing)
- `MonthDay`: Comparable only (no past/future)

### ValidationConfig
- `failFast: Boolean` - stop at first error vs collect all
- `clock: Clock` - for temporal validators
- `logger: ((LogEntry) -> Unit)?` - debug logging
