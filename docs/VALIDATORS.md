# Available Validators

All validators are extension functions with a `context(_: Validation)` receiver. Each validator has a unique constraint ID that can be used for custom error handling or internationalization.

## Quick Reference

| Category                                       | Types                                                | Validators                                         |
|------------------------------------------------|------------------------------------------------------|----------------------------------------------------|
| [String & CharSequence](#string--charsequence) | `String`, `CharSequence`                             | Length, content, pattern matching, type conversion |
| [Numbers](#numbers)                            | `Int`, `Long`, `Double`, `Float`, `BigDecimal`, etc. | Positive, negative, range validation               |
| [Comparable](#comparable)                      | Any `Comparable` type                                | Comparison operators, range validation             |
| [Temporal](#temporal)                          | `LocalDate`, `LocalDateTime`, `Instant`, etc.        | Past, future, date comparisons                     |
| [Collections](#collections)                    | `List`, `Set`, `Collection`                          | Size, element validation                           |
| [Iterables](#iterables)                        | Any `Iterable`                                       | Contains, each element validation                  |
| [Maps](#maps)                                  | `Map`                                                | Size, key/value validation                         |
| [Nullable](#nullable)                          | Any nullable type                                    | Null checks with smart casting                     |
| [Boolean](#boolean)                            | `Boolean`                                            | True/false validation                              |
| [Any Type](#any-type)                          | Any type                                             | Equality, allowed values                           |

---

## String & CharSequence

### Length Validation

| Validator                    | Constraint ID                     | Error Message                     | Example                             |
|------------------------------|-----------------------------------|-----------------------------------|-------------------------------------|
| `ensureLength(n)`            | `kova.charSequence.length`        | must be exactly {0} characters    | `input.ensureLength(10)`            |
| `ensureLengthAtLeast(n)`     | `kova.charSequence.lengthAtLeast` | must be at least {0} characters   | `input.ensureLengthAtLeast(1)`      |
| `ensureLengthAtMost(n)`      | `kova.charSequence.lengthAtMost`  | must be at most {0} characters    | `input.ensureLengthAtMost(100)`     |
| `ensureLengthInRange(range)` | `kova.charSequence.lengthInRange` | must have length within range {0} | `input.ensureLengthInRange(1..100)` |

### Content Validation

| Validator                      | Constraint ID                     | Error Message               | Example                                |
|--------------------------------|-----------------------------------|-----------------------------|----------------------------------------|
| `ensureNotBlank()`             | `kova.charSequence.notBlank`      | must not be blank           | `input.ensureNotBlank()`               |
| `ensureBlank()`                | `kova.charSequence.blank`         | must be blank               | `input.ensureBlank()`                  |
| `ensureNotEmpty()`             | `kova.charSequence.notEmpty`      | must not be empty           | `input.ensureNotEmpty()`               |
| `ensureEmpty()`                | `kova.charSequence.empty`         | must be empty               | `input.ensureEmpty()`                  |
| `ensureStartsWith(prefix)`     | `kova.charSequence.startsWith`    | must start with "{0}"       | `input.ensureStartsWith("https://")`   |
| `ensureNotStartsWith(prefix)`  | `kova.charSequence.notStartsWith` | must not start with "{0}"   | `input.ensureNotStartsWith("http://")` |
| `ensureEndsWith(suffix)`       | `kova.charSequence.endsWith`      | must end with "{0}"         | `input.ensureEndsWith(".com")`         |
| `ensureNotEndsWith(suffix)`    | `kova.charSequence.notEndsWith`   | must not end with "{0}"     | `input.ensureNotEndsWith(".exe")`      |
| `ensureContains(substring)`    | `kova.charSequence.contains`      | must contain "{0}"          | `input.ensureContains("@")`            |
| `ensureNotContains(substring)` | `kova.charSequence.notContains`   | must not contain "{0}"      | `input.ensureNotContains("<script>")`  |
| `ensureMatches(regex)`         | `kova.charSequence.matches`       | must match pattern: {0}     | `input.ensureMatches(Regex("\\d+"))`   |
| `ensureNotMatches(regex)`      | `kova.charSequence.notMatches`    | must not match pattern: {0} | `input.ensureNotMatches(Regex("\\s"))` |

### Case Validation (String only)

| Validator           | Constraint ID           | Error Message     | Example                   |
|---------------------|-------------------------|-------------------|---------------------------|
| `ensureUppercase()` | `kova.string.uppercase` | must be uppercase | `input.ensureUppercase()` |
| `ensureLowercase()` | `kova.string.lowercase` | must be lowercase | `input.ensureLowercase()` |

### Type Checking (String only)

These validators check if the string can be parsed as the specified type without converting.

| Validator            | Constraint ID            | Error Message                  |
|----------------------|--------------------------|--------------------------------|
| `ensureInt()`        | `kova.string.int`        | must be a valid integer        |
| `ensureLong()`       | `kova.string.long`       | must be a valid long           |
| `ensureShort()`      | `kova.string.short`      | must be a valid short          |
| `ensureByte()`       | `kova.string.byte`       | must be a valid byte           |
| `ensureDouble()`     | `kova.string.double`     | must be a valid double         |
| `ensureFloat()`      | `kova.string.float`      | must be a valid float          |
| `ensureBigDecimal()` | `kova.string.bigDecimal` | must be a valid decimal number |
| `ensureBigInteger()` | `kova.string.bigInteger` | must be a valid integer number |
| `ensureBoolean()`    | `kova.string.boolean`    | must be "true" or "false"      |
| `ensureEnum<T>()`    | `kova.string.enum`       | must be one of: {0}            |

### Type Conversion (String only)

These validators convert the string to the specified type, raising an error immediately if conversion fails.

| Validator                 | Constraint ID            | Return Type  | Error Message                  |
|---------------------------|--------------------------|--------------|--------------------------------|
| `transformToInt()`        | `kova.string.int`        | `Int`        | must be a valid integer        |
| `transformToLong()`       | `kova.string.long`       | `Long`       | must be a valid long           |
| `transformToShort()`      | `kova.string.short`      | `Short`      | must be a valid short          |
| `transformToByte()`       | `kova.string.byte`       | `Byte`       | must be a valid byte           |
| `transformToDouble()`     | `kova.string.double`     | `Double`     | must be a valid double         |
| `transformToFloat()`      | `kova.string.float`      | `Float`      | must be a valid float          |
| `transformToBigDecimal()` | `kova.string.bigDecimal` | `BigDecimal` | must be a valid decimal number |
| `transformToBigInteger()` | `kova.string.bigInteger` | `BigInteger` | must be a valid integer number |
| `transformToBoolean()`    | `kova.string.boolean`    | `Boolean`    | must be "true" or "false"      |
| `transformToEnum<T>()`    | `kova.string.enum`       | `T`          | must be one of: {0}            |

**Usage:**
```kotlin
context(_: Validation)
fun buildProduct(rawPrice: String): Product {
    val price by capture { rawPrice.transformToDouble().ensurePositive() }
    return Product(price)
}
```

---

## Numbers

Supported types: `Int`, `Long`, `Double`, `Float`, `Byte`, `Short`, `BigDecimal`, `BigInteger`

| Validator             | Constraint ID             | Error Message        | Condition |
|-----------------------|---------------------------|----------------------|-----------|
| `ensurePositive()`    | `kova.number.positive`    | must be positive     | > 0       |
| `ensureNegative()`    | `kova.number.negative`    | must be negative     | < 0       |
| `ensureNotPositive()` | `kova.number.notPositive` | must not be positive | <= 0      |
| `ensureNotNegative()` | `kova.number.notNegative` | must not be negative | >= 0      |

Numbers also support all [Comparable validators](#comparable) for range and comparison operations.

---

## Comparable

Works with any `Comparable` type including numbers, strings, dates, and custom types.

### Comparison Validators

| Validator                         | Constraint ID                        | Error Message                        | Condition |
|-----------------------------------|--------------------------------------|--------------------------------------|-----------|
| `ensureAtLeast(value)`            | `kova.comparable.atLeast`            | must be greater than or equal to {0} | >= value  |
| `ensureAtMost(value)`             | `kova.comparable.atMost`             | must be less than or equal to {0}    | <= value  |
| `ensureGreaterThan(value)`        | `kova.comparable.greaterThan`        | must be greater than {0}             | > value   |
| `ensureGreaterThanOrEqual(value)` | `kova.comparable.greaterThanOrEqual` | must be greater than or equal to {0} | >= value  |
| `ensureLessThan(value)`           | `kova.comparable.lessThan`           | must be less than {0}                | < value   |
| `ensureLessThanOrEqual(value)`    | `kova.comparable.lessThanOrEqual`    | must be less than or equal to {0}    | <= value  |

### Range Validators

| Validator                     | Constraint ID                    | Error Message            | Example                                |
|-------------------------------|----------------------------------|--------------------------|----------------------------------------|
| `ensureInRange(range)`        | `kova.comparable.inRange`        | must be within range {0} | `input.ensureInRange(1..100)`          |
| `ensureInClosedRange(range)`  | `kova.comparable.inClosedRange`  | must be within range {0} | `input.ensureInClosedRange(1.0..10.0)` |
| `ensureInOpenEndRange(range)` | `kova.comparable.inOpenEndRange` | must be within range {0} | `input.ensureInOpenEndRange(1..<10)`   |

---

## Temporal

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

| Validator                 | Constraint ID                   | Error Message                    | Description               |
|---------------------------|---------------------------------|----------------------------------|---------------------------|
| `ensureFuture()`          | `kova.temporal.future`          | must be in the future            | After current time        |
| `ensureFutureOrPresent()` | `kova.temporal.futureOrPresent` | must be in the future or present | At or after current time  |
| `ensurePast()`            | `kova.temporal.past`            | must be in the past              | Before current time       |
| `ensurePastOrPresent()`   | `kova.temporal.pastOrPresent`   | must be in the past or present   | At or before current time |

Temporal types also support all [Comparable validators](#comparable) for date/time comparisons.

**Note:** `MonthDay` only supports Comparable validators (no past/future validation).

**Testing with fixed clock:**
```kotlin
val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))
val result = tryValidate(ValidationConfig(clock = fixedClock)) {
    date.ensureFuture()
}
```

---

## Collections

Supported types: `List`, `Set`, `Collection`

| Validator                  | Constraint ID                 | Error Message                                         | Example                          |
|----------------------------|-------------------------------|-------------------------------------------------------|----------------------------------|
| `ensureSize(n)`            | `kova.collection.size`        | Collection (size {0}) must have exactly {1} elements  | `input.ensureSize(5)`            |
| `ensureSizeAtLeast(n)`     | `kova.collection.sizeAtLeast` | Collection (size {0}) must have at least {1} elements | `input.ensureSizeAtLeast(1)`     |
| `ensureSizeAtMost(n)`      | `kova.collection.sizeAtMost`  | Collection (size {0}) must have at most {1} elements  | `input.ensureSizeAtMost(10)`     |
| `ensureSizeInRange(range)` | `kova.collection.sizeInRange` | Collection size must be within range {0}              | `input.ensureSizeInRange(1..10)` |

Collections also support all [Iterable validators](#iterables).

---

## Iterables

Works with any `Iterable` including `List`, `Set`, and custom iterables.

| Validator                    | Constraint ID               | Error Message                                    | Example                             |
|------------------------------|-----------------------------|--------------------------------------------------|-------------------------------------|
| `ensureNotEmpty()`           | `kova.iterable.notEmpty`    | must not be empty                                | `input.ensureNotEmpty()`            |
| `ensureContains(element)`    | `kova.iterable.contains`    | must contain {0}                                 | `input.ensureContains("admin")`     |
| `ensureNotContains(element)` | `kova.iterable.notContains` | must not contain {0}                             | `input.ensureNotContains("banned")` |
| `ensureEach { }`             | `kova.iterable.each`        | Some elements do not satisfy the constraint: {0} | See below                           |

**Validating each element:**
```kotlin
context(_: Validation)
fun List<Int>.validateScores() {
    ensureEach { score -> score.ensureInRange(0..100) }
}
// Error path includes index: items[0], items[1], etc.
```

---

## Maps

| Validator                       | Constraint ID               | Error Message                                   | Example                                  |
|---------------------------------|-----------------------------|-------------------------------------------------|------------------------------------------|
| `ensureSize(n)`                 | `kova.map.size`             | Map (size {0}) must have exactly {1} entries    | `input.ensureSize(5)`                    |
| `ensureSizeAtLeast(n)`          | `kova.map.sizeAtLeast`      | Map (size {0}) must have at least {1} entries   | `input.ensureSizeAtLeast(1)`             |
| `ensureSizeAtMost(n)`           | `kova.map.sizeAtMost`       | Map (size {0}) must have at most {1} entries    | `input.ensureSizeAtMost(10)`             |
| `ensureSizeInRange(range)`      | `kova.map.sizeInRange`      | Map size must be within range {0}               | `input.ensureSizeInRange(1..10)`         |
| `ensureNotEmpty()`              | `kova.map.notEmpty`         | must not be empty                               | `input.ensureNotEmpty()`                 |
| `ensureContainsKey(key)`        | `kova.map.containsKey`      | must contain key {0}                            | `input.ensureContainsKey("id")`          |
| `ensureNotContainsKey(key)`     | `kova.map.notContainsKey`   | must not contain key {0}                        | `input.ensureNotContainsKey("password")` |
| `ensureContainsValue(value)`    | `kova.map.containsValue`    | must contain value {0}                          | `input.ensureContainsValue(true)`        |
| `ensureNotContainsValue(value)` | `kova.map.notContainsValue` | must not contain value {0}                      | `input.ensureNotContainsValue(null)`     |
| `ensureEachKey { }`             | `kova.map.eachKey`          | Some keys do not satisfy the constraint: {0}    | See below                                |
| `ensureEachValue { }`           | `kova.map.eachValue`        | Some values do not satisfy the constraint: {0}  | See below                                |
| `ensureEach { }`                | `kova.map.each`             | Some entries do not satisfy the constraint: {0} | See below                                |

**Validating map contents:**
```kotlin
context(_: Validation)
fun Map<String, Int>.validateConfig() {
    ensureEachKey { key -> key.ensureNotBlank() }
    ensureEachValue { value -> value.ensureInRange(0..100) }
    ensureEach { (key, value) ->
        if (key.startsWith("max")) value.ensureAtLeast(50)
    }
}
```

---

## Nullable

| Validator          | Constraint ID           | Error Message    | Description                                 |
|--------------------|-------------------------|------------------|---------------------------------------------|
| `ensureNull()`     | `kova.nullable.null`    | must be null     | Value must be null                          |
| `ensureNotNull()`  | `kova.nullable.notNull` | must not be null | Value must not be null (enables smart cast) |
| `ensureNullOr { }` | `kova.nullable.null`    | must be null     | Accept null or validate non-null value      |

**Smart casting with ensureNotNull:**
```kotlin
context(_: Validation)
fun validateName(name: String?): String {
    val validName = name.ensureNotNull()  // Smart cast to String
    return validName.ensureNotBlank()     // No null check needed
}
```

**Note:** `ensureNotNull()` raises immediately on failure (doesn't accumulate with subsequent errors).

**Optional validation with ensureNullOr:**
```kotlin
context(_: Validation)
fun validateEmail(email: String?) {
    email.ensureNullOr { it.ensureContains("@") }  // Skip validation if null
}

tryValidate { validateEmail(null) }         // Success (null is accepted)
tryValidate { validateEmail("a@b.com") }    // Success (contains @)
tryValidate { validateEmail("invalid") }    // Failure (missing @)
```

---

## Boolean

| Validator       | Constraint ID        | Error Message |
|-----------------|----------------------|---------------|
| `ensureTrue()`  | `kova.boolean.true`  | must be true  |
| `ensureFalse()` | `kova.boolean.false` | must be false |

---

## Any Type

Works with any type.

| Validator                  | Constraint ID        | Error Message            | Example                                         |
|----------------------------|----------------------|--------------------------|-------------------------------------------------|
| `ensureEquals(value)`      | `kova.any.equals`    | must be equal to {0}     | `input.ensureEquals("active")`                  |
| `ensureNotEquals(value)`   | `kova.any.notEquals` | must not be equal to {0} | `input.ensureNotEquals("deleted")`              |
| `ensureInIterable(values)` | `kova.any.in`        | must be one of: {0}      | `input.ensureInIterable(listOf("a", "b", "c"))` |

---

## Custom Error Messages

All validators accept an optional `message` parameter to override the default error message:

```kotlin
// Text message
input.ensureNotBlank(message = { text("Username is required") })

// Internationalized message
input.ensureLengthAtLeast(3, message = { "custom.username.tooShort".resource(3) })
```

---

## Internationalization

Default messages are loaded from `kova.properties`. Create locale-specific files for translations:

**kova_ja.properties:**
```properties
kova.charSequence.notBlank=空白にできません
kova.number.positive=正の数である必要があります
kova.temporal.future=将来の日付である必要があります
```

The constraint ID is used as the property key.
