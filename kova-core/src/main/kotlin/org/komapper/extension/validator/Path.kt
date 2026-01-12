package org.komapper.extension.validator

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

