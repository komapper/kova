# CLAUDE.md

**Kova** is a type-safe Kotlin validation library with composable validators and detailed error reporting.

## Build Commands

```bash
./gradlew test              # Run all tests
./gradlew build             # Build project
./gradlew spotlessApply     # Format code (auto-runs during build)
```

**Stack**: Kotlin, Gradle 8.14, Java 17, Kotest, Spotless/ktlint

## Core Architecture

### Validators
- **Validator<IN, OUT>**: Core interface with `execute(input, context)` method (input first, context second)
- **ValidationResult**: Sealed interface (`Success<T>` | `Failure`)
- **FailureDetail**: Sealed interface (`Single` | `Or`) representing individual failure information
- **ValidationContext**: Tracks state (root, path, config), supports circular reference detection via `Path.containsObject()`
- **ValidationConfig**: Centralized settings (failFast, locale)

### Key Patterns

**Simple validation:**
```kotlin
val result = Kova.string().min(1).max(10).tryValidate("hello")
```

**Object validation:**
```kotlin
object UserSchema : ObjectSchema<User>() {
    val name = User::name { Kova.string().min(1).max(10) }
    val age = User::age { Kova.int().min(0) }
}
```

**Object-level constraints:**
```kotlin
object PeriodSchema : ObjectSchema<Period>({
    constrain("dateRange") { satisfies(it.input.startDate <= it.input.endDate, "...") }
}) {
    val startDate = Period::startDate { Kova.localDate() }
    val endDate = Period::endDate { Kova.localDate() }
}
```

**Object factory (validate + construct):**
```kotlin
object PersonSchema : ObjectSchema<Person>() {
    private val name = Person::name { Kova.string().min(1) }
    private val age = Person::age { Kova.int().min(0) }

    fun build(name: String, age: Int) =
        arguments(arg(this.name, name), arg(this.age, age)).build(::Person)
}
val result = PersonSchema.build("Alice", 30).tryCreate()
```

## Important Implementation Details

### Immutability & Composition
All validators are immutable. Composition operators (`+`, `and`, `or`, `map`, `andThen`) return new instances.

### Circular Reference Detection
- `ValidationContext.addRoot()` accepts object reference for tracking
- `ValidationContext.addPathChecked()` detects circular references via `Path.containsObject()`
- Uses object identity (`===`) to prevent infinite loops in nested validation

### ObjectSchema Properties
Properties must be object properties (not in constructor lambda) since the `invoke` operator is only available on `ObjectSchema` itself.

### Nullable Handling
- `Kova.nullable<T>()` accepts null by default
- `.asNullable()` converts any validator to nullable (accepts null)
- `.asNullable(defaultValue)` converts to nullable with default value for null inputs
- `.asNullable { defaultValue }` converts to nullable with lazy-evaluated default value
- `.withDefault(value)` sets default value for null inputs on nullable validators
- `.withDefault { value }` sets lazy-evaluated default for null inputs on nullable validators
- `.notNull()` rejects null values
- `.isNull()` accepts only null values

### Failure Structure
- **ValidationResult.Failure**: Contains a list of `FailureDetail` objects
- **FailureDetail.Single**: Individual failure with context, message, and optional cause
- **FailureDetail.Or**: Composite failure from `or` operator with first/second branches
- **Message Types**: `Message.Text`, `Message.Resource` (i18n from `kova.properties`), `Message.ValidationFailure` (contains nested FailureDetail list)
- Access message content via `.content` property
- OR failures automatically compose messages showing both validation branches

## Key Files
- `Kova.kt` - Main API entry point
- `Validator.kt` - Core interface and composition operators (`+`, `and`, `or`, `map`, `andThen`)
- `ValidationResult.kt` - Result types (`Success`, `Failure`) and `FailureDetail` hierarchy (`Single`, `Or`)
- `ValidationContext.kt` - State tracking with circular reference detection
- `ValidationConfig.kt` - Centralized validation settings (failFast, locale)
- `ObjectSchema.kt` - Object validation with property rules
- `ObjectFactory.kt` - Validate + construct (supports 1-10 args)
- `ConstraintValidator.kt` - Converts `ConstraintResult` to `ValidationResult`
- `Constraints.kt` - Shared constraint utilities (`min`, `max`, `isNull`, `notNull`)
- `Message.kt` - Message types (Text, Resource, ValidationFailure)