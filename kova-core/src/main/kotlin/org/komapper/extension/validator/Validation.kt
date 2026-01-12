package org.komapper.extension.validator

import org.komapper.extension.validator.ValidationIor.Both
import org.komapper.extension.validator.ValidationIor.FailureLike
import org.komapper.extension.validator.ValidationResult.Failure
import org.komapper.extension.validator.ValidationResult.Success
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

