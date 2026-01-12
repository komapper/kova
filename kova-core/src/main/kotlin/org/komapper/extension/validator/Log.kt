package org.komapper.extension.validator

/**
 * Logs a debug entry if logging is enabled.
 *
 * This function uses lazy evaluation - the entry block is only executed if a logger
 * is configured in [ValidationConfig]. This ensures zero overhead when logging is disabled.
 *
 * The log entry typically includes information about constraint validation results,
 * such as constraint ID, root object, validation path, and input value.
 *
 * Example:
 * ```kotlin
 * log {
 *     LogEntry.Satisfied(
 *         constraintId = "kova.string.min",
 *         root = "User",
 *         path = "name",
 *         input = "Alice"
 *     )
 * }
 * // The lambda is only evaluated if config.logger is non-null
 * ```
 *
 * @param Validation (context parameter) The validation context containing the logger configuration
 * @param block Lambda that generates the log entry (only called if logger is configured)
 */
context(v: Validation)
public inline fun log(block: () -> LogEntry) {
    v.config.logger?.invoke(block())
}

/**
 * Logs a constraint violation and enriches the message with constraint details.
 *
 * This function performs two operations:
 * 1. Logs a [LogEntry.Violated] entry with constraint information if logging is enabled
 * 2. Enriches the message with the input value and constraint ID
 *
 * This is typically called internally by the [constrain] function when a validation
 * constraint fails. The enriched message includes the actual input value and constraint ID,
 * which are displayed in error messages to help users understand what failed and why.
 *
 * @param message The validation error message to enrich
 * @param input The input value that failed validation
 * @param id The constraint identifier (e.g., "kova.number.min")
 * @return The enriched message with input and constraint ID details
 */
@PublishedApi
context(v: Validation)
internal fun logAndAddDetails(
    message: Message,
    input: Any?,
    id: String,
): Message {
    log {
        LogEntry.Violated(
            constraintId = id,
            root = message.root,
            path = message.path.fullName,
            input = input,
            args = if (message is Message.Resource) message.args else emptyList(),
        )
    }
    return message.withDetails(input, id)
}

/**
 * Represents a log entry generated during validation execution.
 *
 * Log entries are generated when validation constraints are evaluated and sent to the
 * logger callback configured in [ValidationConfig.logger]. This enables debugging and
 * monitoring of validation logic.
 */
public sealed interface LogEntry {
    /**
     * Log entry indicating that a validation constraint was satisfied.
     *
     * This is emitted when a constraint evaluation succeeds.
     *
     * @property constraintId The unique identifier of the constraint (e.g., "kova.charSequence.lengthAtLeast")
     * @property root The qualified name of the root object being validated
     * @property path The validation path indicating the property location (e.g., "address.city")
     * @property input The input value that was validated
     */
    public data class Satisfied(
        val constraintId: String,
        val root: String,
        val path: String,
        val input: Any?,
    ) : LogEntry

    /**
     * Log entry indicating that a validation constraint was violated.
     *
     * This is emitted when a constraint evaluation fails and a validation error is generated.
     *
     * @property constraintId The unique identifier of the constraint (e.g., "kova.number.min")
     * @property root The qualified name of the root object being validated
     * @property path The validation path indicating the property location (e.g., "age")
     * @property input The input value that failed validation
     * @property args The arguments used in the constraint evaluation (e.g., [10] for min(value, 10))
     */
    public data class Violated(
        val constraintId: String,
        val root: String,
        val path: String,
        val input: Any?,
        val args: List<Any?>,
    ) : LogEntry
}
