package org.komapper.extension.validator

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

class QuickStartTest :
    FunSpec({

        beforeSpec {
            Locale.setDefault(Locale.US)
        }

        context("basic") {
            context(_: Validation)
            fun validateProductName(name: String): String {
                ensureNotBlank(name)
                ensureLengthInRange(name, 1..100)
                return name
            }

            test("tryValidate") {
                val result = tryValidate { validateProductName("Wireless Mouse") }
                if (result.isSuccess()) {
                    println("Valid: ${result.value}") // Valid: Wireless Mouse
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeSuccess()
            }

            test("validate") {
                try {
                    val value = validate { validateProductName("Wireless Mouse") }
                    println("Valid: $value") // Valid: Wireless Mouse
                } catch (e: ValidationException) {
                    e.messages.forEach { println("Invalid: $it") }

                    fail("never reach here")
                }
            }
        }

        context("multiple") {
            context(_: Validation)
            fun validateProductName(name: String): String {
                ensureNotBlank(name)
                ensureLengthInRange(name, 1..100)
                return name
            }

            context(_: Validation)
            fun validatePrice(price: Double): Double {
                ensureInClosedRange(price, 0.0..1000.0)
                return price
            }

            test("success") {
                val result =
                    tryValidate {
                        val name = validateProductName("Wireless Mouse")
                        val price = validatePrice(29.99)
                        name to price
                    }

                if (result.isSuccess()) {
                    println("Valid: ${result.value}") // Valid: (Wireless Mouse, 29.99)
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeSuccess()
            }

            test("failure") {
                val result =
                    tryValidate {
                        val name = validateProductName("Wireless Mouse")
                        val price = validatePrice(1029.99)
                        name to price
                    }

                if (result.isSuccess()) {
                    println("Valid: ${result.value}")
                } else {
                    result.messages.forEach { println("Invalid: $it") }
                }

                result.shouldBeFailure()
            }
        }

        context("object") {
            data class Product(
                val id: Int,
                val name: String,
                val price: Double,
            )

            context(_: Validation)
            fun validate(product: Product) =
                product.schema {
                    product::id { ensureMin(it, 1) }
                    product::name {
                        ensureNotBlank(it)
                        ensureMinLength(it, 1)
                        ensureMaxLength(it, 100)
                    }
                    product::price { ensureMin(it, 0.0) }
                }

            test("test") {
                val result = tryValidate { validate(Product(1, "Mouse", 29.99)) }
                result.shouldBeSuccess()
            }
        }

        context("nested object") {
            data class Address(
                val street: String,
                val city: String,
                val zipCode: String,
            )

            data class Customer(
                val name: String,
                val email: String,
                val address: Address,
            )

            context(_: Validation)
            fun validate(address: Address) =
                address.schema {
                    address::street {
                        ensureNotBlank(it)
                        ensureMinLength(it, 1)
                    }
                    address::city {
                        ensureNotBlank(it)
                        ensureMinLength(it, 1)
                    }
                    address::zipCode { ensureMatches(it, Regex("^\\d{5}(-\\d{4})?$")) }
                }

            context(_: Validation)
            fun validate(customer: Customer) =
                customer.schema {
                    customer::name {
                        ensureNotBlank(it)
                        ensureMinLength(it, 1)
                        ensureMaxLength(it, 100)
                    }
                    customer::email {
                        ensureNotBlank(it)
                        ensureContains(it, "@")
                    }
                    customer::address { validate(it) } // Nested validation
                }

            test("test") {
                val customer =
                    Customer(
                        name = "John Doe",
                        email = "invalid-email",
                        address = Address(street = "", city = "Tokyo", zipCode = "123"),
                    )

                val result = tryValidate { validate(customer) }

                if (result.isSuccess()) {
                    println("Valid")
                } else {
                    println("Invalid")
                    result.messages.joinToString("\n").let { println(it) }
                    // Message(constraintId=kova.charSequence.ensureContains, text='must contain "@"', root=Customer, path=email, input=invalid-email, args=[@])
                    // Message(constraintId=kova.charSequence.ensureNotBlank, text='must not be ensureBlank', root=Customer, path=address.street, input=, args=[])
                    // Message(constraintId=kova.charSequence.minLength, text='must be at least 1 characters', root=Customer, path=address.street, input=, args=[1])
                    // Message(constraintId=kova.charSequence.ensureMatches, text='must match pattern: ^\d{5}(-\d{4})?$', root=Customer, path=address.zipCode, input=123, args=[^\d{5}(-\d{4})?$])
                }

                result.shouldBeFailure()
            }
        }

        context("cross-property ") {
            data class PriceRange(
                val minPrice: Double,
                val maxPrice: Double,
            )

            context(_: Validation)
            fun validate(range: PriceRange) =
                range.schema {
                    range::minPrice { ensureNotNegative(it) }
                    range::maxPrice { ensureNotNegative(it) }

                    // Validate relationship
                    range.constrain("priceRange") {
                        satisfies(it.minPrice <= it.maxPrice) {
                            text("minPrice must be less than or equal to maxPrice")
                        }
                    }
                }

            test("test") {
                val result = tryValidate { validate(PriceRange(10.0, 100.0)) }

                result.shouldBeSuccess()
            }
        }

        context("fail fast") {
            context(_: Validation)
            fun validateProductName(name: String) {
                ensureNotBlank(name)
                ensureLengthInRange(name, 1..100)
            }

            test("test") {
                val result =
                    tryValidate(ValidationConfig(failFast = true)) {
                        validateProductName("Wireless Mouse")
                    }

                result.shouldBeSuccess()
            }
        }

        context("custom clock") {
            context(_: Validation)
            fun validateDate(date: LocalDate) {
                ensureFuture(date)
            }

            test("test") {
                val fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneId.of("UTC"))
                val result =
                    tryValidate(config = ValidationConfig(clock = fixedClock)) {
                        val date = LocalDate.of(2024, 6, 20)
                        ensureFuture(date) // Uses the fixed clock for comparison
                    }

                result.shouldBeSuccess()
            }
        }

        context("debug logging") {
            context(_: Validation)
            fun validateUsername(username: String) {
                ensureMinLength(username, 3)
                ensureMaxLength(username, 20)
            }

            test("test") {
                val result =
                    tryValidate(
                        config =
                            ValidationConfig(
                                logger = { logEntry -> println("[Validation] $logEntry") },
                            ),
                    ) {
                        validateUsername("ab")
                    }

                result.shouldBeFailure()
            }
        }
    })
