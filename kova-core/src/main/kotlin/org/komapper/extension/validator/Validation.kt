package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
import java.time.Clock
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

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
data class Validation(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val config: ValidationConfig = ValidationConfig(),
    val acc: Accumulate = { error("Accumulate context not initialized") },
) {
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
     * fun Validation.alphanumeric(
     *     input: String,
     *     message: MessageProvider = { "kova.string.alphanumeric".resource }
     * ) = input.constrain("kova.string.alphanumeric") {
     *     satisfies(it.all { c -> c.isLetterOrDigit() }, message)
     * }
     *
     * tryValidate { alphanumeric("abc123") } // Success
     * tryValidate { alphanumeric("abc-123") } // Failure
     * ```
     *
     * @param id Unique identifier for the constraint (used in error messages and logging)
     * @param check Constraint logic that validates the input value
     */
    @IgnorableReturnValue
    inline fun <T, R> T.constrain(
        id: String,
        check: Constraint.(T) -> R,
    ) = accumulating {
        mapEachMessage({ logAndAddDetails(it, this@constrain, id) }) {
            val result = Constraint(this).check(this@constrain)
            log {
                LogEntry.Satisfied(
                    constraintId = id,
                    root = root,
                    path = path.fullName,
                    input = this@constrain,
                )
            }
            result
        }
    }

    fun <T> ValidationIor<T>.bind(): T =
        when (this) {
            is Success -> value
            is Failure -> raise(messages)
            is Both -> {
                accumulate(messages)
                value
            }
        }

    inline infix fun <R> ValidationIor<R>.or(block: Validation.() -> R): ValidationIor<R> {
        if (this !is FailureLike) return this
        val other = this@Validation.or(block)
        if (other !is FailureLike) return other
        return (this as? Both ?: other).withMessage("kova.or".resource(messages, other.messages))
    }

    inline infix fun <R> ValidationIor<R>.orElse(block: Validation.() -> R): R = or(block).bind()

    inline fun <T, R> T.name(
        name: String,
        block: Validation.() -> R,
    ): R = addPath(name, this, block)

    inline fun <T : Any> T.schema(block: Validation.() -> Unit) {
        val klass = this::class
        val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
        return addRoot(rootName, this, block)
    }

    @IgnorableReturnValue
    operator fun <T> KProperty0<T>.invoke(block: Validation.(T) -> Unit): Accumulate.Value<Unit> {
        val value = get()
        return addPathChecked(name, value) { accumulating { block(value) } } ?: Accumulate.Ok(Unit)
    }

    /**
     * Binds a validation lambda using property delegation.
     *
     * This enables composing validation logic to build complex nested object validation.
     * The property name is automatically used as the validation path. The validation
     * is executed within an accumulating context that collects errors.
     *
     * Example:
     * ```kotlin
     * val address by { validateAddress(input.address) }
     * ```
     *
     * @param S the type produced by the validation lambda
     * @return an [Accumulate.Value] for accessing the validation result
     */
    operator fun <S> (Validation.() -> S).provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): Accumulate.Value<S> = name(property.name) { accumulating { this@provideDelegate() } }

    /**
     * Creates a resource-based validation message.
     *
     * Use this method to create internationalized messages that load text from `kova.properties`.
     * The message key is the receiver string (typically a constraint ID like "kova.number.min").
     * Arguments are provided as a vararg and used for MessageFormat substitution
     * (i.e., the first argument becomes {0}, second becomes {1}, etc.).
     *
     * Example usage in a constraint:
     * ```kotlin
     * fun Validation.min(
     *     input: Int,
     *     minValue: Int,
     *     message: MessageProvider = { "kova.number.min".resource(minValue) }
     * ) = input.constrain("kova.number.min") {
     *     satisfies(it >= minValue, message)
     * }
     *
     * tryValidate { min(5, 0) } // Success
     * ```
     *
     * The corresponding entry in `kova.properties` would be:
     * ```
     * kova.number.min=The value must be greater than or equal to {0}.
     * ```
     *
     * For multiple arguments:
     * ```kotlin
     * fun Validation.range(
     *     input: Int,
     *     minValue: Int,
     *     maxValue: Int,
     *     message: MessageProvider = { "kova.number.range".resource(minValue, maxValue) }
     * ) = input.constrain("kova.number.range") {
     *     satisfies(it in minValue..maxValue, message)
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
    fun String.resource(vararg args: Any?): Message.Resource = Message.Resource(this, this, root, path, null, args = args)

    val String.resource: Message.Resource get() = resource()
}

/**
 * The clock used for temporal validation constraints (past, future, etc.).
 *
 * Delegates to [ValidationConfig.clock].
 */
val Validation.clock: Clock get() = config.clock

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
 *     past(LocalDate.of(2024, 12, 31))
 * }
 * ```
 */
data class ValidationConfig(
    val failFast: Boolean = false,
    val clock: Clock = Clock.systemDefaultZone(),
    val logger: ((LogEntry) -> Unit)? = null,
)

/**
 * Initializes the validation root if not already set and executes a block.
 *
 * The root represents the top-level object being validated and is displayed
 * in error messages. This function is typically called once at the start of
 * validation by ObjectSchema.
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
 * @param name The qualified name of the root object (typically class name)
 * @param obj The root object instance (used for circular reference detection)
 * @param block The validation logic to execute with the initialized context
 * @return The result of executing the block
 */
inline fun <R> Validation.addRoot(
    name: String,
    obj: Any?,
    block: Validation.() -> R,
): R =
    block(
        if (root.isEmpty()) {
            // initialize root
            copy(root = name, path = Path(name = "", obj = obj, parent = null))
        } else {
            this
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
 * @param name The name of the property or field being validated
 * @param obj The object at this path (used for circular reference detection)
 * @param block The validation logic to execute with the extended path
 * @return The result of executing the block
 */
inline fun <R> Validation.addPath(
    name: String,
    obj: Any?,
    block: Validation.() -> R,
): R {
    val parent = path
    val path =
        parent.copy(
            name = name,
            obj = obj,
            parent = parent,
        )
    return block(copy(path = path))
}

/**
 * Binds an object reference to the current path node and executes a block.
 *
 * This updates the object reference at the current path level without changing
 * the path name or structure. It's used internally to bind validated values
 * to the path for circular reference detection. After updating the object reference,
 * the block is executed.
 *
 * @param obj The object to bind to the current path
 * @param block The validation logic to execute with the updated context
 * @return The result of executing the block
 */
inline fun <R> Validation.bindObject(
    obj: Any?,
    block: Validation.() -> R,
): R {
    val path = path.copy(obj = obj)
    return block(copy(path = path))
}

/**
 * Adds a path segment with circular reference detection and executes a block.
 *
 * This function combines [addPath] with circular reference checking. If the object
 * has already appeared in the validation path, it returns null to prevent
 * infinite validation loops.
 *
 * The caller (typically ObjectSchema) interprets the null return as a signal to
 * skip validation for this property, preventing stack overflow.
 *
 * Example:
 * ```kotlin
 * data class Node(val value: Int, val next: Node?)
 * val circular = Node(1, null)
 * circular.next = circular  // Circular reference
 *
 * // When validating, addPathChecked will detect the circle
 * val result = addPathChecked("next", circular.next) { /* validate */ }
 * // Returns null, which ObjectSchema uses to skip validation
 * ```
 *
 * @param name The name of the property or field being validated
 * @param obj The object at this path (checked for circular references)
 * @param block The validation logic to execute if no circular reference is detected
 * @return The result of executing the block, or null if a circular reference is detected
 */
inline fun <T, R> Validation.addPathChecked(
    name: String,
    obj: T,
    block: Validation.() -> R,
): R? {
    val parent = path
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
 * @param text The text to append to the current path name
 * @param block The validation logic to execute with the modified path
 * @return The result of executing the block
 */
inline fun <R> Validation.appendPath(
    text: String,
    block: Validation.() -> R,
): R {
    val path = path.copy(name = path.name + text)
    return block(copy(path = path))
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
 * @param block Lambda that generates the log entry (only called if logger is configured)
 */
inline fun Validation.log(block: () -> LogEntry) {
    config.logger?.invoke(block())
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
