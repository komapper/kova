package org.komapper.extension.validator

import kotlin.reflect.KProperty0

/**
 * Validates an object using its class name as the validation root.
 *
 * This function initializes the validation root with the object's qualified class name
 * and sets up the validation context for property-level validation using the `invoke`
 * operator on KProperty0 instances. This is the primary entry point for object schema
 * validation.
 *
 * Example:
 * ```kotlin
 * data class User(val name: String, val age: Int)
 *
 * context(_: Validation)
 * fun validate(user: User) = user.schema {
 *     user::name { it.ensureNotBlank().ensureLengthInRange(1..100) }
 *     user::age { it.ensureAtLeast(0).ensureAtMost(120) }
 * }
 *
 * tryValidate { validate(User("Alice", 30)) }
 * ```
 *
 * @param Validation (context parameter) The validation context for constraint checking and error accumulation
 * @receiver T The object to validate
 * @param T The type of the object being validated (must be Any for reflection)
 * @param block The validation logic that defines constraints for object properties
 */
context(_: Validation)
public inline fun <T : Any> T.schema(block: context(Validation) Schema<T>.() -> Unit) {
    val obj = this
    val klass = this::class
    val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
    return addRoot(rootName, this) { block(Schema(obj)) }
}

/**
 * Schema validation context that provides property-based validation operators.
 *
 * This class is used internally by [schema] to enable property validation syntax.
 * It provides the [invoke] operator for [KProperty0] that allows validating properties
 * using the `property { constraints }` syntax.
 *
 * @param T The type of the object being validated
 * @property obj The object being validated
 */
public class Schema<T>(
    private val obj: T,
) {
    /**
     * Validates a property value within a schema using the invoke operator.
     *
     * This operator function enables the schema validation syntax where properties are
     * validated using `property { constraints }`. It automatically:
     * - Extracts the property value using reflection
     * - Adds the property name to the validation path
     * - Detects circular references to prevent infinite loops
     * - Accumulates validation errors in the current context
     *
     * This is typically used within a `schema { }` block for object validation.
     *
     * Example:
     * ```kotlin
     * data class User(val name: String, val age: Int, val email: String?)
     *
     * context(_: Validation)
     * fun validate(user: User) = user.schema {
     *     user::name { it.ensureNotBlank().ensureLengthAtLeast(1) }
     *     user::age { it.ensureAtLeast(0).ensureAtMost(120) }
     *     user::email { it.ensureNullOr { it.ensureMatches(emailRegex) } }
     * }
     * ```
     *
     * If a circular reference is detected (the object already appears in the validation
     * path), the validation is skipped and returns [Accumulate.Ok] to prevent stack overflow.
     *
     * @param Validation (context parameter) The validation context for constraint checking and error accumulation
     * @receiver KProperty0<T> The property reference to validate
     * @param T The type of the property value
     * @param block The validation logic to apply to the property value
     * @return [Accumulate.Value] wrapping Unit, or [Accumulate.Ok] if circular reference detected
     */
    @IgnorableReturnValue
    context(_: Validation)
    public operator fun <T> KProperty0<T>.invoke(block: context(Validation)(T) -> Unit): Accumulate.Value<Unit> {
        val value = get()
        return addPathChecked(name, value) { accumulating { block(value) } } ?: Accumulate.Ok(Unit)
    }

    /**
     * Adds a custom constraint to the schema object.
     *
     * This is a convenience method that delegates to the top-level [constrain] function,
     * allowing you to add object-level constraints within a schema block.
     *
     * Example:
     * ```kotlin
     * data class Period(val startDate: LocalDate, val endDate: LocalDate)
     *
     * context(_: Validation)
     * fun validate(period: Period) = period.schema {
     *     period::startDate { it.ensurePastOrPresent() }
     *     period::endDate { it.ensureFutureOrPresent() }
     *     constrain("period.dateOrder") {
     *         satisfies(it.startDate <= it.endDate) { text("Start date must be before end date") }
     *     }
     * }
     * ```
     *
     * @param Validation (context parameter) The validation context for constraint checking and error accumulation
     * @param id Unique identifier for the constraint (used in error messages and logging)
     * @param check Constraint logic that validates the schema object; receives the [Validation] context
     *              and executes within a [Constraint] receiver scope
     * @return The validated schema object (allows method chaining)
     */
    @IgnorableReturnValue
    context(_: Validation)
    public fun constrain(
        id: String,
        check: context(Validation) Constraint.(T) -> Unit,
    ): T = obj.constrain(id, check)
}
