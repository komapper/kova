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
- **Validator<IN, OUT>**: Core interface with `execute(context, input)` method
- **ValidationResult**: Sealed interface (`Success<T>` | `Failure`)
- **ValidationContext**: Tracks state (root, path, failFast), supports circular reference detection via `Path.containsObject()`

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
- `.notNull()` rejects null values
- `.isNull()` accepts only null values

### Message System
Three types: `Message.Text`, `Message.Resource` (i18n from `kova.properties`), `Message.ValidationFailure` (nested). Access via `.content` property.

## Key Files
- `Kova.kt` - Main API
- `Validator.kt` - Core interface
- `ValidationContext.kt` - State tracking with circular reference detection
- `ObjectSchema.kt` - Object validation with property rules
- `ObjectFactory.kt` - Validate + construct (supports 1-10 args)
- `Constraints.kt` - Shared constraint utilities (`min`, `max`, `isNull`, `notNull`)