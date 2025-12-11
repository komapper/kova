# CLAUDE.md

**Kova** is a type-safe Kotlin validation library with composable validators and detailed error reporting.

## Build Commands

```bash
./gradlew test              # Run all tests
./gradlew kova-core:test    # Run kova-core tests
./gradlew kova-ktor:test    # Run kova-ktor tests
./gradlew build             # Build project
./gradlew spotlessApply     # Format code (auto-runs during build)
```

**Stack**: Kotlin, Gradle 8.14, Java 17, Kotest, Spotless/ktlint, Ktor 3.0.0+
**Note**: Spotless suppresses `standard:no-wildcard-imports` ktlint rule.

## Modules

- **kova-core**: Core validation library (`org.komapper.extension.validator`)
  - Main: `kova-core/src/main/kotlin/org/komapper/extension/validator/`
  - Tests: `kova-core/src/test/kotlin/org/komapper/extension/validator/`
- **kova-ktor**: Ktor integration (`org.komapper.extension.validator.ktor.server`)
  - Main: `kova-ktor/src/main/kotlin/org/komapper/extension/validator/ktor/server/`
- **example-core**: Core validation examples
- **example-ktor**: Ktor integration example

## Core Architecture

### Validators
- **Validator<IN, OUT>**: Core interface with `execute(input, context)` method (input first, context second)
- **IdentityValidator<T>**: Type alias for `Validator<T, T>` - for validators that don't transform types
- **NullableValidator<T, S>**: Type alias for `Validator<T?, S?>` - for nullable validators (accepts null input/output)
- **NullCoalescingValidator<T, S>**: Type alias for `Validator<T?, S>` - coalesces null to default value (nullable input, non-null output)
- **TemporalValidator<T>**: Type alias for `IdentityValidator<T>` - for temporal validators
- **ValidationResult**: Sealed interface (`Success<T>` | `Failure`)
- **ValidationContext**: Tracks state (root, path, config), supports circular reference detection
- **ValidationConfig**: Centralized settings (failFast, clock, logger)

### Design Patterns
- **Most validators**: Extension functions built on `constrain()` (e.g., `CharSequenceValidator.kt`, `StringValidator.kt`, `NumberValidator.kt`, `TemporalValidator.kt`)
- **CharSequenceValidator**: Extension functions for CharSequence types (length, blank, empty, contains, matches, email, etc.) - works with String and other CharSequence types
- **StringValidator**: String-specific validators (type conversions, enum, case transformations) - builds on CharSequenceValidator
- **TemporalValidator**: Type alias with extension functions using reified type parameters for temporal constraints
- **Immutability**: All validators immutable; composition operators return new instances
- **Composition**: `+`, `and`, `or`, `map`, `then`, `compose` with lambda-based overloads for fluent API

## Key Patterns

### Basic Usage
```kotlin
Kova.string().min(1).max(10).tryValidate("hello")
```

### Object Validation
```kotlin
object UserSchema : ObjectSchema<User>({
    User::name { it.min(1).max(10) }  // 'it' is base validator
    User::age { it.min(0) }
})
```

### Ktor Integration
```kotlin
@ValidatedWith(CustomerSchema::class)
@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String)

object CustomerSchema : ObjectSchema<Customer>({
    Customer::id { it.positive() }
    Customer::firstName { it.min(1).max(50) }
})

fun Application.module() {
    install(RequestValidation) { validate(SchemaValidator()) }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
}
```

## Important Implementation Details

### ObjectSchema Properties
- Properties defined in constructor lambda using property references (e.g., `User::name { validator }`)
- Constructor lambda provides `ObjectSchemaScope` with `constrain()` for object-level constraints
- `choose` method provides both object instance and base validator for conditional validation
- Use `self` to reference the schema itself for recursive validation

### Nullable Handling
- `Kova.nullable<T>()` accepts null by default
- `.asNullable()` converts any validator to nullable
- `.asNullable(defaultValue)` / `.asNullable { defaultValue }` sets default for null
- `.notNull()` rejects null, `.isNull()` accepts only null
- `.isNullOr { validator }` - equivalent to `.isNull().or(validator.asNullable())`
- `.notNullAnd { validator }` - equivalent to `.notNull().and(validator.asNullable())`

### Circular Reference Detection
- `ValidationContext.addRoot()` accepts object reference for tracking
- `ValidationContext.addPathChecked()` detects circular references via `Path.containsObject()`
- Uses object identity (`===`) to prevent infinite loops

### Message System
- **Message Types**: `Text`, `Resource` (i18n from `kova.properties`), `Collection`, `Or`
- All have `constraintId`, `text`, `root`, `path`, `context` properties
- Access via `.text` property (not `.content`)
- **MessageProvider**: Functional interface with signature `invoke(vararg args: Any?): (ConstraintContext<*>) -> Message`
- Pass constraint parameters (not input) to `message()`, access via `ctx[0]`, `ctx[1]` in lambda
- Input accessible via `ctx.input` if needed
- **Constraint IDs**:
  - Comparison validators use consolidated `kova.comparable.*` IDs (not type-specific)
  - CharSequence validators use `kova.charSequence.*` IDs (min, max, length, blank, empty, startsWith, endsWith, contains, matches, email)
  - String-specific validators use `kova.string.*` IDs (type conversions, enum, case transformations)


### Temporal Validators
- Type alias `TemporalValidator<T> = IdentityValidator<T>` - no wrapper interface
- Extension functions with reified type parameters (e.g., `inline fun <reified T> TemporalValidator<T>.future()`)
- Clock obtained from `ValidationConfig.clock` at validation time (not at validator creation)
- Internal `now()` function maps `KClass<T>` to appropriate `T.now(Clock)` method
- `MonthDay` implements `Comparable` but not `Temporal` (comparison constraints only, no past/future)
- Custom clock for testing: `validator.tryValidate(date, config = ValidationConfig(clock = fixedClock))`

### Ktor Integration
- **@ValidatedWith(schemaClass)**: Links data class to ObjectSchema (schema must be object declaration)
- **SchemaValidator**:
  - Constructor: `errorFormatter: (List<Message>) -> String` (defaults to joining with newlines)
  - `filter()` checks for @ValidatedWith annotation
  - `validate()` extracts schema and validates request body
  - Converts Kova's ValidationResult to Ktor's ValidationResult
- Use StatusPages plugin to handle `RequestValidationException`

## Key Files

### kova-core
- **Core**: `Validator.kt`, `IdentityValidator.kt`, `NullableValidator.kt`, `NullCoalescingValidator.kt`, `ValidationResult.kt`, `ValidationContext.kt`, `ValidationConfig.kt`
- **Type validators**: `CharSequenceValidator.kt`, `StringValidator.kt`, `NumberValidator.kt`, `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`, `ComparableValidator.kt`
- **Object validation**: `ObjectSchema.kt`
- **Constraint system**: `ConstraintValidator.kt`, `ConstraintContext.kt`, `ConstraintResult.kt`
- **Messaging**: `Message.kt`, `MessageProvider.kt`
- **Entry point**: `Kova.kt` - factory methods for creating validators (companion object provides static access)
- **Utilities**: `Path.kt`, `ValidationException.kt`

### kova-ktor
- `SchemaValidator.kt` - Integrates ObjectSchemas with Ktor's RequestValidation plugin
- `ValidatedWith.kt` - Annotation for linking data classes to ObjectSchemas
