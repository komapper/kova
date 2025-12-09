# CLAUDE.md

**Kova** is a type-safe Kotlin validation library with composable validators and detailed error reporting.

## Build Commands

```bash
./gradlew test              # Run all tests
./gradlew build             # Build project
./gradlew spotlessApply     # Format code (auto-runs during build)
```

**Stack**: Kotlin, Gradle 8.14, Java 17, Kotest, Spotless/ktlint

## Package Structure & File Locations

All Kova classes and functions use the package name **`org.komapper.extension.validator`**. Files are located in the directory structure corresponding to the package:

- **Main source**: `kova-core/src/main/kotlin/org/komapper/extension/validator/`
- **Test source**: `kova-core/src/test/kotlin/org/komapper/extension/validator/`

**File organization by type:**
- **Core interfaces**: `Validator.kt`, `IdentityValidator.kt`, `NullableValidator.kt`, `ValidationResult.kt`, `ValidationContext.kt`, `ValidationConfig.kt`
- **Type-specific validators**: `StringValidator.kt`, `NumberValidator.kt`, `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`, etc.
- **Object validation**: `ObjectSchema.kt`, `ObjectFactory.kt`
- **Constraint system**: `ConstraintValidator.kt`, `Constraints.kt`, `ConstraintContext.kt`, `ConstraintResult.kt`
- **Messaging**: `Message.kt`, `MessageProvider.kt`
- **Main entry point**: `Kova.kt`
- **Utilities**: `Path.kt`, `ValidationException.kt`

## Core Architecture

### Validators
- **Validator<IN, OUT>**: Core interface with `execute(input, context)` method (input first, context second)
- **IdentityValidator<T>**: Type alias for `Validator<T, T>` - simplifies type signatures for validators that don't transform types
- **ValidationResult**: Sealed interface (`Success<T>` | `Failure`)
- **FailureDetail**: Sealed interface (`Single` | `Or`) representing individual failure information
- **ValidationContext**: Tracks state (root, path, config), supports circular reference detection via `Path.containsObject()`
- **ValidationConfig**: Centralized settings (failFast, locale)
- **Architecture**: Validators use extension functions built on top of `constrain()` rather than specialized interfaces

### Key Patterns

**Simple validation:**
```kotlin
val result = Kova.string().min(1).max(10).tryValidate("hello")
```

**Object validation:**
```kotlin
object UserSchema : ObjectSchema<User>() {
    val name = User::name { it.min(1).max(10) }
    val age = User::age { it.min(0) }
}
```

**Object-level constraints:**
```kotlin
object PeriodSchema : ObjectSchema<Period>({
    constrain("dateRange") { satisfies(it.input.startDate <= it.input.endDate, "...") }
}) {
    val startDate = Period::startDate { it }
    val endDate = Period::endDate { it }
}
```

**Object factory (validate + construct):**
```kotlin
object PersonSchema : ObjectSchema<Person>() {
    private val nameV = Person::name { it.min(1) }
    private val ageV = Person::age { it.min(0) }

    fun bind(name: String, age: Int) = factory {
        create(::Person, nameV.bind(name), ageV.bind(age))
    }
}
val result = PersonSchema.bind("Alice", 30).tryCreate()
```

**Temporal validators:**
```kotlin
// LocalDate, LocalTime, and LocalDateTime validators with temporal constraints
Kova.localDate().past()
Kova.localTime().future()
Kova.localDateTime().futureOrPresent()

// Comparison methods (min, max, gt, gte, lt, lte)
Kova.localDate().min(LocalDate.of(2024, 1, 1)).max(LocalDate.of(2024, 12, 31))
Kova.localTime().gte(LocalTime.of(9, 0)).lte(LocalTime.of(17, 0))
Kova.localDateTime().gt(startDateTime).lt(endDateTime)

// All temporal validators support composition operators (+, and, or, chain)
val validator = Kova.localDate().past() + Kova.localDate().min(LocalDate.of(2020, 1, 1))
```

## Important Implementation Details

### Immutability & Composition
All validators are immutable. Composition operators (`+`, `and`, `or`, `map`, `then`, `compose`) return new instances. The `chain` method is available on `IdentityValidator<T>` for chaining validators with the same input/output type.

**Lambda-based composition**: Composition operators have lambda-based overloads that accept builder functions for more fluent API:
- `and { validator }` - Combines validators with AND logic using a lambda
- `or { validator }` - Combines validators with OR logic using a lambda
- `then { validator }` - Chains validators (right-to-left composition) using a lambda
- `compose { validator }` - Composes validators (left-to-right composition) using a lambda

### Circular Reference Detection
- `ValidationContext.addRoot()` accepts object reference for tracking
- `ValidationContext.addPathChecked()` detects circular references via `Path.containsObject()`
- Uses object identity (`===`) to prevent infinite loops in nested validation

### ObjectSchema Properties
Properties must be object properties (not in constructor lambda) since the `invoke` operator is only available on `ObjectSchema` itself.

**Property validator DSL**: The `invoke` operator passes a base validator as the lambda parameter (`it`), enabling concise validator composition:
```kotlin
object UserSchema : ObjectSchema<User>() {
    val id = User::id { it.min(1) }  // 'it' is a base validator for the property type
    val name = User::name { it.min(1).max(10) }
}
```

**Conditional validation with choose**: The `choose` method provides both the object instance and a base validator parameter:
```kotlin
object AddressSchema : ObjectSchema<Address>() {
    val country = Address::country { it }
    val postalCode = Address::postalCode choose { address, v ->
        when (address.country) {
            "US" -> v.length(5)
            "CA" -> v.length(6)
            else -> v.min(1)
        }
    }
}
```

### Nullable Handling
- `Kova.nullable<T>()` accepts null by default
- `.asNullable()` converts any validator to nullable (accepts null)
- `.asNullable(defaultValue)` converts to nullable with default value for null inputs
- `.asNullable { defaultValue }` converts to nullable with lazy-evaluated default value
- `.withDefault(value)` sets default value for null inputs on nullable validators
- `.withDefault { value }` sets lazy-evaluated default for null inputs on nullable validators
- `.notNull()` rejects null values
- `.isNull()` accepts only null values
- `.isNullOr { validator }` convenience method: equivalent to `.isNull().or(validator.asNullable())`
- `.notNullAnd { validator }` convenience method: equivalent to `.notNull().and(validator.asNullable())`

### Failure Structure
- **ValidationResult.Failure**: Contains a list of `FailureDetail` objects
- **FailureDetail**: Interface with `context`, `message`, `root`, and `path` properties
- Internal implementations handle simple and composite (OR) failures
- **Message Types**:
  - `Message.Text` - Simple hardcoded text messages with `text` property
  - `Message.Resource` - i18n messages from `kova.properties` with `text` property (lazy-loaded)
  - `Message.Collection` - Collection/map element validation failures with `elements` property containing nested FailureDetail list
  - `Message.Or` - OR validator failures with `first` and `second` properties containing FailureDetail from both branches
- All Message types have `constraintId`, `text`, `root`, `path`, and `context` properties
- Access message text via `.text` property (renamed from `.content`)
- OR failures automatically compose messages showing both validation branches

### ObjectFactory Pattern
- **`factory(block)`**: Method on ObjectSchema that creates an ObjectSchemaFactoryScope, providing access to bind/create methods
- **`bind(value)`**: Extension method on validators (available in factory scope) that creates an ObjectFactory from a validator and value
- **`create(constructor, ...factories)`**: Method (available in factory scope) that validates ObjectFactories and constructs objects (supports 1-10 arguments)
- **`tryCreate(config)`**: Execute ObjectFactory, returning ValidationResult
- **`create(config)`**: Execute ObjectFactory, returning object or throwing ValidationException
- **Note**: The `bind` and `create` methods are only available within the `factory { }` scope (ObjectSchemaFactoryScope)

### MessageProvider Pattern
- **MessageProvider**: Functional interface for creating error messages in custom validators
- **No type parameter**: `MessageProvider` (not `MessageProvider<T>`)
- **Signature**: `invoke(vararg args: Any?): (ConstraintContext<*>) -> Message`
- **Factory methods**: `Message.text { ctx -> "..." }` for custom text, `Message.resource()` for i18n
- **Usage in validators**: Pass constraint parameters (not input value) to `message()`, access via `ctx[0]`, `ctx[1]`, etc. in message lambda
- **Input access**: Input value can be accessed via `ctx.input` in the message lambda if needed
- **Constraint IDs**: Comparison validators (min, max, gt, gte, lt, lte) use consolidated `kova.comparable.*` constraint IDs regardless of type (instead of type-specific kova.number.*, kova.temporal.*, etc.)
- **Example**:
```kotlin
fun StringValidator.min(
    length: Int,
    message: MessageProvider = Message.resource()
) = constrain("kova.string.min") {
    satisfies(it.input.length >= length, message(length))
}
```

## Key Files
- `Kova.kt` - Main API entry point, factory methods returning `IdentityValidator<T>`
- `Validator.kt` - Core interface and composition operators (`+`, `and`, `or`, `map`, `then`, `compose`) with lambda-based overloads
- `IdentityValidator.kt` - Type alias for `Validator<T, T>`, provides `chain`, `constrain`, `onlyIf`, and `literal` extension functions
- `NullableValidator.kt` - Type alias for `Validator<T?, S?>` with nullable-specific extensions (`isNull`, `notNull`, `isNullOr`, `notNullAnd`, `withDefault`, `asNullable`)
- `ValidationResult.kt` - Result types (`Success`, `Failure`) and `FailureDetail` interface
- `ValidationContext.kt` - State tracking with circular reference detection
- `ValidationConfig.kt` - Centralized validation settings (failFast, locale)
- `ObjectSchema.kt` - Object validation with property rules, factory method, and ObjectSchemaFactoryScope for object construction
- `ObjectFactory.kt` - Object construction interface and internal createObjectFactory functions (1-10 args)
- `ConstraintValidator.kt` - Converts `ConstraintResult` to `ValidationResult`, base for extension functions
- `Constraints.kt` - Shared constraint utilities (`min`, `max`, `isNull`, `notNull`)
- `Message.kt` - Message types (Text, Resource, Collection, Or) with `text`, `constraintId`, `root`, `path`, and `context` properties
- `MessageProvider.kt` - MessageProvider interface (no type parameter) and MessageProviderFactory for creating text/resource message providers
- **Validator extension files** - `StringValidator.kt`, `NumberValidator.kt`, `CollectionValidator.kt`, etc. define extension functions on `IdentityValidator<T>`