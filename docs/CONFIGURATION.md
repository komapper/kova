# Validation Configuration

You can customize validation behavior using `ValidationConfig`.

## Fail-Fast Mode

By default, Kova collects all validation errors. Use fail-fast mode to stop at the first error:

```kotlin
// Collect all errors (default)
val result = tryValidate { user.validate() }

// Stop at first error
val result = tryValidate(ValidationConfig(failFast = true)) { user.validate() }
```

**When to use fail-fast:**
- Performance-critical paths where collecting all errors is wasteful
- When the first error makes subsequent validation meaningless
- Chain of dependent validations where later ones depend on earlier success

## Custom Clock for Temporal Validation

Temporal validators (`ensureInFuture()`, `ensureInPast()`, etc.) use the system clock by default. Provide a custom clock for testing:

```kotlin
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

context(_: Validation)
fun validateDate(date: LocalDate) {
    date.ensureInFuture()
}

// Fixed clock for testing
val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))

val result = tryValidate(config = ValidationConfig(clock = fixedClock)) {
    val date = LocalDate.of(2024, 6, 20)
    validateDate(date)  // Compares against the fixed clock
}
```

## Debug Logging

Enable logging to trace the validation flow:

```kotlin
val result = tryValidate(config = ValidationConfig(
    logger = { logEntry -> println("[Validation] $logEntry") }
)) {
    user.validate()
}
```

Log entries include:
- Constraint evaluations
- Path changes
- Error accumulation

## Combined Configuration

All options can be combined:

```kotlin
val result = tryValidate(config = ValidationConfig(
    failFast = true,
    clock = Clock.systemUTC(),
    logger = { logEntry -> println(logEntry) }
)) {
    user.validate()
}
```

## Configuration Options Reference

| Option     | Type                    | Default                     | Description                        |
|------------|-------------------------|-----------------------------|------------------------------------|
| `failFast` | `Boolean`               | `false`                     | Stop at first error vs collect all |
| `clock`    | `Clock`                 | `Clock.systemDefaultZone()` | Clock for temporal validators      |
| `logger`   | `((LogEntry) -> Unit)?` | `null`                      | Debug logging callback             |
