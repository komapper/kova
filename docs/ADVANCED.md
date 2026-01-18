# Advanced Topics

## Custom Constraints

Create custom validators using `constrain` and `satisfies`. The `constrain()` function automatically populates the constraint ID and input value in error messages:

```kotlin
context(_: Validation)
fun String.ensureUrlPath() =
    constrain("custom.urlPath") {
        satisfies(it.startsWith("/") && !it.contains("..")) {
            text("Must be a valid URL path")
        }
    }

val result = tryValidate { "/a/../b".ensureUrlPath() }
if (!result.isSuccess()) {
    result.messages.forEach(::println)
    // Message(text='Must be a valid URL path', root=, path=, input=/a/../b)
}
```

The `satisfies()` method uses a `MessageProvider` lambda for lazy message construction—the message is only created when validation fails:

```kotlin
context(_: Validation)
fun String.ensureAlphanumeric(
    message: MessageProvider = { "kova.string.alphanumeric".resource }
) = constrain("kova.string.alphanumeric") {
        satisfies(it.all { c -> c.isLetterOrDigit() }, message)
    }
```

## Nullable Validation

```kotlin
// Accept or reject null
value.ensureNull()
value.ensureNotNull()

// Validate only if non-null
email.ensureNullOr { it.ensureContains("@") }

// ensureNotNull enables smart casting - subsequent validators work on non-null type
context(_: Validation)
fun validateName(name: String?): String {
    name.ensureNotNull()           // Validates and enables smart cast
    return name.ensureLengthInRange(1..100)  // Compiler knows name is non-null
}
```

## Conditional Validation with `or` and `orElse`

Try the first validation; if it fails, try the next. Useful for alternative validation rules.

```kotlin
// Accept either domestic or international phone format
context(_: Validation)
fun validatePhone(phone: String) =
    or { phone.ensureMatches(Regex("^\\d{3}-\\d{4}$")) }      // Domestic format: 123-4567
        .orElse { phone.ensureMatches(Regex("^\\+\\d{1,3}-\\d+$")) }  // International format: +1-1234567

val result = tryValidate { validatePhone("123-abc-456") }
if (!result.isSuccess()) {
    result.messages.map { it.text }.forEach { println(it) }
    // at least one constraint must be satisfied: [[must match pattern: ^\d{3}-\d{4}$], [must match pattern: ^\+\d{1,3}-\d+$]]
}

// Chain multiple alternatives
or { id.ensureMatches(Regex("^[a-z]+$")) }    // Lowercase letters only
    .or { id.ensureMatches(Regex("^\\d+$")) }  // Digits only
    .orElse { id.ensureMatches(Regex("^[A-Z]+$")) }  // Uppercase letters only
```

## Wrapping Errors with `withMessage`

The `withMessage` function wraps validation logic and consolidates multiple errors into a single custom message. This is useful when you want to present a higher-level error message instead of detailed field-level errors:

```kotlin
data class Address(val street: String, val city: String, val zipCode: String)

context(_: Validation)
fun Address.validate() = schema {
    ::zipCode {
        withMessage("Invalid ZIP code format") {
            it.ensureMatches(Regex("^\\d{5}(-\\d{4})?$")).ensureLengthAtLeast(5)
        }
    }
}

val result = tryValidate { Address("Eitai", "Tokyo", "123-456").validate() }
if (!result.isSuccess()) {
    result.messages.forEach { println(it) }
    // Message(text='Invalid ZIP code format', root=Address, path=zipCode, input=null)
}
```

You can also use a transform function to customize how multiple errors are consolidated:

```kotlin
context(_: Validation)
fun validatePassword(password: String) =
    withMessage({ messages ->
        text("Password validation failed: ${messages.size} errors found")
    }) {
        password.ensureLengthAtLeast(8).ensureMatches(Regex(".*[A-Z].*")).ensureMatches(Regex(".*[0-9].*"))
    }
```

## Circular Reference Detection

Kova automatically detects and handles circular references in nested object validation to prevent infinite loops.

## Internationalization

Kova uses Java's `ResourceBundle` for internationalization. Default messages are provided in `kova-default.properties` within the library. You can override any message by creating a `kova.properties` file in your project's resources directory.

### Overriding Messages

Create `src/main/resources/kova.properties` to customize messages:

```properties
# Override only the messages you want to customize
kova.charSequence.notBlank=This field is required
kova.number.positive=Please enter a positive number
```

Messages not overridden in your `kova.properties` will fall back to the library defaults. This allows you to customize only the messages relevant to your application.

### Locale-Specific Messages

For locale-specific overrides, create files like `src/main/resources/kova_ja.properties` (Japanese), `src/main/resources/kova_fr.properties` (French), etc.:

```properties
# kova_ja.properties
kova.charSequence.notBlank=空白にできません
kova.number.positive=正の数である必要があります
```

The appropriate locale is selected automatically based on `Locale.getDefault()`.

### Using `resource()` in Custom Validators

The `resource()` function creates internationalized messages with parameter substitution (using MessageFormat syntax where {0}, {1}, etc. are replaced with the provided arguments):

```kotlin
// Using resource keys
str.ensureLengthAtLeast(5, message = { "custom.message.key".resource(5) })

// Multiple parameters
context(_: Validation)
fun Int.range(
    minValue: Int,
    maxValue: Int,
    message: MessageProvider = { "kova.number.range".resource(minValue, maxValue) }
) = constrain("kova.number.range") {
    satisfies(it in minValue..maxValue, message)
}
```

Corresponding entry in `kova.properties`:
```properties
kova.number.range=The value must be between {0} and {1}.
```
