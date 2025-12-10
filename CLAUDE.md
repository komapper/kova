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

**Note**: Spotless is configured to suppress the `standard:no-wildcard-imports` ktlint rule to allow wildcard imports where appropriate.

## Package Structure & File Locations

Kova consists of two modules:
- **kova-core**: Core validation library
- **kova-ktor**: Ktor integration module

### kova-core

Core validation classes use the package name **`org.komapper.extension.validator`**:

- **Main source**: `kova-core/src/main/kotlin/org/komapper/extension/validator/`
- **Test source**: `kova-core/src/test/kotlin/org/komapper/extension/validator/`

**File organization by type:**
- **Core interfaces**: `Validator.kt`, `IdentityValidator.kt`, `NullableValidator.kt`, `ValidationResult.kt`, `ValidationContext.kt`, `ValidationConfig.kt`
- **Type-specific validators**: `StringValidator.kt`, `NumberValidator.kt`, `CollectionValidator.kt`, `MapValidator.kt`, `TemporalValidator.kt`, `ComparableValidator.kt`, etc.
- **Object validation**: `ObjectSchema.kt`, `ObjectFactory.kt`
- **Constraint system**: `ConstraintValidator.kt`, `ConstraintContext.kt`, `ConstraintResult.kt`
- **Messaging**: `Message.kt`, `MessageProvider.kt`
- **Main entry point**: `Kova.kt`
- **Utilities**: `Path.kt`, `ValidationException.kt`

### kova-ktor

Ktor integration classes use the package name **`org.komapper.extension.validator.ktor.server`**:

- **Main source**: `kova-ktor/src/main/kotlin/org/komapper/extension/validator/ktor/server/`
- **Key files**:
  - `SchemaValidator.kt` - Ktor Validator implementation for ObjectSchema validation
  - `ValidatedWith.kt` - Annotation to link data classes with ObjectSchemas

### Example projects

- **example-core**: Examples demonstrating core validation features (object validation, factory pattern, nested validation)
- **example-ktor**: Example Ktor application demonstrating kova-ktor integration with RequestValidation plugin

## Core Architecture

### Validators
- **Validator<IN, OUT>**: Core interface with `execute(input, context)` method (input first, context second)
- **IdentityValidator<T>**: Type alias for `Validator<T, T>` - simplifies type signatures for validators that don't transform types
- **ValidationResult**: Sealed interface (`Success<T>` | `Failure`)
- **FailureDetail**: Sealed interface (`Single` | `Or`) representing individual failure information
- **ValidationContext**: Tracks state (root, path, config), supports circular reference detection via `Path.containsObject()`
- **ValidationConfig**: Centralized settings (failFast, locale)
- **Architecture**: Most validators use extension functions built on top of `constrain()`. TemporalValidator uses an interface-based design where validation methods are interface members rather than extension functions, providing better encapsulation of clock and temporalNow state

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

// MonthDay implements Comparable but not Temporal (comparison constraints only)
Kova.monthDay().min(MonthDay.of(3, 1)).max(MonthDay.of(10, 31))

// Custom clock for testing (captured in closure, not exposed as public property)
val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
val kova = Kova(fixedClock)
val validator = kova.localDate().future()

// All temporal validators support composition operators (+, and, or)
val validator = Kova.localDate().past() + Kova.localDate().min(LocalDate.of(2020, 1, 1))
```

**Ktor integration:**
```kotlin
// Annotate data class with validation schema
@ValidatedWith(CustomerSchema::class)
@Serializable
data class Customer(val id: Int, val firstName: String, val lastName: String)

object CustomerSchema : ObjectSchema<Customer>() {
    val id = Customer::id { it.positive() }
    val firstName = Customer::firstName { it.min(1).max(50) }
}

// Install SchemaValidator in Ktor application
fun Application.module() {
    install(RequestValidation) {
        validate(SchemaValidator())
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString("\n"))
        }
    }
}

// Requests are automatically validated
routing {
    post("/customers") {
        val customer = call.receive<Customer>()  // Validated automatically
        call.respond(HttpStatusCode.Created, customer)
    }
}
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

### Ktor Integration (kova-ktor module)
- **`@ValidatedWith(schemaClass)`**: Annotation that links a data class to its ObjectSchema for automatic validation
  - Must be applied to the data class that needs validation
  - The schema class must be an object declaration (not a regular class)
  - Works with Ktor's RequestValidation plugin
- **`SchemaValidator`**: Ktor Validator implementation that integrates Kova ObjectSchemas with Ktor's request validation
  - Constructor parameter: `errorFormatter: (List<Message>) -> String` - Optional custom error formatter (defaults to joining messages with newlines)
  - Implements Ktor's `Validator` interface with `validate()` and `filter()` methods
  - `filter()` checks for @ValidatedWith annotation to determine which requests to validate
  - `validate()` extracts the ObjectSchema from the annotation and uses it to validate the request body
  - Converts Kova's ValidationResult to Ktor's ValidationResult
- **Error handling**: Use Ktor's StatusPages plugin to handle RequestValidationException
- **Package**: All Ktor integration classes are in `org.komapper.extension.validator.ktor.server`

## Key Files
- `Kova.kt` - Main API entry point with factory methods returning `IdentityValidator<T>` or specialized validators. Includes `Kova()` factory function for creating instances with custom clocks
- `Validator.kt` - Core interface and composition operators (`+`, `and`, `or`, `map`, `then`, `compose`) with lambda-based overloads
- `IdentityValidator.kt` - Type alias for `Validator<T, T>`, provides `chain`, `constrain`, `onlyIf`, and `literal` extension functions
- `NullableValidator.kt` - Type alias for `Validator<T?, S?>` with nullable-specific extensions (`isNull`, `notNull`, `isNullOr`, `notNullAnd`, `withDefault`, `asNullable`)
- `TemporalValidator.kt` - Interface-based validator for temporal types with methods as interface members. Factory function captures clock and temporalNow in closure for better encapsulation
- `ComparableValidator.kt` - Extension functions for `Comparable` types (min, max, gt, gte, lt, lte) with consolidated `kova.comparable.*` constraint IDs
- `ValidationResult.kt` - Result types (`Success`, `Failure`) and `FailureDetail` interface
- `ValidationContext.kt` - State tracking with circular reference detection
- `ValidationConfig.kt` - Centralized validation settings (failFast, locale)
- `ObjectSchema.kt` - Object validation with property rules, factory method, and ObjectSchemaFactoryScope for object construction
- `ObjectFactory.kt` - Object construction interface and internal createObjectFactory functions (1-10 args)
- `ConstraintValidator.kt` - Converts `ConstraintResult` to `ValidationResult`, base for extension functions
- `Message.kt` - Message types (Text, Resource, Collection, Or) with `text`, `constraintId`, `root`, `path`, and `context` properties
- `MessageProvider.kt` - MessageProvider interface (no type parameter) and MessageProviderFactory for creating text/resource message providers
- **Validator extension files** - `StringValidator.kt`, `NumberValidator.kt`, `CollectionValidator.kt`, etc. define extension functions on `IdentityValidator<T>`

### kova-ktor files
- `SchemaValidator.kt` - Ktor Validator implementation that integrates ObjectSchemas with Ktor's RequestValidation plugin
- `ValidatedWith.kt` - Annotation for linking data classes to ObjectSchemas for automatic validation