package org.komapper.extension.validator

import java.time.Clock
import kotlin.contracts.contract

/**
 * Context object that tracks the state of validation execution.
 *
 * Contains information about the validation root, current path, logs, and configuration.
 * The context is immutable and threaded through the validation process, with each
 * validator potentially creating a new context with updated state.
 *
 * @property root The root object's qualified name (e.g., "com.example.User")
 * @property path The current validation path, tracking nested objects and circular references
 * @property config Validation configuration settings
 */
data class Validation(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val config: ValidationConfig = ValidationConfig(),
)

/** Whether validation should stop at the first failure. */
context(c: Validation)
val failFast: Boolean get() = c.config.failFast

fun interface Accumulate {
    sealed class Value<out T> {
        abstract val value: T
    }

    class Ok<T>(
        override val value: T,
    ) : Value<T>()

    class Error(
        val validationToken: ValidationToken,
    ) : Value<Nothing>() {
        override val value: Nothing
            get() = validationToken.raise()
    }

    @IgnorableReturnValue
    fun accumulate(messages: List<Message>): Value<Nothing>
}

@IgnorableReturnValue
context(acc: Accumulate)
fun accumulate(messages: List<Message>) = acc.accumulate(messages)

context(_: Accumulate)
fun raise(messages: List<Message>): Nothing = accumulate(messages).value

context(_: Accumulate)
fun raise(message: Message): Nothing = raise(listOf(message))

@IgnorableReturnValue
context(_: Accumulate)
inline fun <R> accumulating(block: context(Accumulate) () -> R): Accumulate.Value<R> {
    lateinit var outsideError: Accumulate.Value<Nothing>
    // raise/error is only used after outsideError is initialized
    return recoverValidation({ outsideError }) {
        val error = Accumulate.Error(this)
        block {
            outsideError = accumulate(it)
            error
        }.let(Accumulate::Ok)
    }
}

/**
 * The clock used for temporal validation constraints (past, future, etc.).
 *
 * Delegates to [ValidationConfig.clock].
 */
context(c: Validation)
val clock: Clock get() = c.config.clock

/**
 * Evaluates a condition and returns the appropriate constraint result.
 *
 * Returns [ValidationResult.Success] if the condition is true,
 * or [ValidationResult.Failure] with the given message if false.
 *
 * Example with message factory function:
 * ```kotlin
 * satisfies(
 *     value > 0
 * ) { ctx ->
 *     Message.Resource(ctx.createMessageContext(listOf(value)))
 * }
 * ```
 *
 * Example with MessageProvider:
 * ```kotlin
 * val messageProvider = MessageProvider.resource()
 * satisfies(
 *     value > 0,
 *     messageProvider(value)
 * )
 * ```
 *
 * @param condition The condition to evaluate
 * @param message A function that accepts a Validation and returns a Message
 * @return The constraint result
 */
context(_: Accumulate)
fun satisfies(
    condition: Boolean,
    message: MessageProvider,
) {
    contract { returns() implies condition }
    if (!condition) raise(message())
}

/**
 * Creates a resource-based validation message.
 *
 * Use this method to create internationalized messages that load text from `kova.properties`.
 * The message key is determined by the constraint ID in the validation context.
 * Arguments are provided as a vararg and automatically converted to indexed pairs for
 * MessageFormat substitution (i.e., the first argument becomes {0}, second becomes {1}, etc.).
 *
 * Example usage in a constraint:
 * ```kotlin
 * constrain("kova.number.min") { context ->
 *     val minValue = 0
 *     satisfies(
 *         context.input >= minValue,
 *         context.resource(minValue)
 *     )
 * }
 * ```
 *
 * The corresponding entry in `kova.properties` would be:
 * ```
 * kova.number.min=The value must be greater than or equal to {0}.
 * ```
 *
 * For multiple arguments:
 * ```kotlin
 * constrain("kova.number.range") { context ->
 *     val minValue = 0
 *     val maxValue = 100
 *     satisfies(
 *         context.input in minValue..maxValue,
 *         context.resource(minValue, maxValue)
 *     )
 * }
 * ```
 *
 * With corresponding resource:
 * ```
 * kova.number.range=The value must be between {0} and {1}.
 * ```
 *
 * @param args Arguments to be interpolated into the message template using MessageFormat
 * @return A [Message.Resource] instance configured with the provided arguments
 */
context(c: Validation)
fun String.resource(vararg args: Any?): Message.Resource = Message.Resource(this, c.root, c.path, null, args = args)

context(_: Validation)
val String.resource: Message.Resource get() = resource()

/**
 * Configuration settings for validation execution.
 *
 * @property failFast If true, validation stops at the first failure instead of collecting all errors.
 *                    Default is false (collect all errors).
 * @property clock The clock used for temporal validation constraints (past, future, pastOrPresent, futureOrPresent).
 *                 Defaults to [Clock.systemDefaultZone]. Use a fixed clock for deterministic testing.
 * @property logger Optional callback function for receiving debug log messages during validation.
 *                  If null (default), no logging is performed. Each log message contains information
 *                  about constraint satisfaction/violation, including constraint ID, root, path, and input value.
 *
 * Example:
 * ```kotlin
 * // Basic configuration
 * val config = ValidationConfig(
 *     failFast = true,
 *     logger = { message -> println(message) }
 * )
 * val result = validator.tryValidate(input, config)
 *
 * // Configuration with fixed clock for testing temporal validators
 * val fixedClock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
 * val testConfig = ValidationConfig(clock = fixedClock)
 * val dateValidator = Kova.localDate().past()
 * dateValidator.tryValidate(LocalDate.of(2024, 12, 31), config = testConfig)
 * ```
 */
data class ValidationConfig(
    val failFast: Boolean = false,
    val clock: Clock = Clock.systemDefaultZone(),
    val logger: ((LogEntry) -> Unit)? = null,
)

/**
 * Initializes the validation root if not already set.
 *
 * The root represents the top-level object being validated and is displayed
 * in error messages. This function is typically called once at the start of
 * validation by ObjectSchema or ObjectFactory.
 *
 * If the root is already initialized, returns the context unchanged.
 *
 * Example:
 * ```kotlin
 * val context = Validation().addRoot("com.example.User", userInstance)
 * // context.root == "com.example.User"
 * ```
 *
 * @param name The qualified name of the root object (typically class name)
 * @param obj The root object instance (used for circular reference detection)
 * @return A new context with the root initialized, or the same context if already set
 */
context(c: Validation)
inline fun <R> addRoot(
    name: String,
    obj: Any?,
    block: context(Validation) () -> R,
): R =
    block(
        if (c.root.isEmpty()) {
            // initialize root
            c.copy(root = name, path = Path(name = "", obj = obj, parent = null))
        } else {
            c
        },
    )

/**
 * Adds a path segment for nested validation.
 *
 * This creates a new path node in the validation chain, tracking the progression
 * through nested objects and properties. The path is displayed in error messages
 * to indicate exactly where validation failed.
 *
 * Example:
 * ```kotlin
 * val context = Validation()
 *     .addRoot("User", user)
 *     .addPath("address", user.address)
 *     .addPath("city", user.address.city)
 * // context.path.fullName == "address.city"
 * ```
 *
 * @param name The name of the property or field being validated
 * @param obj The object at this path (used for circular reference detection)
 * @return A new context with the extended path
 */
context(c: Validation)
inline fun <R> addPath(
    name: String,
    obj: Any?,
    block: context(Validation) () -> R,
): R {
    val parent = c.path
    val path =
        parent.copy(
            name = name,
            obj = obj,
            parent = parent,
        )
    return block(c.copy(path = path))
}

/**
 * Binds an object reference to the current path node.
 *
 * This updates the object reference at the current path level without changing
 * the path name or structure. It's used in ObjectFactory to bind validated values
 * to the path for circular reference detection.
 *
 * Example:
 * ```kotlin
 * val context = validationContext.bindObject(validatedValue)
 * ```
 *
 * @param obj The object to bind to the current path
 * @return A new context with the updated object reference
 */
context(c: Validation)
inline fun <R> bindObject(
    obj: Any?,
    block: context(Validation) () -> R,
): R {
    val path = c.path.copy(obj = obj)
    return block(c.copy(path = path))
}

/**
 * Adds a path segment with circular reference detection.
 *
 * This function combines [addPath] with circular reference checking. If the object
 * has already appeared in the validation path, it returns a failure to prevent
 * infinite validation loops.
 *
 * The caller (typically ObjectSchema) interprets the failure as a signal to
 * terminate validation early with success, preventing stack overflow.
 *
 * Example:
 * ```kotlin
 * data class Node(val value: Int, val next: Node?)
 * val circular = Node(1, null)
 * circular.next = circular  // Circular reference
 *
 * // When validating, addPathChecked will detect the circle
 * val result = context.addPathChecked("next", circular.next)
 * // Returns failure, which ObjectSchema converts to success to stop validation
 * ```
 *
 * @param name The name of the property or field being validated
 * @param obj The object at this path (checked for circular references)
 * @return Success with extended path, or Failure if circular reference detected
 */
context(c: Validation)
inline fun <T, R> addPathChecked(
    name: String,
    obj: T,
    block: context(Validation) () -> R,
): R? {
    val parent = c.path
    // Check for circular reference
    if (obj != null && parent.containsObject(obj)) return null
    return addPath(name, obj, block)
}

/**
 * Appends text to the current path name.
 *
 * This is used for creating synthetic path segments like array indices or map keys
 * without creating a new path node in the linked list.
 *
 * Example:
 * ```kotlin
 * val context = validationContext.appendPath("[0]<collection element>")
 * // If path was "items", it becomes "items[0]<collection element>"
 * ```
 *
 * @param text The text to append to the current path name
 * @return A new context with the modified path name
 */
context(c: Validation)
inline fun <R> appendPath(
    text: String,
    block: context(Validation) () -> R,
): R {
    val path = c.path.copy(name = c.path.name + text)
    return block(c.copy(path = path))
}

/**
 * Logs a debug message if logging is enabled.
 *
 * This function uses lazy evaluation - the message block is only executed if a logger
 * is configured in [ValidationConfig]. This ensures zero overhead when logging is disabled.
 *
 * The log message typically includes information about constraint validation results,
 * such as constraint ID, root object, validation path, and input value.
 *
 * Example:
 * ```kotlin
 * context.log { "Satisfied(constraintId=kova.string.min, root=User, path=name, input=Alice)" }
 * // The lambda is only evaluated if config.logger is non-null
 * ```
 *
 * @param block Lambda that generates the log message (only called if logger is configured)
 */
context(c: Validation)
inline fun log(block: () -> LogEntry) {
    c.config.logger?.invoke(block())
}

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
data class Path(
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
    fun containsObject(target: Any): Boolean {
        if (obj === target) return true
        return parent?.containsObject(target) ?: false
    }
}

sealed interface LogEntry {
    data class Satisfied(
        val constraintId: String,
        val root: String,
        val path: String,
        val input: Any?,
    ) : LogEntry

    data class Violated(
        val constraintId: String,
        val root: String,
        val path: String,
        val input: Any?,
        val args: List<Any?>,
    ) : LogEntry
}
