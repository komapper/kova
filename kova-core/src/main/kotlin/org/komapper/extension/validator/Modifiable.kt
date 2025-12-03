package org.komapper.extension.validator

/**
 * Interface for validators that support value transformation before validation.
 *
 * This interface is primarily used internally by validator implementations to provide
 * transformation methods. For example, [StringValidator] implements this to provide
 * methods like `trim()`, `lowercase()`, and `uppercase()`.
 *
 * Implementation example (internal use):
 * ```kotlin
 * class StringValidatorImpl : StringValidator, Modifiable<String, StringValidator> {
 *     override fun trim(): StringValidator = modify("trim") { it.trim() }
 *     override fun lowercase(): StringValidator = modify("lowercase") { it.lowercase() }
 * }
 * ```
 *
 * Users should use the provided transformation methods on validators rather than
 * calling `modify()` directly:
 * ```kotlin
 * // Recommended: Use built-in methods
 * val validator = Kova.string()
 *     .trim()
 *     .lowercase()
 *     .min(1)
 *     .max(50)
 * ```
 *
 * @param T The type being validated and modified
 * @param R The return type (typically the validator itself for chaining)
 */
interface Modifiable<T, R> {
    /**
     * Adds a transformation that modifies the input value before validation.
     *
     * This method is primarily intended for use by validator implementations to
     * provide transformation methods to users. The transformation is applied
     * before any validation rules.
     *
     * @param name A descriptive name for this modification (for debugging/logging)
     * @param transform Function that transforms the input value
     * @return A new validator with the modification applied (for method chaining)
     */
    fun modify(
        name: String,
        transform: (T) -> T,
    ): R
}
