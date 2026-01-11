# Available Validators

All validators are extension functions on the input type with a `Validation` context receiver.

## String & CharSequence

Supported types: `String`, `CharSequence`

```kotlin
// Length validation
input.ensureLength(10)                   // Exact length
input.ensureLengthAtLeast(1)             // Minimum length
input.ensureLengthAtMost(100)            // Maximum length
input.ensureLengthInRange(1..100)        // Length within range (supports both 1..100 and 1..<100)

// Content validation
input.ensureBlank()                      // Must be blank (empty or whitespace only)
input.ensureNotBlank()                   // Must not be blank
input.ensureEmpty()                      // Must be empty
input.ensureNotEmpty()                   // Must not be empty
input.ensureStartsWith("prefix")         // Must start with prefix
input.ensureNotStartsWith("prefix")      // Must not start with prefix
input.ensureEndsWith("suffix")           // Must end with suffix
input.ensureNotEndsWith("suffix")        // Must not end with suffix
input.ensureContains("substring")        // Must contain substring
input.ensureNotContains("substring")     // Must not contain substring
input.ensureMatches(Regex("\\d+"))       // Must match regex
input.ensureNotMatches(Regex("\\d+"))    // Must not match regex
input.ensureUppercase()                  // Must be uppercase
input.ensureLowercase()                  // Must be lowercase

// Comparable validation
input.ensureAtLeast("a")                 // At least "a" (>= "a")
input.ensureAtMost("z")                  // At most "z" (<= "z")
input.ensureGreaterThan("a")             // Greater than "a" (> "a")
input.ensureGreaterThanOrEqual("a")      // Greater than or equal to "a" (>= "a")
input.ensureLessThan("z")                // Less than "z" (< "z")
input.ensureLessThanOrEqual("z")         // Less than or equal to "z" (<= "z")
input.ensureEquals("value")              // Equal to "value" (==)
input.ensureNotEquals("value")           // Not equal to "value" (!=)

// String-specific validation
input.ensureInt()                        // Validates string is valid Int
input.ensureLong()                       // Validates string is valid Long
input.ensureShort()                      // Validates string is valid Short
input.ensureByte()                       // Validates string is valid Byte
input.ensureDouble()                     // Validates string is valid Double
input.ensureFloat()                      // Validates string is valid Float
input.ensureBigDecimal()                 // Validates string is valid BigDecimal
input.ensureBigInteger()                 // Validates string is valid BigInteger
input.ensureBoolean()                    // Validates string is valid Boolean
input.ensureEnum<Status>()               // Validates string is valid enum value

// Conversions
input.transformToInt()                   // Validate and convert to Int
input.transformToLong()                  // Validate and convert to Long
input.transformToShort()                 // Validate and convert to Short
input.transformToByte()                  // Validate and convert to Byte
input.transformToDouble()                // Validate and convert to Double
input.transformToFloat()                 // Validate and convert to Float
input.transformToBigDecimal()            // Validate and convert to BigDecimal
input.transformToBigInteger()            // Validate and convert to BigInteger
input.transformToBoolean()               // Validate and convert to Boolean
input.transformToEnum<Status>()          // Validate and convert to enum
```

## Numbers

Supported types: `Int`, `Long`, `Double`, `Float`, `Byte`, `Short`, `BigDecimal`, `BigInteger`

```kotlin
input.ensureAtLeast(0)                 // At least 0 (>= 0)
input.ensureAtMost(100)                // At most 100 (<= 100)
input.ensureGreaterThan(0)             // Greater than 0 (> 0)
input.ensureGreaterThanOrEqual(0)      // Greater than or equal to 0 (>= 0)
input.ensureLessThan(100)              // Less than 100 (< 100)
input.ensureLessThanOrEqual(100)       // Less than or equal to 100 (<= 100)
input.ensureEquals(42)                 // Equal to 42 (==)
input.ensureNotEquals(0)               // Not equal to 0 (!=)
input.ensurePositive()                 // Positive (> 0)
input.ensureNegative()                 // Negative (< 0)
input.ensureNotPositive()              // Not positive (<= 0)
input.ensureNotNegative()              // Not negative (>= 0)
```

## Temporal Types

Supported types: `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `OffsetDateTime`, `OffsetTime`, `ZonedDateTime`, `Year`, `YearMonth`, `MonthDay`

```kotlin
input.ensureAtLeast(LocalDate.of(2024, 1, 1))                 // At least 2024-01-01 (>=)
input.ensureAtMost(LocalDate.of(2024, 12, 31))                // At most 2024-12-31 (<=)
input.ensureGreaterThan(LocalDate.of(2024, 6, 1))             // Greater than 2024-06-01 (>)
input.ensureGreaterThanOrEqual(LocalDate.of(2024, 1, 1))      // Greater than or equal to 2024-01-01 (>=)
input.ensureLessThan(LocalDate.of(2025, 1, 1))                // Less than 2025-01-01 (<)
input.ensureLessThanOrEqual(LocalDate.of(2024, 12, 31))       // Less than or equal to 2024-12-31 (<=)
input.ensureEquals(LocalDate.of(2024, 6, 15))                 // Equal to 2024-06-15 (==)
input.ensureNotEquals(LocalDate.of(2024, 1, 1))               // Not equal to 2024-01-01 (!=)
input.ensureFuture()                                          // In the future
input.ensureFutureOrPresent()                                 // In the future or present
input.ensurePast()                                            // In the past
input.ensurePastOrPresent()                                   // In the past or present
```

## Iterables

Supported types: Any `Iterable` (including `List`, `Set`, `Collection`)

```kotlin
input.ensureNotEmpty()                     // Must not be empty
input.ensureContains("foo")                // Must contain element (alias: ensureHas)
input.ensureNotContains("bar")             // Must not contain element
input.ensureEach { element ->              // Validate each element
    element.ensureAtLeast(1)
}
```

## Collections

Supported types: `List`, `Set`, `Collection`

```kotlin
input.ensureSize(5)                        // Exact size
input.ensureSizeAtLeast(1)                 // Minimum size
input.ensureSizeAtMost(10)                 // Maximum size
input.ensureSizeInRange(1..10)             // Size within range (supports both 1..10 and 1..<10)
```

## Maps

```kotlin
input.ensureSize(5)                        // Exact size
input.ensureSizeAtLeast(1)                 // Minimum size
input.ensureSizeAtMost(10)                 // Maximum size
input.ensureSizeInRange(1..10)             // Size within range (supports both 1..10 and 1..<10)
input.ensureNotEmpty()                     // Must not be empty
input.ensureContainsKey("foo")             // Must contain key (alias: ensureHasKey)
input.ensureNotContainsKey("bar")          // Must not contain key
input.ensureContainsValue(42)              // Must contain value (alias: ensureHasValue)
input.ensureNotContainsValue(0)            // Must not contain value
input.ensureEachKey { key ->               // Validate each key
    key.ensureAtLeast(1)
}
input.ensureEachValue { value ->           // Validate each value
    value.ensureAtLeast(0)
}
```

## Nullable

```kotlin
input.ensureNull()                         // Must be null
input.ensureNotNull()                      // Must not be null (enables smart casting, stops on failure)
input.ensureNullOr { block }               // Accept null or validate non-null
```

## Boolean

```kotlin
input.ensureTrue()                         // Must be true
input.ensureFalse()                        // Must be false
```

## Comparable Types

Supports all `Comparable` types, such as `UInt`, `ULong`, `UByte`, and `UShort`.

```kotlin
input.ensureAtLeast(0u)                    // At least 0u (>= 0u)
input.ensureAtMost(100u)                   // At most 100u (<= 100u)
input.ensureGreaterThan(0u)                // Greater than 0u (> 0u)
input.ensureGreaterThanOrEqual(0u)         // Greater than or equal to 0u (>= 0u)
input.ensureLessThan(100u)                 // Less than 100u (< 100u)
input.ensureLessThanOrEqual(100u)          // Less than or equal to 100u (<= 100u)
input.ensureEquals(42u)                    // Equal to 42u (==)
input.ensureNotEquals(0u)                  // Not equal to 0u (!=)

// Range validation
input.ensureInRange(1..10)                 // In range 1..10 (supports both 1..10 and 1..<10)
input.ensureInClosedRange(1.0..10.0)       // In closed range 1.0..10.0 (inclusive)
input.ensureInOpenEndRange(1..<10)         // In open-end range 1..<10 (inclusive start, exclusive end)
```

## Any Type Validators

Works with any type:

```kotlin
input.ensureEquals("completed")                    // Equal to "completed"
input.ensureNotEquals("rejected")                  // Not equal to "rejected"
input.ensureInIterable(listOf("a", "b", "c"))      // One of the allowed values
```
