package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import java.time.Clock
import kotlin.contracts.contract

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
 * Adds a custom constraint to the input value.
 *
 * This is a fundamental building block for creating custom validation rules.
 * The constraint function is executed within an accumulating context that collects
 * validation errors. If the constraint fails, the error is accumulated and validation
 * continues (unless failFast is enabled).
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun String.alphanumeric(
 *     message: MessageProvider = { "kova.string.alphanumeric".resource }
 * ) = constrain("kova.string.alphanumeric") {
 *     satisfies(it.all { c -> c.isLetterOrDigit() }, message)
 * }
 *
 * tryValidate { "abc123".alphanumeric() } // Success
 * tryValidate { "abc-123".alphanumeric() } // Failure
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to validate
 * @param T The type of the value being validated
 * @param id Unique identifier for the constraint (used in error messages and logging)
 * @param check Constraint logic that validates the input value; receives the [Validation] context
 *              and executes within a [Constraint] receiver scope
 * @return The validated input value (allows method chaining)
 */
@IgnorableReturnValue
context(_: Validation)
public inline fun <T> T.constrain(
    id: String,
    check: context(Validation) Constraint.(T) -> Unit,
): T = apply { accumulatingThenCheck(id, check) }

@IgnorableReturnValue
@PublishedApi
context(_: Validation)
internal inline fun <T> T.accumulatingThenCheck(
    id: String,
    check: context(Validation) Constraint.(T) -> Unit,
): Accumulate.Value<T> =
    accumulating {
        mapEachMessage({ logAndAddDetails(it, this@accumulatingThenCheck, id) }) {
            val v = contextOf<Validation>()
            Constraint.check(this)
            log {
                LogEntry.Satisfied(
                    constraintId = id,
                    root = v.root,
                    path = v.path.fullName,
                    input = this,
                )
            }
        }
        this
    }

/**
 * Raises a validation error immediately if the predicate returns true.
 *
 * This function evaluates the predicate against the input value and, if true,
 * immediately raises a validation error (bypassing error accumulation). This is
 * useful for fail-fast scenarios where subsequent validation cannot continue
 * if a certain condition is met (e.g., null checks before accessing properties).
 *
 * The predicate logic is inverted internally: `satisfies(!predicate(it), message)`
 * means the constraint is satisfied when the predicate is false.
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to validate
 * @param T The type of the value being validated
 * @param constraintId Unique identifier for the constraint (used in error messages and logging)
 * @param message Provider for the error message if the predicate returns true
 * @param predicate Function that returns true when validation should fail
 */
@IgnorableReturnValue
@PublishedApi
context(_: Validation)
internal fun <T> T.raiseIf(
    constraintId: String,
    message: MessageProvider,
    predicate: (T) -> Boolean,
) {
    contract { returns() implies (this@raiseIf != null) }
    val result = accumulatingThenCheck(constraintId) { satisfies(!predicate(it), message) }
    when (result) {
        is Accumulate.Ok -> {}
        is Accumulate.Error -> result.raise()
    }
}

/**
 * Transforms a value and raises an error immediately if the transformation fails.
 *
 * This function applies a transformation to the input value. If the transformation
 * returns null, it immediately raises a validation error. This is designed for
 * type-transforming validators (e.g., `String.transformToInt()`) where the original
 * type cannot continue through the validation chain.
 *
 * Example:
 * ```kotlin
 * context(_: Validation)
 * fun String.transformToInt(
 *     message: MessageProvider = { "kova.string.int".resource },
 * ): Int = transformOrRaise("kova.string.int", message) { it.toIntOrNull() }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value to transform
 * @param T The type of the input value
 * @param R The type of the transformed value
 * @param constraintId Unique identifier for the constraint (used in error messages and logging)
 * @param message Provider for the error message if transformation returns null
 * @param transform Function that transforms the input; returns null on failure
 * @return The transformed non-null value
 */
@IgnorableReturnValue
context(_: Validation)
public fun <T, R> T.transformOrRaise(
    constraintId: String,
    message: MessageProvider,
    transform: (T) -> R?,
): R & Any {
    val transformed = transform(this)
    raiseIf(constraintId, message) { transformed == null }
    return transformed!!
}

/**
 * Extracts the value from a [ValidationIor], accumulating or raising errors as needed.
 *
 * This function handles all cases of [ValidationIor]:
 * - [Success]: Returns the value directly
 * - [Failure]: Raises validation failure with accumulated messages
 * - [Both]: Accumulates partial errors and returns the partial value
 *
 * Example:
 * ```kotlin
 * val result: ValidationIor<String> = or { ensureNotBlank(name) }
 * val value: String = result.bind()  // Extracts or raises
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<T> The validation result to extract from
 * @param T The type of the value
 * @return The validated value
 * @throws ValidationCancellationException if this is a [Failure]
 */
context(_: Validation)
public fun <T> ValidationIor<T>.bind(): T =
    when (this) {
        is Success -> value
        is Failure -> raise(messages)
        is Both -> {
            accumulate(messages)
            value
        }
    }

/**
 * Attempts alternative validation logic if this validation fails.
 *
 * This function implements a fallback strategy: if the current validation succeeds
 * (is not [FailureLike]), it returns immediately. Otherwise, it executes the fallback
 * block. If both fail, it wraps both error messages with a combined "or" message.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     or { min(input, 10) } or { max(input, 5) }
 *     // Validates that input >= 10 OR input <= 5
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return [ValidationIor] representing the combined validation result
 */
context(v: Validation)
public inline infix fun <R> ValidationIor<R>.or(block: context(Validation)() -> R): ValidationIor<R> {
    if (this !is FailureLike) return this
    val other = eval(block)
    if (other !is FailureLike) return other
    return (this as? Both ?: other).withMessage("kova.or".resource(messages, other.messages))
}

@PublishedApi
context(v: Validation)
internal inline fun <R> eval(block: context(Validation)() -> R): ValidationIor<R> = or(block)

/**
 * Attempts alternative validation logic and extracts the result, raising errors if both fail.
 *
 * This is a convenience function that combines [or] and [bind]. It tries the current
 * validation, falls back to the alternative if needed, and extracts the value or raises
 * accumulated errors.
 *
 * Example:
 * ```kotlin
 * tryValidate {
 *     val value = or { min(input, 10) } orElse { max(input, 5) }
 *     // Returns the value if either constraint passes, raises if both fail
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver ValidationIor<R> The current validation result
 * @param R The type of the validation result
 * @param block The fallback validation logic to try if this fails
 * @return The validated value
 * @throws ValidationCancellationException if both validations fail
 */
context(_: Validation)
public inline infix fun <R> ValidationIor<R>.orElse(block: context(Validation)() -> R): R = or(block).bind()

/**
 * Validates a value with a named path segment.
 *
 * This creates a new validation context with an extended path, allowing you to
 * track where in a nested object structure validation occurs. The path name is
 * included in error messages to help identify which property failed validation.
 *
 * Example:
 * ```kotlin
 * data class Address(val city: String, val zipCode: String)
 * data class User(val name: String, val address: Address)
 *
 * tryValidate {
 *     user.address.name("address") {
 *         ensureNotBlank(user.address.city)
 *         // Error path will be "address.city"
 *     }
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The value being validated
 * @param T The type of the value being validated
 * @param R The type of the validation result
 * @param name The name for this path segment (typically a property name)
 * @param block The validation logic to execute in this path context
 * @return The result of executing the validation block
 */
context(_: Validation)
public inline fun <T, R> T.named(
    name: String,
    block: context(Validation)(T) -> R,
): R = addPath(name, this, { block(this@named) })

/**
 * Creates a text-based validation message with plain text content.
 *
 * Use this method to create ad-hoc validation messages instead of using i18n resource keys.
 * When a validation error occurs, the enclosing `constrain()` call will automatically populate
 * the constraint ID and input value in the message.
 *
 * Example usage in a constraint:
 * ```kotlin
 * tryValidate {
 *     10.constrain("ensurePositive") {
 *         satisfies(it > 0) { text("Value must be ensurePositive") }
 *     }
 * }
 * ```
 *
 * Example with schema validation:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * context(_: Validation)
 * fun validate(period: Period) = period.schema {
 *     period::startDate { it.ensurePastOrPresent() }
 *     period::endDate { it.ensureFutureOrPresent() }
 *     period.constrain("period") {
 *         satisfies(it.startDate <= it.endDate) {
 *             text("Start date must be before or equal to end date")
 *         }
 *     }
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param content The text content of the error message
 * @return A [Message.Text] instance with the given content
 */
context(v: Validation)
public fun text(content: String): Message = Message.Text("", v.root, v.path, content, null)

/**
 * Creates a resource-based validation message for internationalization.
 *
 * Use this method to create internationalized messages that load text from `kova.properties`.
 * The message key is the receiver string (typically a constraint ID like "kova.number.min").
 * Arguments are provided as a vararg and used for MessageFormat substitution
 * (i.e., the first argument becomes {0}, second becomes {1}, etc.).
 *
 * When a validation error occurs, the enclosing `constrain()` call will automatically populate
 * the constraint ID and input value in the message.
 *
 * Example usage in a constraint:
 * ```kotlin
 * context(_: Validation)
 * fun Int.min(
 *     value: Int,
 *     message: MessageProvider = { "kova.number.min".resource(value) }
 * ) = apply {
 *     constrain("kova.number.min") {
 *         satisfies(it >= value, message)
 *     }
 * }
 *
 * tryValidate { 5.min(0) } // Success
 * ```
 *
 * The corresponding entry in `kova.properties` would be:
 * ```
 * kova.number.min=The value must be greater than or equal to {0}.
 * ```
 *
 * For multiple arguments:
 * ```kotlin
 * context(_: Validation)
 * fun Int.range(
 *     min: Int,
 *     max: Int,
 *     message: MessageProvider = { "kova.number.range".resource(min, max) }
 * ) = apply {
 *     constrain("kova.number.range") {
 *         satisfies(it in min..max, message)
 *     }
 * }
 * ```
 *
 * With corresponding resource:
 * ```
 * kova.number.range=The value must be between {0} and {1}.
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The resource key (typically a constraint ID like "kova.number.min")
 * @param args Arguments to be interpolated into the message template using MessageFormat
 * @return A [Message.Resource] instance configured with the provided arguments
 */
context(v: Validation)
public fun String.resource(vararg args: Any?): Message.Resource = Message.Resource(this, this, v.root, v.path, null, args = args.toList())

/**
 * Creates a resource-based validation message without arguments.
 *
 * This is a convenience property that calls [resource] with no arguments.
 * Use this for simple messages that don't require parameter interpolation.
 *
 * Example:
 * ```kotlin
 * satisfies(condition) { "kova.string.notBlank".resource }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver String The resource key (typically a constraint ID like "kova.string.notBlank")
 * @return A [Message.Resource] instance with no interpolation arguments
 */
context(_: Validation)
public val String.resource: Message.Resource get() = resource()

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

/**
 * Initializes the validation root if not already set and executes a block.
 *
 * The root represents the top-level object being validated and is displayed
 * in error messages. This function is typically called internally by the [schema]
 * extension function at the start of object validation.
 *
 * If the root is already initialized, the block is executed with the current context.
 * Otherwise, a new context is created with the root initialized before executing the block.
 *
 * Example:
 * ```kotlin
 * obj.schema {
 *     // This internally calls addRoot with the object's class name
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the result from executing the block
 * @param name The qualified name of the root object (typically class name)
 * @param obj The root object instance (used for circular reference detection)
 * @param block The validation logic to execute with the initialized context
 * @return The result of executing the block
 */
context(v: Validation)
public inline fun <R> addRoot(
    name: String,
    obj: Any?,
    block: context(Validation)() -> R,
): R =
    block(
        if (v.root.isEmpty()) {
            // initialize root
            v.copy(root = name, path = Path(name = "", obj = obj, parent = null))
        } else {
            v
        },
    )

/**
 * Adds a path segment for nested validation and executes a block.
 *
 * This creates a new path node in the validation chain, tracking the progression
 * through nested objects and properties. The path is displayed in error messages
 * to indicate exactly where validation failed. After creating the new context with
 * the extended path, the block is executed.
 *
 * Example:
 * ```kotlin
 * obj.name("address") {
 *     // Validate address properties with path "address"
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the result from executing the block
 * @param name The name of the property or field being validated
 * @param obj The object at this path (used for circular reference detection)
 * @param block The validation logic to execute with the extended path
 * @return The result of executing the block
 */
context(v: Validation)
public inline fun <R> addPath(
    name: String,
    obj: Any?,
    block: context(Validation)() -> R,
): R {
    val parent = v.path
    val path =
        parent.copy(
            name = name,
            obj = obj,
            parent = parent,
        )
    return block(v.copy(path = path))
}

/**
 * Binds an object reference to the current path node and executes a block.
 *
 * This updates the object reference at the current path level without changing
 * the path name or structure. It's used internally to bind validated values
 * to the path for circular reference detection. After updating the object reference,
 * the block is executed.
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the result from executing the block
 * @param obj The object to bind to the current path
 * @param block The validation logic to execute with the updated context
 * @return The result of executing the block
 */
context(v: Validation)
public inline fun <R> bindObject(
    obj: Any?,
    block: context(Validation)() -> R,
): R {
    val path = v.path.copy(obj = obj)
    return block(v.copy(path = path))
}

/**
 * Adds a path segment with circular reference detection and executes a block.
 *
 * This function combines [addPath] with circular reference checking. If the object
 * ensureHas already appeared in the validation path, it returns null to prevent
 * infinite validation loops.
 *
 * The caller (typically the property invoke operator in [schema] validation)
 * interprets the null return as a signal to skip validation for this property,
 * preventing stack overflow.
 *
 * Example:
 * ```kotlin
 * data class Node(val value: Int, val next: Node?)
 * val circular = Node(1, null)
 * circular.next = circular  // Circular reference
 *
 * // When validating, addPathChecked will detect the circle
 * val result = addPathChecked("next", circular.next) { /* validate */ }
 * // Returns null, which the property invoke operator uses to skip validation
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param T The type of the object being validated
 * @param R The type of the result from executing the block
 * @param name The name of the property or field being validated
 * @param obj The object at this path (checked for circular references)
 * @param block The validation logic to execute if no circular reference is detected
 * @return The result of executing the block, or null if a circular reference is detected
 */
context(v: Validation)
public inline fun <T, R> addPathChecked(
    name: String,
    obj: T,
    block: context(Validation)() -> R,
): R? {
    val parent = v.path
    // Check for circular reference
    if (obj != null && parent.containsObject(obj)) return null
    return addPath(name, obj, block)
}

/**
 * Appends text to the current path name and executes a block.
 *
 * This is used for creating synthetic path segments like array indices or map keys
 * without creating a new path node in the linked list. After modifying the path name,
 * the block is executed.
 *
 * Example usage in collection validation:
 * ```kotlin
 * appendPath("[0]<collection element>") {
 *     // Validate element at index 0
 *     // If path was "items", it becomes "items[0]<collection element>"
 * }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @param R The type of the result from executing the block
 * @param text The text to append to the current path name
 * @param block The validation logic to execute with the modified path
 * @return The result of executing the block
 */
context(v: Validation)
public inline fun <R> appendPath(
    text: String,
    block: context(Validation)() -> R,
): R {
    val path = v.path.copy(name = v.path.name + text)
    return block(v.copy(path = path))
}

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
 * Executes a validation block with a message transformation function applied to all errors.
 *
 * This function wraps the error accumulator so that each validation message accumulated
 * during the block's execution is transformed by the provided function. This is useful
 * for enriching messages with additional context or modifying their content.
 *
 * The transformation is applied to all validation errors that occur within the block,
 * including nested validation errors. This is used internally by the [constrain] function
 * to add constraint details (input value and constraint ID) to error messages.
 *
 * Example internal usage:
 * ```kotlin
 * mapEachMessage({ logAndAddDetails(it, input, "kova.string.min") }) {
 *     // All messages from this block will have details added
 *     satisfies(condition) { "kova.string.min".resource }
 * }
 * ```
 *
 * @param R the type of the validation result
 * @param transform The function to apply to each validation message
 * @param block The validation logic to execute
 * @return The result of executing the validation block
 */
@PublishedApi
context(v: Validation)
internal inline fun <R> mapEachMessage(
    noinline transform: (Message) -> Message,
    block: context(Validation)() -> R,
): R = block(v.copy(acc = { accumulate(it.map(transform)) }))

/**
 * Represents a path through the object graph during validation.
 *
 * This class forms a linked list structure that tracks the validation path and
 * object references for circular reference detection.
 *
 * @property name The name of the current path segment (e.g., "address", "city")
 * @property obj The object at this path point (used for circular reference detection)
 * @property parent The parent path segment, or null if this is the root
 */
public data class Path(
    val name: String,
    val obj: Any?,
    val parent: Path?,
) {
    /**
     * The full dotted path from root to this point, excluding the root class name.
     *
     * Examples: "address.city", "name" (not "User.address.city" or "Person.name")
     */
    val fullName: String
        get() {
            if (parent == null || parent.name.isEmpty()) return name
            return if (name.isEmpty()) parent.fullName else "${parent.fullName}.$name"
        }

    /**
     * Checks if the given object appears anywhere in the path ancestry.
     *
     * Uses object identity (===) to detect circular references. This prevents
     * infinite loops when validating objects with circular references.
     *
     * @param target The object to search for in the path
     * @return true if the object is found in the path ancestry
     */
    public fun containsObject(target: Any): Boolean {
        if (obj === target) return true
        return parent?.containsObject(target) ?: false
    }
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
