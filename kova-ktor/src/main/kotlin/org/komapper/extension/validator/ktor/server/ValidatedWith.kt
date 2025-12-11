package org.komapper.extension.validator.ktor.server

import org.komapper.extension.validator.ObjectSchema
import kotlin.reflect.KClass

/**
 * Annotation that specifies the [ObjectSchema] to use for validating a class
 * with [SchemaValidator].
 *
 * This annotation should be applied to classes that need validation in Ktor
 * request handling. The annotation value must reference an [ObjectSchema] object
 * declaration (not a class).
 *
 * Example:
 * ```kotlin
 * @ValidatedWith(CustomerSchema::class)
 * @Serializable
 * class Customer(val id: Int, val name: String)
 *
 * object CustomerSchema : ObjectSchema<Customer>({
 *     Customer::id { it.positive() }
 *     Customer::name { it.min(1).max(100) }
 * })
 * ```
 *
 * When used with Ktor's RequestValidation plugin and [SchemaValidator], requests
 * will be automatically validated before reaching the route handler:
 * ```kotlin
 * install(RequestValidation) {
 *     validate(SchemaValidator())
 * }
 * ```
 *
 * @property value The [ObjectSchema] class to use for validation. Must be an object
 *   declaration, not a regular class.
 *
 * @see SchemaValidator
 * @see ObjectSchema
 */
@Target(AnnotationTarget.CLASS)
annotation class ValidatedWith(
    val value: KClass<*>,
)
