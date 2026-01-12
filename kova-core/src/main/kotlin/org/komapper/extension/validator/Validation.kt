package org.komapper.extension.validator

import java.time.Clock

/**
 * Context object that tracks the state of validation execution.
 *
 * Contains information about the validation root, current path, and configuration.
 * The context is immutable and threaded through the validation process, with validation
 * functions creating new contexts with updated state as needed.
 *
 * @property root The root object's qualified name (e.g., "com.example.User")
 * @property path The current validation path, tracking nested objects and circular references
 * @property config Validation configuration settings
 * @property acc Accumulator function for collecting validation errors
 */
public data class Validation(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val config: ValidationConfig = ValidationConfig(),
    val acc: Accumulate = { error("Accumulate context not initialized") },
)

/**
 * The clock used for temporal validation constraints (ensurePast, ensureFuture, etc.).
 *
 * Delegates to [ValidationConfig.clock].
 *
 * @param Validation (context parameter) The validation context containing the clock configuration
 * @return The configured [Clock] instance
 */
context(v: Validation)
public val clock: Clock get() = v.config.clock

/**
 * Configuration settings for validation execution.
 *
 * @property failFast If true, validation stops at the first failure instead of collecting all errors.
 *                    Default is false (collect all errors).
 * @property clock The clock used for temporal validation constraints (ensurePast, ensureFuture, ensurePastOrPresent, ensureFutureOrPresent).
 *                 Defaults to [Clock.systemDefaultZone]. Use a fixed clock for deterministic testing.
 * @property logger Optional callback function for receiving debug log messages during validation.
 *                  If null (default), no logging is performed. Each log message ensureContains information
 *                  about constraint satisfaction/violation, including constraint ID, root, path, and input value.
 *
 * Example:
 * ```kotlin
 * // Basic configuration with fail-fast
 * val config = ValidationConfig(
 *     failFast = true,
 *     logger = { entry -> println(entry) }
 * )
 * val result = tryValidate(config) { min("hello", 1); max("hello", 10) }
 *
 * // Configuration with fixed clock for testing temporal validators
 * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
 * val testConfig = ValidationConfig(clock = fixedClock)
 * val result2 = tryValidate(testConfig) {
 *     ensurePast(LocalDate.of(2024, 12, 31))
 * }
 * ```
 */
public data class ValidationConfig(
    val failFast: Boolean = false,
    val clock: Clock = Clock.systemDefaultZone(),
    val logger: ((LogEntry) -> Unit)? = null,
)
